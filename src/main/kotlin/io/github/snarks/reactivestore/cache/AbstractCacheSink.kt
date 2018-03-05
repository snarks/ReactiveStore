package io.github.snarks.reactivestore.cache

import io.github.snarks.reactivestore.utils.*
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class AbstractCacheSink<T> : CacheSink<T> {

	protected abstract val scheduler: Scheduler
	protected abstract val loader: Loader<T>
	protected abstract var current: Holder<T>

	override fun update(updater: Updater<T>) {
		scheduler.scheduleDirect { updateImpl(updater) }
	}

	private fun updateImpl(updater: Updater<T>) {
		val prevHolder = current

		val prev = prevHolder.status
		val change = updater.applyUpdate(prev)
		val next = change.nextStatus(prev) ?: return // do nothing if no change happens

		prevHolder.disposer?.dispose()

		val disposer = change.makeTaskDisposer()

		val nextHolder = Holder(next, disposer)
		current = nextHolder

		if (change is Defer) startDeferredTask(change, disposer)
	}

	private fun Change<T>.makeTaskDisposer(): CompositeDisposable? {
		return if (this is Defer && ignoreIfUpdated) CompositeDisposable() else null
	}

	private fun startDeferredTask(defer: Defer<T>, disposables: CompositeDisposable?) {
		val future = defer.future ?: loader.asFutureUpdater()

		val subscription = future
				.observeOn(scheduler)
				.subscribe { u -> updateImpl(u) }

		disposables?.add(subscription)
	}

	data class Holder<out T>(val status: Status<T>, internal val disposer: Disposable?) {
		constructor() : this(Empty, null)
	}
}

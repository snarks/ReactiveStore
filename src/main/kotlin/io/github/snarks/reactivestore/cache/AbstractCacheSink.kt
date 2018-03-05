package io.github.snarks.reactivestore.cache

import io.github.snarks.reactivestore.utils.*
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Provides a skeletal implementation of the CacheSink interface
 */
abstract class AbstractCacheSink<T> : CacheSink<T> {

	/**
	 * Where [update] operations will be done on.
	 *
	 * This implementation relies on this `scheduler` for its thread-safety, so don't use parallel schedulers here.
	 */
	protected abstract val scheduler: Scheduler

	/**
	 * The default loader to be used when `load` / `reload` / etc. is called on this sink
	 *
	 * Technically:
	 * - If an updater was applied on this sink
	 * - … and that updater returned a [Defer]
	 * - … and the [Defer.future] field is `null`
	 * - Create a `Single<Updater>` from this `loader`
	 * - Then use that as the replacement for `Defer.future`
	 */
	protected abstract val loader: Loader<T>

	/**
	 * The current state of this sink.
	 *
	 * This field will be accessed and set on the provided [scheduler] whenever [update] is called.
	 */
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

	/**
	 * Represents the current state of an [AbstractCacheSink] implementor
	 *
	 * This holds both the current [Status] and, possibly, a [disposer] which will be invoked when this holder is
	 * replaced with another.
	 */
	data class Holder<out T>(val status: Status<T>, internal val disposer: Disposable?) {
		/** Creates a default instance with an [Empty] status */
		constructor() : this(Empty, null)
	}
}

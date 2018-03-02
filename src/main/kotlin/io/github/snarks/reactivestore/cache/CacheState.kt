package io.github.snarks.reactivestore.cache

import io.github.snarks.reactivestore.overhaul.utils.*
import io.github.snarks.reactivestore.utils.*
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign

abstract class CacheState<T> : CacheSink<T> {

	protected abstract val scheduler: Scheduler
	protected abstract val loader: Loader<T>
	protected abstract var current: Holder<T>

	override fun update(updater: Updater<T>) {
		scheduler.scheduleDirect { updateImpl(updater) }
	}

	private fun updateImpl(updater: Updater<T>) {
		val prevHolder = current
		val prev = prevHolder.status
		val change = updater.applyUpdate(prev, loader)

		when (change) {
			NoChange -> return
			is Defer -> {
				val disposables = CompositeDisposable()

				prevHolder.disposable?.dispose()
				current = Holder(Loading(prev), disposables.takeIf { change.ignoreIfUpdated })

				disposables += change.future.observeOn(scheduler).subscribe(::updateImpl)
			}
			is ImmediateAction -> {
				prevHolder.disposable?.dispose()
				current = Holder(change.toStatus(prev), null)
			}
		}
	}

	class Holder<T>(
			val status: Status<T>,
			val disposable: Disposable?) {
		constructor() : this(Empty(), null)
	}
}

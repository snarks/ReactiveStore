package io.github.snarks.reactivestore.utils.internal

import io.github.snarks.reactivestore.utils.Updater
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

internal fun <T : Any> applyUpdate(
		updater: Updater<T>, loader: Single<out T>, scheduler: Scheduler,
		getPrev: () -> Content<T>, emitNext: (Content<T>) -> Unit) {

	scheduler.scheduleDirect {
		val prev = getPrev()
		val prevStatus = prev.status
		val nextStatus = updater(prevStatus, loader)

		if (prevStatus !== nextStatus) {
			prev.onRemove()

			val next = Content(nextStatus)
			emitNext(next)

			next.onStart { u -> applyUpdate(u, loader, scheduler, getPrev, emitNext) }
		}
	}
}

internal fun <T> Observable<T>.optObserveOn(scheduler: Scheduler?): Observable<T> =
		if (scheduler == null) this else observeOn(scheduler)

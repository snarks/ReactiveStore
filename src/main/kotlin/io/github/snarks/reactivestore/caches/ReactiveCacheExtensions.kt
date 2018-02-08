package io.github.snarks.reactivestore.caches

import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable
import io.reactivex.SingleSource


/**
 * Convenience method to call [ReactiveCache.update] and then [ReactiveCache.observe]
 *
 * The default [updater] is [Updaters.auto].
 */
fun <T : Any> ReactiveCache<T>.load(updater: Updater<T> = Updaters.auto()): Observable<LoadStatus<T>> {
	update(updater)
	return observe()
}

/** Alias of `update(Updaters.cancelLoading())` */
fun <T : Any> ReactiveCache<T>.cancelLoading() {
	update(Updaters.cancelLoading())
}

/** Alias of `update(Updaters.resetFailure())` */
fun <T : Any> ReactiveCache<T>.resetFailure() {
	update(Updaters.resetFailure())
}

/** Alias of `update(Updaters.reload())` */
fun <T : Any> ReactiveCache<T>.reload(customLoader: SingleSource<T>? = null) {
	update(Updaters.reload(customLoader))
}

/** Alias of `update(Updaters.set())` */
fun <T : Any> ReactiveCache<T>.set(value: T?) {
	update(Updaters.set(value))
}

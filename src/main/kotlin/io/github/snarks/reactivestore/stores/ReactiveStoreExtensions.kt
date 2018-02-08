package io.github.snarks.reactivestore.stores

import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable
import io.reactivex.SingleSource


/**
 * Convenience method to call [ReactiveStore.update] and then [ReactiveStore.observe]
 *
 * The default [updater] is [Updaters.auto].
 */
fun <K : Any, V : Any> ReactiveStore<K, V>.load(key: K, updater: Updater<V> = Updaters.auto()): Observable<LoadStatus<V>> {
	update(key, updater)
	return observe(key)
}

/** Alias of `update(Updaters.cancelLoading())` */
fun <K : Any, V : Any> ReactiveStore<K, V>.cancelLoading(key: K) {
	update(key, Updaters.cancelLoading())
}

/** Alias of `update(Updaters.resetFailure())` */
fun <K : Any, V : Any> ReactiveStore<K, V>.resetFailure(key: K) {
	update(key, Updaters.resetFailure())
}

/** Alias of `update(Updaters.reload())` */
fun <K : Any, V : Any> ReactiveStore<K, V>.reload(key: K, customLoader: SingleSource<V>? = null) {
	update(key, Updaters.reload(customLoader))
}

/** Alias of `update(Updaters.set())` */
operator fun <K : Any, V : Any> ReactiveStore<K, V>.set(key: K, value: V?) {
	update(key, Updaters.set(value))
}

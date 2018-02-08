package io.github.snarks.reactivestore.stores

import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable


/**
 * An observable container that stores multiple values
 *
 * Each `value` put in the store has an associated `key` which can be used to retrieve said value.
 */
interface ReactiveStore<K : Any, V : Any> {

	/**
	 * Updates the content associated to the given [key] with the given [updater]
	 *
	 * @see Updaters
	 */
	fun update(key: K, updater: Updater<V>)

	/**
	 * Emits the current status and subsequent updates associated to the given [key]
	 *
	 * The first item emitted by the observable is the current status as of the time of its subscription.
	 */
	fun observe(key: K): Observable<LoadStatus<V>>

	/**
	 * Emits all the currently loaded values of this store
	 *
	 * The emitted values will be the ones that exist as of the time of subscription.
	 */
	fun contents(): Observable<V>

	/**
	 * Emits all the keys with associated content on this store
	 *
	 * The emitted values will be the ones that exist as of the time of subscription.
	 */
	fun keys(): Observable<K>
}

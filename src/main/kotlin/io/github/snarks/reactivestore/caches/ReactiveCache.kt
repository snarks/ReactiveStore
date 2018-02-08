package io.github.snarks.reactivestore.caches

import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable


/**
 * An observable container that stores a single value
 */
interface ReactiveCache<T : Any> {

	/**
	 * Updates the contents of this cache with the given [updater]
	 *
	 * @see Updaters
	 */
	fun update(updater: Updater<T>)

	/**
	 * Emits the current status and subsequent updates of this cache
	 *
	 * The first item emitted by the observable is the current status as of the time of its subscription.
	 */
	fun observe(): Observable<LoadStatus<T>>
}

package io.github.snarks.reactivestore.utils

import io.reactivex.Single


/**
 * A function the transitions one [LoadStatus] to the next
 *
 * If the return value of this function is the same as `currentStatus`, it means no change should happen in the store.
 *
 * Updaters **should be stateless** since they can be called in concurrent contexts.
 */
typealias Updater<T> = (currentStatus: LoadStatus<T>, defaultLoader: Single<out T>) -> LoadStatus<T>

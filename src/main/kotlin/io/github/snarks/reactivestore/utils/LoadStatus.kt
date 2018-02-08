package io.github.snarks.reactivestore.utils

import io.reactivex.Single


/**
 * Represents the status of a cache or store
 *
 * It can be any of the following:
 * - [Empty]
 * - [Loading]
 * - [Failed]
 * - [Loaded]
 */
sealed class LoadStatus<out T : Any> {
	abstract val lastStableStatus: StableStatus<T>
}

sealed class StableStatus<out T : Any> : LoadStatus<T>() {
	override val lastStableStatus: StableStatus<T> get() = this
}

sealed class PendingStatus<out T : Any> : LoadStatus<T>()


// ------------------------------------------------------------------------------------------------------------------ //


/** The status when a cache or store has nothing in it */
object Empty : StableStatus<Nothing>() {
	override fun toString(): String = "Empty"
}

/** The status when the cache or store has something in it */
data class Loaded<out T : Any>(val value: T) : StableStatus<T>()

/** The status when a cache or store is currently loading something */
data class Loading<out T : Any>(override val lastStableStatus: StableStatus<T>, val loader: Single<out T>) : PendingStatus<T>() {
	constructor(prev: LoadStatus<T>, loader: Single<out T>) : this(prev.lastStableStatus, loader)
}

/** The status when the loading had failed */
data class Failed<out T : Any>(override val lastStableStatus: StableStatus<T>, val error: Throwable) : PendingStatus<T>() {
	constructor(prev: LoadStatus<T>, error: Throwable) : this(prev.lastStableStatus, error)
}

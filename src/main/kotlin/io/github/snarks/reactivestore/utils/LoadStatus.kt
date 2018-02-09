/*
 * Copyright 2018 James Cruz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	/** The latest stable status of the cache or store */
	abstract val lastStableStatus: StableStatus<T>
}

/** Statuses where there's no pending action in the store or cache */
sealed class StableStatus<out T : Any> : LoadStatus<T>() {
	override val lastStableStatus: StableStatus<T> get() = this
}

/** Statuses that are meant to be just temporary */
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

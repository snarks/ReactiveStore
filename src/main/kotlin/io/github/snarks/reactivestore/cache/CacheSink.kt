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
package io.github.snarks.reactivestore.cache

import io.github.snarks.reactivestore.utils.*

/**
 * An object that consumes [Updater]s
 */
interface CacheSink<in T : Any> {
	/**
	 * Updates this object with the provided [updater]
	 */
	fun update(updater: Updater<T>)
}

// ------------------------------------------------------------------------------------------------------------------ //
// Mapping Functions

/**
 * Clears the value of this cache
 *
 * Alias of `update(Updater.clear())`
 *
 * @see Updater.clear
 */
fun <T : Any> CacheSink<T>.clear() {
	update(Updater.clear())
}

/**
 * Sets a new value for this cache
 *
 * Alias of `update(Updater.set())`
 *
 * @see Updater.set
 */
fun <T : Any> CacheSink<T>.set(newValue: T?) {
	update(Updater.set(newValue))
}

/**
 * Sets this cache to a failure state
 *
 * Alias of `update(Updater.fail())`
 *
 * @see Updater.fail
 */
fun <T : Any> CacheSink<T>.fail(error: Throwable) {
	update(Updater.fail(error))
}

/**
 * Loads a new value for this cache if there's no current value
 *
 * Alias of `update(Updater.autoLoad())`
 *
 * @param customLoader The custom loader to be used. If null, the default loader will be used
 * @param ignoreIfUpdated If the loading will be cancelled if another update happened before it finishes
 * @see Updater.autoLoad
 */
fun <T : Any> CacheSink<T>.load(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true) {
	update(Updater.autoLoad(customLoader, ignoreIfUpdated))
}

/**
 * Reloads a new value
 *
 * Alias of `update(Updater.reload())`
 *
 * @param customLoader The custom loader to be used. If null, the default loader will be used
 * @param ignoreIfUpdated If the loading will be cancelled if another update happened before it finishes
 * @see Updater.reload
 */
fun <T : Any> CacheSink<T>.reload(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true) {
	update(Updater.reload(customLoader, ignoreIfUpdated))
}

/**
 * Reverts to the last value if this cache is loading or in a failed state
 *
 * Alias of `update(Updater.revert())`
 *
 * @see Updater.revert
 */
fun <T : Any> CacheSink<T>.revert() {
	update(Updater.revert())
}

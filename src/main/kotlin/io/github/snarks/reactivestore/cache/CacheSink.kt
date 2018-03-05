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

import io.github.snarks.reactivestore.utils.Loader
import io.github.snarks.reactivestore.utils.Updater

interface CacheSink<in T : Any> {
	fun update(updater: Updater<T>)
}

fun <T : Any> CacheSink<T>.clear() {
	update(Updater.clear())
}

fun <T : Any> CacheSink<T>.set(newValue: T) {
	update(Updater.set(newValue))
}

fun <T : Any> CacheSink<T>.fail(error: Throwable) {
	update(Updater.fail(error))
}

fun <T : Any> CacheSink<T>.load(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true) {
	update(Updater.autoLoad(customLoader, ignoreIfUpdated))
}

fun <T : Any> CacheSink<T>.reload(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true) {
	update(Updater.reload(customLoader, ignoreIfUpdated))
}

fun <T : Any> CacheSink<T>.revert() {
	update(Updater.revert())
}

// TODO mapping functions

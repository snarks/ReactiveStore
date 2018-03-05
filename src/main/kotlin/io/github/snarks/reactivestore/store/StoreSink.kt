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
package io.github.snarks.reactivestore.store

import io.github.snarks.reactivestore.cache.CacheSink
import io.github.snarks.reactivestore.utils.Keyed
import io.github.snarks.reactivestore.utils.Loader
import io.github.snarks.reactivestore.utils.Updater

interface StoreSink<in K, in V> {
	fun update(key: K, updater: Updater<V>)
}

fun <K, V> StoreSink<K, V>.cacheSinkFor(key: K): CacheSink<V> {
	val orig = this
	return object : CacheSink<V> {
		override fun update(updater: Updater<V>) = orig.update(key, updater)
	}
}

fun <K, V, L> StoreSink<K, V>.mapKeys(fn: (L) -> K): StoreSink<L, V> {
	val orig = this
	return object : StoreSink<L, V> {
		override fun update(key: L, updater: Updater<V>) {
			orig.update(fn(key), updater)
		}
	}
}

// ------------------------------------------------------------------------------------------------------------------ //

fun <K, V> StoreSink<K, V>.clear(key: K) {
	update(key, Updater.clear())
}

fun <K, V> StoreSink<K, V>.put(key: K, newValue: V) {
	update(key, Updater.set(newValue))
}

fun <K, V : Keyed<K>> StoreSink<K, V>.put(newValue: V) {
	put(newValue.key, newValue)
}

fun <K, V> StoreSink<K, V>.fail(key: K, error: Throwable) {
	update(key, Updater.fail(error))
}

fun <K, V> StoreSink<K, V>.load(
		key: K,
		customLoader: Loader<V>? = null,
		ignoreIfUpdated: Boolean = true) {
	update(key, Updater.autoLoad(customLoader, ignoreIfUpdated))
}

fun <K, V> StoreSink<K, V>.reload(
		key: K,
		customLoader: Loader<V>? = null,
		ignoreIfUpdated: Boolean = true) {
	update(key, Updater.reload(customLoader, ignoreIfUpdated))
}

fun <K, V> StoreSink<K, V>.revert(key: K) {
	update(key, Updater.revert())
}

// TODO mapping functions

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
package io.github.snarks.reactivestore.overhaul.store

import io.github.snarks.reactivestore.overhaul.cache.Cache
import io.github.snarks.reactivestore.overhaul.utils.Status
import io.github.snarks.reactivestore.overhaul.utils.Updater
import io.reactivex.Observable
import io.reactivex.Single

interface Store<K, V> : StoreSink<K, V>, StoreSource<K, V>

fun <K, V> Store<K, V>.cacheFor(key: K): Cache<V> {
	return object : Cache<V> {
		override fun update(updater: Updater<V>) = update(key, updater)
		override fun current(): Single<Status<V>> = currentFor(key)
		override fun updates(): Observable<Status<V>> = updatesFor(key)
		override fun observe(): Observable<Status<V>> = observeFor(key)
	}
}

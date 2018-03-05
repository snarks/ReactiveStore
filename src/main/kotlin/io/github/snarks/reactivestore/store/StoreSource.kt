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

import io.github.snarks.reactivestore.cache.CacheSource
import io.github.snarks.reactivestore.utils.*
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

interface StoreSource<K : Any, V : Any> {

	fun current(): Single<Map<K, Status<V>>>

	fun updates(): Observable<KeyStatus<K, V>>

	fun currentFor(key: K): Single<Status<V>>

	fun updatesFor(key: K): Observable<Status<V>> = updates().withKey(key)

	fun observeFor(key: K): Observable<Status<V>> = updatesFor(key).startWith(currentFor(key).toObservable())

	fun currentKeys(): Single<Set<K>>

	fun currentStatuses(): Single<Collection<Status<V>>>
}

// ------------------------------------------------------------------------------------------------------------------ //

fun <K : Any, V : Any> StoreSource<K, V>.currentValues(): Single<Collection<V>> {
	return currentStatuses().map {
		it.asSequence().flatMap {
			val content = it.lastContent
			when (content) {
				Empty -> emptySequence()
				is Loaded -> sequenceOf(content.value)
			}
		}.toList()
	}
}

fun <K : Any, V : Any> StoreSource<K, V>.currentValueFor(key: K): Maybe<V> = currentFor(key).contentValue()

fun <K : Any, V : Any> StoreSource<K, V>.cacheSourceFor(key: K): CacheSource<V> {
	return object : CacheSource<V> {
		override fun current(): Single<Status<V>> = currentFor(key)
		override fun updates(): Observable<Status<V>> = updatesFor(key)
		override fun observe(): Observable<Status<V>> = observeFor(key)
	}
}

// ------------------------------------------------------------------------------------------------------------------ //

inline fun <K : Any, V : Any, R : Any> StoreSource<K, V>.transform(
		crossinline fn: (Status<V>) -> Status<R>): StoreSource<K, R> {

	val orig = this
	return object : StoreSource<K, R> {
		override fun current(): Single<Map<K, Status<R>>> {
			return orig.current().map { it.mapValues { (_, v) -> v.transform { fn(it) } } }
		}

		override fun updates(): Observable<KeyStatus<K, R>> {
			return orig.updates().map { (k, v) -> KeyPair(k, v.transform { fn(it) }) }
		}

		override fun currentFor(key: K): Single<Status<R>> = orig.currentFor(key).map { it.transform { fn(it) } }
		override fun updatesFor(key: K): Observable<Status<R>> = orig.updatesFor(key).map { it.transform { fn(it) } }
		override fun observeFor(key: K): Observable<Status<R>> = orig.observeFor(key).map { it.transform { fn(it) } }

		override fun currentKeys(): Single<Set<K>> = orig.currentKeys()

		override fun currentStatuses(): Single<Collection<Status<R>>> {
			return orig.currentStatuses().map { it.map { it.transform { fn(it) } } }
		}
	}
}

fun <K : Any, V : Any, R : Any> StoreSource<K, V>.flatMap(fn: (V) -> Status<R>): StoreSource<K, R> {
	return transform { it.flatMap(fn) }
}

fun <K : Any, V : Any, R : Any> StoreSource<K, V>.map(fn: (V) -> R): StoreSource<K, R> {
	return flatMap { Loaded(fn(it)) }
}

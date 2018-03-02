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

import io.github.snarks.reactivestore.overhaul.cache.CacheState
import io.github.snarks.reactivestore.overhaul.utils.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class SimpleStore<K, V>(
		val loaderFactory: (K) -> Loader<V> = { Loader.empty<V>() },
		val scheduler: Scheduler = Schedulers.single()) : Store<K, V> {

	private val map = mutableMapOf<K, CacheState.Holder<V>>()
	private val relay = PublishSubject.create<KeyStatus<K, V>>()

	override fun update(key: K, updater: Updater<V>) {
		State(key).update(updater)
	}

	override fun updates(): Observable<KeyStatus<K, V>> = relay

	override fun current(): Single<Map<K, Status<V>>> {
		return makeSingle { map.asSequence().associate { (k, v) -> k to v.status } }
	}

	override fun currentFor(key: K): Single<Status<V>> = makeSingle { map[key]?.status ?: Empty() }

	override fun currentKeys(): Single<Set<K>> = makeSingle { map.keys.toSet() }

	override fun currentStatuses(): Single<Collection<Status<V>>> = makeSingle { map.values.map { it.status } }

	private inner class State(val key: K) : CacheState<V>() {
		override val scheduler: Scheduler get() = this@SimpleStore.scheduler
		override val loader: Loader<V> = loaderFactory(key)

		override var current: Holder<V>
			get() = map[key] ?: Holder()
			set(value) {
				val status = value.status
				if (status == Empty) {
					map.remove(key)
				} else {
					map[key] = value
				}
				relay.onNext(KeyPair(key, status))
			}
	}

	private inline fun <T> makeSingle(crossinline supplier: () -> T): Single<T> = Single
			.fromCallable { supplier() }
			.subscribeOn(scheduler)
}

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
package io.github.snarks.reactivestore.stores

import io.github.snarks.reactivestore.utils.Empty
import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.internal.Content
import io.github.snarks.reactivestore.utils.internal.applyUpdate
import io.github.snarks.reactivestore.utils.internal.optObserveOn
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


/**
 * A basic implementation of [ReactiveStore]
 *
 * This implementation relies on [updateScheduler] for its thread safety. So single thread schedulers are preferred if
 * this store will be used in a multithreaded / concurrent environment. Parallel schedulers should be avoided
 * altogether. _(The default value,_ `Schedulers.single()`, _should be adequate in most cases.)_
 *
 * The [publishScheduler] is an optional scheduler which will be used to emit items from the [observe], [contents] and
 * [keys] methods.
 */
class SimpleStore<K : Any, V : Any>(
		private val loaderSupplier: (key: K) -> SingleSource<V>,
		private val updateScheduler: Scheduler = Schedulers.single(),
		private val publishScheduler: Scheduler? = null) : ReactiveStore<K, V> {

	private val map = mutableMapOf<K, Content<V>>()
	private val relay = PublishSubject.create<Pair<K, Content<V>>>()


	override fun update(key: K, updater: Updater<V>) {
		applyUpdate(updater, loader(key), updateScheduler, { current(key) }, { emit(it, key) })
	}

	override fun observe(key: K): Observable<LoadStatus<V>> {
		val current = Observable.fromCallable { current(key) }.subscribeOn(updateScheduler)

		return current
				.concatWith(relay(key))
				.map { it.status }
				.optObserveOn(publishScheduler)
	}

	override fun observeUpdates(): Observable<Pair<K, LoadStatus<V>>> {
		return relay.map { (k, v) -> k to v.status }
				.optObserveOn(publishScheduler)
	}

	override fun currentStatus(): Observable<Pair<K, LoadStatus<V>>> {
		val entries = Observable.defer { map.entries.toObservable() }.subscribeOn(updateScheduler)

		return entries
				.map { (k, v) -> k to v.status }
				.optObserveOn(publishScheduler)
	}

	override fun keys(): Observable<K> {
		val keys = Observable.defer { Observable.fromIterable(map.keys) }.subscribeOn(updateScheduler)

		return keys.optObserveOn(publishScheduler)
	}

	// -------------------------------------------------------------------------------------------------------------- //

	private fun relay(key: K) = relay.filter { (k, _) -> k == key }.map { (_, v) -> v }
	private fun loader(key: K) = Single.wrap(loaderSupplier(key))
	private fun current(key: K) = map.getOrElse(key) { Content(Empty) }

	private fun emit(next: Content<V>, key: K) {
		if (next.status == Empty) {
			map.remove(key)
		} else {
			map[key] = next
		}
		relay.onNext(key to next)
	}
}

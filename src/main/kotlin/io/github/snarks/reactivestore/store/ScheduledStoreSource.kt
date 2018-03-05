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

import io.github.snarks.reactivestore.utils.KeyStatus
import io.github.snarks.reactivestore.utils.Status
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

class ScheduledStoreSource<K : Any, V : Any>(
		private val original: StoreSource<K, V>,
		private val observeOn: Scheduler) : StoreSource<K, V> {

	override fun current(): Single<Map<K, Status<V>>> = original.current().applyScheduler()

	override fun updates(): Observable<KeyStatus<K, V>> = original.updates().applyScheduler()

	override fun currentFor(key: K): Single<Status<V>> = original.currentFor(key).applyScheduler()

	override fun updatesFor(key: K): Observable<Status<V>> = original.updatesFor(key).applyScheduler()

	override fun observeFor(key: K): Observable<Status<V>> = original.observeFor(key).applyScheduler()

	override fun currentKeys(): Single<Set<K>> = original.currentKeys().applyScheduler()

	override fun currentStatuses(): Single<Collection<Status<V>>> = original.currentStatuses().applyScheduler()

	private fun <T> Single<T>.applyScheduler(): Single<T> = observeOn(observeOn)

	private fun <T> Observable<T>.applyScheduler() = observeOn(observeOn)
}

fun <K : Any, V : Any> StoreSource<K, V>.observeOn(scheduler: Scheduler): StoreSource<K, V> {
	return ScheduledStoreSource(this, scheduler)
}

fun <K : Any, V : Any> Store<K, V>.observeOn(scheduler: Scheduler): Store<K, V> {
	return object : Store<K, V>,
			StoreSource<K, V> by ScheduledStoreSource(this, scheduler),
			StoreSink<K, V> by this {}
}

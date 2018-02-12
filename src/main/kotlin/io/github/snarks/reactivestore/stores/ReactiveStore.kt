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
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable


/**
 * An observable container that loads and stores multiple values
 *
 * Each `value` put in the store has an associated `key` which can be used to retrieve said value.
 *
 * @param K The type of the key used to retrieve the contents of this store.
 * @param V The type of the contents to be stored.
 */
interface ReactiveStore<K : Any, V : Any> {

	/**
	 * Updates the content associated to the given [key] with the given [updater]
	 *
	 * @see Updaters
	 */
	fun update(key: K, updater: Updater<V>)

	/**
	 * Emits the current status and subsequent updates associated to the given [key]
	 *
	 * The first item emitted by the observable is the current status as of the time of its subscription.
	 *
	 * This observable is **unbounded**, and will not _normally_ emit a terminal signal.
	 */
	fun observe(key: K): Observable<LoadStatus<V>>

	/**
	 * Emits all content updates on this store
	 *
	 * Unlike [observe], this will only emit content updates that happen after subscription.
	 *
	 * This observable is **unbounded**, and will not _normally_ emit a terminal signal.
	 */
	fun observeUpdates(): Observable<Pair<K, LoadStatus<V>>>

	/**
	 * Emits the current status of all present content
	 *
	 * The [Empty] status will be skipped altogether. If this store has nothing, it will return an empty observable.
	 *
	 * The emitted values will be the ones that exist as of the time of subscription in no particular order.
	 *
	 * This observable is bounded and will not _normally_ emit an `onError` signal.
	 */
	fun currentStatus(): Observable<Pair<K, LoadStatus<V>>>

	/**
	 * Emits all the keys with associated content on this store
	 *
	 * The emitted values will be the ones that exist as of the time of subscription in no particular order.
	 *
	 * This observable is bounded and will not _normally_ emit an `onError` signal.
	 */
	fun keys(): Observable<K>
}

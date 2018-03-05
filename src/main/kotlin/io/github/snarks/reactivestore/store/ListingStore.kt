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

import io.github.snarks.reactivestore.utils.Keyed
import io.github.snarks.reactivestore.utils.Loaded
import io.github.snarks.reactivestore.utils.Loader
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers

class ListingStore<K, L, V : Keyed<K>> private constructor(
		val mainStore: StoreSink<K, V>,
		private val internal: SimpleStore<L, Collection<V>>) : Store<L, Collection<V>> by internal {

	constructor(
			mainStore: StoreSink<K, V>,
			loaderFactory: (L) -> Loader<Collection<V>> = { Loader.empty() },
			scheduler: Scheduler = Schedulers.single()) :
			this(mainStore, SimpleStore(loaderFactory, scheduler))

	init {
		internal.updates()
				.map { (_, v) -> v }
				.ofType<Loaded<Collection<V>>>()
				.map { it.value }
				.subscribe { for (v in it) mainStore.put(v) }
	}

	val loaderFactory: (L) -> Loader<Collection<V>> = internal.loaderFactory
	val scheduler: Scheduler get() = internal.scheduler
}

fun <K, L, V : Keyed<K>> StoreSink<K, V>.listing(
		loaderFactory: (L) -> Loader<Collection<V>>,
		scheduler: Scheduler): Store<L, Collection<V>> {
	return ListingStore(this, loaderFactory, scheduler)
}

fun <K, L, V : Keyed<K>> SimpleStore<K, V>.listing(
		loaderFactory: (L) -> Loader<Collection<V>>,
		scheduler: Scheduler = this.scheduler): Store<L, Collection<V>> {
	return ListingStore(this, loaderFactory, scheduler)
}

// LATER listing for keypairs + sink.mapUpdater

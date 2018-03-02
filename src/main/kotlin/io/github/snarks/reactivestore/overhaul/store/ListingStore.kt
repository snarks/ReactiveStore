package io.github.snarks.reactivestore.overhaul.store

import io.github.snarks.reactivestore.overhaul.utils.Keyed
import io.github.snarks.reactivestore.overhaul.utils.Loaded
import io.github.snarks.reactivestore.overhaul.utils.Loader
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

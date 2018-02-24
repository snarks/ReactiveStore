package io.github.snarks.reactivestore.overhaul.store

import io.github.snarks.reactivestore.overhaul.utils.KeyStatus
import io.github.snarks.reactivestore.overhaul.utils.Status
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

class ScheduledStoreSource<K, V>(
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

fun <K, V> StoreSource<K, V>.observeOn(scheduler: Scheduler): StoreSource<K, V> {
	return ScheduledStoreSource(this, scheduler)
}

fun <K, V> Store<K, V>.observeOn(scheduler: Scheduler): Store<K, V> {
	val scheduled: StoreSource<K, V> = (this as StoreSource<K, V>).observeOn(scheduler)

	return object : Store<K, V>,
			StoreSource<K, V> by scheduled,
			StoreSink<K, V> by this {}
}

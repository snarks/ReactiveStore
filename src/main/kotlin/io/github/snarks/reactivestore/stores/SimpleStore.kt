package io.github.snarks.reactivestore.stores

import io.github.snarks.reactivestore.utils.Empty
import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Loaded
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.internal.Content
import io.github.snarks.reactivestore.utils.internal.applyUpdate
import io.github.snarks.reactivestore.utils.internal.optObserveOn
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


/**
 * A basic implementation of [ReactiveStore]
 *
 * @param loaderSupplier   The supplier that will provide the default loader for a given key
 * @param updateScheduler  A **single-threaded** scheduler where the update operations will be executed on
 * @param publishScheduler An optional scheduler where the contents of this cache will be emitted
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

	override fun contents(): Observable<V> {
		val contents = Observable.defer { map.values.toObservable() }.subscribeOn(updateScheduler)

		return contents
				.map { it.status.lastStableStatus }
				.ofType<Loaded<V>>()
				.map { it.value }
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

package io.github.snarks.reactivestore.caches

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
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


/**
 * A basic implementation of [ReactiveCache]
 *
 * @param loader           The default loader used to retrieve this cache's value
 * @param updateScheduler  A **single-threaded** scheduler where the update operations will be executed on
 * @param publishScheduler An optional scheduler where the contents of this cache will be emitted
 */
class SimpleCache<T : Any>(
		loader: SingleSource<out T>,
		private val updateScheduler: Scheduler = Schedulers.single(),
		private val publishScheduler: Scheduler? = null) : ReactiveCache<T> {

	private val loader = Single.wrap(loader)
	private val relay = BehaviorSubject.createDefault<Content<T>>(Content(Empty))


	override fun update(updater: Updater<T>) {
		applyUpdate(updater, loader, updateScheduler, relay::getValue, relay::onNext)
	}

	override fun observe(): Observable<LoadStatus<T>> = relay.map { it.status }.optObserveOn(publishScheduler)
}

package io.github.snarks.reactivestore.overhaul.cache

import io.github.snarks.reactivestore.overhaul.utils.Loader
import io.github.snarks.reactivestore.overhaul.utils.Status
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class SimpleCache<T>(
		public override val loader: Loader<T> = Loader.empty(),
		public override val scheduler: Scheduler = Schedulers.single()) : CacheState<T>(), Cache<T> {

	private val relay = PublishSubject.create<Status<T>>()
	override var current: Holder<T> = Holder()
		set(value) {
			field = value
			relay.onNext(value.status)
		}

	override fun current(): Single<Status<T>> = Single.fromCallable { current.status }.subscribeOn(scheduler)

	override fun updates(): Observable<Status<T>> = relay
}

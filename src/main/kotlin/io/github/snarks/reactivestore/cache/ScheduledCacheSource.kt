package io.github.snarks.reactivestore.cache

import io.github.snarks.reactivestore.utils.Status
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

class ScheduledCacheSource<T>(
		private val original: CacheSource<T>,
		private val observeOn: Scheduler) : CacheSource<T> {

	override fun current(): Single<Status<T>> = original.current().applyScheduler()

	override fun updates(): Observable<Status<T>> = original.updates().applyScheduler()

	override fun observe(): Observable<Status<T>> = original.observe().applyScheduler()

	private fun <T> Single<T>.applyScheduler(): Single<T> = observeOn(observeOn)

	private fun <T> Observable<T>.applyScheduler() = observeOn(observeOn)
}

fun <T> CacheSource<T>.observeOn(scheduler: Scheduler): CacheSource<T> {
	return ScheduledCacheSource(this, scheduler)
}

fun <T> Cache<T>.observeOn(scheduler: Scheduler): Cache<T> {
	return object : Cache<T>,
			CacheSource<T> by ScheduledCacheSource(this, scheduler),
			CacheSink<T> by this {}
}

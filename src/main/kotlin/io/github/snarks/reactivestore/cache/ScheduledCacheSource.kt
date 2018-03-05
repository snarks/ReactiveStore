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
package io.github.snarks.reactivestore.cache

import io.github.snarks.reactivestore.utils.Status
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

class ScheduledCacheSource<T : Any>(
		private val original: CacheSource<T>,
		private val observeOn: Scheduler) : CacheSource<T> {

	override fun current(): Single<Status<T>> = original.current().applyScheduler()

	override fun updates(): Observable<Status<T>> = original.updates().applyScheduler()

	override fun observe(): Observable<Status<T>> = original.observe().applyScheduler()

	private fun <T> Single<T>.applyScheduler(): Single<T> = observeOn(observeOn)

	private fun <T> Observable<T>.applyScheduler() = observeOn(observeOn)
}

// ------------------------------------------------------------------------------------------------------------------ //
// Extensions

fun <T : Any> CacheSource<T>.observeOn(scheduler: Scheduler): CacheSource<T> {
	return ScheduledCacheSource(this, scheduler)
}

fun <T : Any> Cache<T>.observeOn(scheduler: Scheduler): Cache<T> {
	return Cache.from(this, ScheduledCacheSource(this, scheduler))
}

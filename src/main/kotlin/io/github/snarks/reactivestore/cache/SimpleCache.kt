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

import io.github.snarks.reactivestore.utils.Loader
import io.github.snarks.reactivestore.utils.Status
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class SimpleCache<T : Any>(
		public override val loader: Loader<T> = Loader.empty(),
		public override val scheduler: Scheduler = Schedulers.single()) : AbstractCacheSink<T>(), Cache<T> {

	private val relay = PublishSubject.create<Status<T>>()
	override var current: Holder<T> = Holder()
		set(value) {
			field = value
			relay.onNext(value.status)
		}

	override fun current(): Single<Status<T>> = Single.fromCallable { current.status }.subscribeOn(scheduler)

	override fun updates(): Observable<Status<T>> = relay
}

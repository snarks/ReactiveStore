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
 * This implementation relies on [updateScheduler] for its thread safety. So single thread schedulers are preferred if
 * this cache will be used in a multithreaded / concurrent environment. Parallel schedulers should be avoided
 * altogether. _(The default value,_ `Schedulers.single()`, _should be adequate in most cases.)_
 *
 * The [publishScheduler] is an optional scheduler which will be used to emit items from the [observe] method.
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

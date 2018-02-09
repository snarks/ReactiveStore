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
package io.github.snarks.reactivestore.utils.internal

import io.github.snarks.reactivestore.utils.Updater
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

internal fun <T : Any> applyUpdate(
		updater: Updater<T>, loader: Single<out T>, scheduler: Scheduler,
		getPrev: () -> Content<T>, emitNext: (Content<T>) -> Unit) {

	scheduler.scheduleDirect {
		val prev = getPrev()
		val prevStatus = prev.status
		val nextStatus = updater(prevStatus, loader)

		if (prevStatus !== nextStatus) {
			prev.onRemove()

			val next = Content(nextStatus)
			emitNext(next)

			next.onStart { u -> applyUpdate(u, loader, scheduler, getPrev, emitNext) }
		}
	}
}

internal fun <T> Observable<T>.optObserveOn(scheduler: Scheduler?): Observable<T> =
		if (scheduler == null) this else observeOn(scheduler)

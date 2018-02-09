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

import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable
import io.reactivex.SingleSource


/**
 * Convenience method to call [ReactiveCache.update] and then [ReactiveCache.observe]
 *
 * The default [updater] is [Updaters.auto].
 */
fun <T : Any> ReactiveCache<T>.load(updater: Updater<T> = Updaters.auto()): Observable<LoadStatus<T>> {
	update(updater)
	return observe()
}

/** Alias of `update(Updaters.cancelLoading())` */
fun <T : Any> ReactiveCache<T>.cancelLoading() {
	update(Updaters.cancelLoading())
}

/** Alias of `update(Updaters.resetFailure())` */
fun <T : Any> ReactiveCache<T>.resetFailure() {
	update(Updaters.resetFailure())
}

/** Alias of `update(Updaters.reload())` */
fun <T : Any> ReactiveCache<T>.reload(customLoader: SingleSource<T>? = null) {
	update(Updaters.reload(customLoader))
}

/** Alias of `update(Updaters.set())` */
fun <T : Any> ReactiveCache<T>.set(value: T?) {
	update(Updaters.set(value))
}

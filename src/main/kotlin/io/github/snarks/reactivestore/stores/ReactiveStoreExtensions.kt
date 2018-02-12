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
package io.github.snarks.reactivestore.stores

import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Loaded
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable
import io.reactivex.SingleSource
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType


/**
 * Convenience method to call [ReactiveStore.update] and then [ReactiveStore.observe]
 *
 * The default [updater] is [Updaters.auto].
 */
fun <K : Any, V : Any> ReactiveStore<K, V>.load(key: K, updater: Updater<V> = Updaters.auto()): Observable<LoadStatus<V>> {
	update(key, updater)
	return observe(key)
}

/**
 * Convenience method to call [ReactiveStore.load] and then [Observable.subscribe]
 *
 * The default [updater] is [Updaters.auto].
 */
inline fun <K : Any, V : Any> ReactiveStore<K, V>.loadThen(
		key: K,
		noinline updater: Updater<V> = Updaters.auto(),
		crossinline callback: (LoadStatus<V>) -> Unit): Disposable = load(key, updater).subscribe { callback(it) }

/**
 * Emits all the currently loaded values of this store
 *
 * The emitted values will be the ones that exist as of the time of subscription in no particular order.
 *
 * This observable is bounded and will not _normally_ emit an `onError` signal.
 */
fun <K : Any, V : Any> ReactiveStore<K, V>.contents(): Observable<V> {
	return currentStatus()
			.map { (_, v) -> v.lastStableStatus }
			.ofType<Loaded<V>>()
			.map { it.value }
}

/** Alias of `update(Updaters.cancelLoading())` */
fun <K : Any, V : Any> ReactiveStore<K, V>.cancelLoading(key: K) {
	update(key, Updaters.cancelLoading())
}

/** Alias of `update(Updaters.resetFailure())` */
fun <K : Any, V : Any> ReactiveStore<K, V>.resetFailure(key: K) {
	update(key, Updaters.resetFailure())
}

/** Alias of `update(Updaters.reload())` */
fun <K : Any, V : Any> ReactiveStore<K, V>.reload(key: K, customLoader: SingleSource<V>? = null) {
	update(key, Updaters.reload(customLoader))
}

/** Alias of `update(Updaters.set())` */
operator fun <K : Any, V : Any> ReactiveStore<K, V>.set(key: K, value: V?) {
	update(key, Updaters.set(value))
}

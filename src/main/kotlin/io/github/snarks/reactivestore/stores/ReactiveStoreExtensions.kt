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
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable
import io.reactivex.SingleSource


/**
 * Convenience method to call [ReactiveStore.update] and then [ReactiveStore.observe]
 *
 * The default [updater] is [Updaters.auto].
 */
fun <K : Any, V : Any> ReactiveStore<K, V>.load(key: K, updater: Updater<V> = Updaters.auto()): Observable<LoadStatus<V>> {
	update(key, updater)
	return observe(key)
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

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

import io.github.snarks.reactivestore.utils.Loaded
import io.github.snarks.reactivestore.utils.Status
import io.github.snarks.reactivestore.utils.contentValue
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

interface CacheSource<T> {

	fun current(): Single<Status<T>>

	fun updates(): Observable<Status<T>>

	fun observe(): Observable<Status<T>> = updates().startWith(current().toObservable())
}

fun <T> CacheSource<T>.currentValue(): Maybe<T> = current().contentValue()

// ------------------------------------------------------------------------------------------------------------------ //

inline fun <T, R> CacheSource<T>.transform(crossinline fn: (Status<T>) -> Status<R>): CacheSource<R> {
	val orig = this

	return object : CacheSource<R> {
		override fun current(): Single<Status<R>> = orig.current().map { it.transform { fn(it) } }
		override fun updates(): Observable<Status<R>> = orig.updates().map { it.transform { fn(it) } }
		override fun observe(): Observable<Status<R>> = orig.observe().map { it.transform { fn(it) } }
	}
}

fun <T, R> CacheSource<T>.flatMap(fn: (T) -> Status<R>): CacheSource<R> = transform { it.flatMap(fn) }

fun <T, R> CacheSource<T>.map(fn: (T) -> R): CacheSource<R> = flatMap { Loaded(fn(it)) }

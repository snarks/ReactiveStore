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
package io.github.snarks.reactivestore.utils

import io.reactivex.Maybe
import io.reactivex.MaybeSource
import io.reactivex.Single

typealias Loader<T> = Maybe<T>

typealias LoadSource<T> = MaybeSource<T>

fun <T : Any> Loader<T>.asFutureChange(): Single<ImmediateChange<T>> = map<ImmediateChange<T>> { SetValue(it) }
		.toSingle(SetValue(null))
		.onErrorReturn { Fail(it) }

fun <T : Any> Loader<T>.asFutureUpdater(): Single<Updater<T>> = asFutureChange().map { Updater.change(it) }

inline fun <T : Any> Loader<T>.asFutureUpdater(crossinline condition: (Status<*>) -> Boolean): Single<Updater<T>> =
		asFutureChange().map { Updater.changeIf(it, condition) }

fun <T : Any> LoadSource<T>.toLoader(): Loader<T> = Maybe.wrap(this)

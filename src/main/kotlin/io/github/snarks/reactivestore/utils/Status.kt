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
import io.reactivex.Single

sealed class Status<out T : Any> {
	abstract val lastContent: ContentStatus<T>

	abstract fun <R : Any> transform(fn: (ResultStatus<T>) -> Status<R>): Status<R>

	abstract fun <R : Any> flatMap(fn: (T) -> Status<R>): Status<R>

	fun <R : Any> map(fn: (T) -> R): Status<R> = flatMap { Loaded(fn(it)) }
}

// ------------------------------------------------------------------------------------------------------------------ //

sealed class ResultStatus<out T : Any> : Status<T>() {
	override fun <R : Any> transform(fn: (ResultStatus<T>) -> Status<R>): Status<R> = fn(this)
}

sealed class ContentStatus<out T : Any> : ResultStatus<T>() {
	override val lastContent: ContentStatus<T> get() = this
}

// ------------------------------------------------------------------------------------------------------------------ //

object Empty : ContentStatus<Nothing>() {
	override fun toString(): String = "Empty"

	override fun <R : Any> flatMap(fn: (Nothing) -> Status<R>): Status<R> = Empty
}

data class Loaded<out T : Any>(val value: T) : ContentStatus<T>() {
	override fun <R : Any> flatMap(fn: (T) -> Status<R>): Status<R> = fn(value)
}

data class Failed<out T : Any>(val error: Throwable, override val lastContent: ContentStatus<T>) : ResultStatus<T>() {
	constructor(error: Throwable, prev: Status<T>) : this(error, prev.lastContent)

	override fun <R : Any> flatMap(fn: (T) -> Status<R>): Status<R> = Failed(error, lastContent.flatMap(fn))
}

data class Loading<out T : Any>(override val lastContent: ContentStatus<T>) : Status<T>() {

	constructor(prev: Status<T>) : this(prev.lastContent)

	override fun <R : Any> transform(fn: (ResultStatus<T>) -> Status<R>): Status<R> {
		return Loading(lastContent.transform(fn))
	}

	override fun <R : Any> flatMap(fn: (T) -> Status<R>): Status<R> = transform { it.flatMap(fn) }
}

// ------------------------------------------------------------------------------------------------------------------ //

fun <T : Any> Single<Status<T>>.contentValue(): Maybe<T> = flatMapMaybe {
	val content = it.lastContent
	when (content) {
		Empty -> Maybe.empty()
		is Loaded -> Maybe.just(content.value)
	}
}

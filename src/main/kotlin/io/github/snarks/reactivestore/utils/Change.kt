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

import io.reactivex.Single

sealed class Change<T> {
	abstract fun transform(fn: (ImmediateChange<T>) -> Change<T>): Change<T>
}

sealed class ImmediateChange<T> : Change<T>() {
	override fun transform(fn: (ImmediateChange<T>) -> Change<T>): Change<T> = fn(this)
}

sealed class ImmediateAction<T> : ImmediateChange<T>() {
	abstract fun toStatus(prev: Status<T>): Status<T>
}

// ------------------------------------------------------------------------------------------------------------------ //

object NoChange : ImmediateChange<Nothing>() {
	operator fun <T> invoke(): ImmediateChange<T> = @Suppress("UNCHECKED_CAST") (this as ImmediateChange<T>)

	override fun toString(): String = "NoChange"
}

object ClearValue : ImmediateAction<Nothing>() {
	operator fun <T> invoke(): ImmediateChange<T> = @Suppress("UNCHECKED_CAST") (this as ImmediateChange<T>)

	override fun toString(): String = "Clear"

	override fun toStatus(prev: Status<Nothing>): Status<Nothing> = Empty
}

data class SetValue<T>(val newValue: T) : ImmediateAction<T>() {
	override fun toStatus(prev: Status<T>): Status<T> = Loaded(newValue)
}

data class Fail<T>(val error: Throwable) : ImmediateAction<T>() {
	override fun toStatus(prev: Status<T>): Status<T> = Failed(error, prev)
}

data class Defer<T>(val future: Single<Updater<T>>, val ignoreIfUpdated: Boolean = true) : Change<T>() {

	override fun transform(fn: (ImmediateChange<T>) -> Change<T>): Change<T> {
		return Defer(future.map { Updater<T> { c, d -> it.applyUpdate(c, d).transform(fn) } }, ignoreIfUpdated)
	}
}

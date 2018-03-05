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

sealed class Change<out T : Any>

sealed class ImmediateChange<out T : Any> : Change<T>()

// ------------------------------------------------------------------------------------------------------------------ //

object NoChange : ImmediateChange<Nothing>() {
	override fun toString(): String = "NoChange"
}

data class SetValue<out T : Any>(val newValue: T?) : ImmediateChange<T>()

data class Fail<out T : Any>(val error: Throwable) : ImmediateChange<T>()

object Revert : ImmediateChange<Nothing>() {
	override fun toString(): String = "Revert"
}

data class Defer<out T : Any>(val future: Single<out Updater<T>>?, val ignoreIfUpdated: Boolean = true) : Change<T>()

// ------------------------------------------------------------------------------------------------------------------ //

fun <T : Any> Change<T>.nextStatus(prev: Status<T>): Status<T>? {
	return when (this) {
		NoChange -> null
		is SetValue -> if (newValue == null) Empty else Loaded(newValue)
		is Fail -> Failed(error, prev)
		Revert -> when (prev) {
			is Loading -> prev.lastContent
			is Failed -> prev.lastContent
			else -> null
		}
		is Defer -> Loading(prev)
	}
}

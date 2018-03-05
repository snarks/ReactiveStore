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

interface Updater<out T : Any> {

	fun applyUpdate(current: Status<*>): Change<T>

	companion object {
		// ---------------------------------------------------------------------------------------------------------- //
		// Lambda Constructor

		inline operator fun <T : Any> invoke(crossinline applyUpdate: (Status<*>) -> Change<T>): Updater<T> {
			return object : Updater<T> {
				override fun applyUpdate(current: Status<*>): Change<T> = applyUpdate(current)
			}
		}

		// ---------------------------------------------------------------------------------------------------------- //
		// Direct Change

		fun <T : Any> change(change: Change<T>): Updater<T> = Updater { change }

		fun <T : Any> clear(): Updater<T> = set(null)

		fun <T : Any> set(newValue: T?): Updater<T> = change(SetValue(newValue))

		fun <T : Any> fail(error: Throwable): Updater<T> = change(Fail(error))

		fun <T : Any> revert(): Updater<T> = change(Revert)

		inline fun <T : Any> changeIf(
				change: Change<T>,
				crossinline condition: (current: Status<*>) -> Boolean): Updater<T> {
			return Updater { current -> if (condition(current)) change else NoChange }
		}

		// ---------------------------------------------------------------------------------------------------------- //
		// Deferred Updates

		fun <T : Any> defer(futureUpdater: Single<Updater<T>>, ignoreIfUpdated: Boolean = true): Updater<T> {
			return change(Defer(futureUpdater, ignoreIfUpdated))
		}

		inline fun <T : Any> fromLoader(
				loader: Loader<T>,
				ignoreIfUpdated: Boolean = true,
				crossinline condition: (current: Status<*>) -> Boolean = { true }): Updater<T> {

			return defer(loader.asFutureUpdater(condition), ignoreIfUpdated)
		}

		inline fun <T : Any> loadIf(
				customLoader: Loader<T>? = null,
				ignoreIfUpdated: Boolean = true,
				crossinline condition: (current: Status<*>) -> Boolean): Updater<T> {

			val futureUpdater = customLoader?.asFutureUpdater(condition)
			val change = Defer(futureUpdater, ignoreIfUpdated)

			return changeIf(change, condition)
		}

		fun <T : Any> autoLoad(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true): Updater<T> {
			return loadIf(customLoader, ignoreIfUpdated) { it == Empty || it is Failed }
		}

		fun <T : Any> reload(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true): Updater<T> {
			return loadIf(customLoader, ignoreIfUpdated) { it !is Loading }
		}
	}
}

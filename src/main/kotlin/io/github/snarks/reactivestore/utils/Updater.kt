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

typealias UpdateCondition<T> = (current: Status<T>, default: Loader<T>) -> Boolean

interface Updater<T> {

	fun applyUpdate(current: Status<T>, default: Loader<T>): Change<T>

	companion object {
		// ---------------------------------------------------------------------------------------------------------- //
		// Lambda Constructor

		inline operator fun <T> invoke(crossinline applyUpdate: (Status<T>, Loader<T>) -> Change<T>): Updater<T> {
			return object : Updater<T> {
				override fun applyUpdate(current: Status<T>, default: Loader<T>) = applyUpdate(current, default)
			}
		}

		// ---------------------------------------------------------------------------------------------------------- //
		// Direct Change

		fun <T> change(change: Change<T>): Updater<T> = Updater { _, _ -> change }

		fun <T> clear(): Updater<T> = change(ClearValue())

		fun <T> set(newValue: T): Updater<T> = change(SetValue(newValue))

		fun <T> fail(error: Throwable): Updater<T> = change(Fail(error))

		inline fun <T> changeIf(change: Change<T>, crossinline condition: UpdateCondition<T>): Updater<T> {
			return Updater { current, default -> if (condition(current, default)) change else NoChange() }
		}

		// ---------------------------------------------------------------------------------------------------------- //
		// Deferred Updates

		fun <T> defer(futureUpdater: Single<Updater<T>>, ignoreIfUpdated: Boolean = true): Updater<T> {
			return change(Defer(futureUpdater, ignoreIfUpdated))
		}

		inline fun <T> fromLoader(
				loader: Loader<T>,
				ignoreIfUpdated: Boolean = true,
				crossinline condition: UpdateCondition<T> = { _, _ -> true }): Updater<T> {

			return defer(loader.asFutureUpdater(condition), ignoreIfUpdated)
		}

		inline fun <T> loadIf(
				customLoader: Loader<T>? = null,
				ignoreIfUpdated: Boolean = true,
				crossinline condition: UpdateCondition<T>): Updater<T> {

			return Updater { _, default ->
				val future = (customLoader ?: default).asFutureUpdater(condition)
				Defer(future, ignoreIfUpdated)
			}
		}

		fun <T> autoLoad(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true): Updater<T> {
			return loadIf(customLoader, ignoreIfUpdated) { c, _ -> c == Empty || c is Failed }
		}

		fun <T> reload(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true): Updater<T> {
			return loadIf(customLoader, ignoreIfUpdated) { current, _ -> current !is Loading }
		}

		// ---------------------------------------------------------------------------------------------------------- //
		// Status Reversion

		inline fun <T> revertIf(crossinline condition: UpdateCondition<T>): Updater<T> {
			return Updater { current, default ->
				if (condition(current, default)) {
					val lastContent = current.lastContent
					when (lastContent) {
						Empty -> ClearValue()
						is Loaded -> SetValue(lastContent.value)
					}
				} else NoChange()
			}
		}

		fun <T> cancelLoad(): Updater<T> = revertIf { current, _ -> current is Loading }

		fun <T> resetError(): Updater<T> = revertIf { current, _ -> current is Failed }
	}
}

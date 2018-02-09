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
import io.reactivex.SingleSource

/**
 * Provides commonly used [Updater] implementations
 */
object Updaters {

	/**
	 * Returns an [Updater] that will load a new value if it doesn't exist yet
	 *
	 * If the store is already loading, it won't do anything.
	 */
	@JvmOverloads
	@JvmStatic
	fun <T : Any> auto(customLoader: SingleSource<T>? = null): Updater<T> {
		return if (customLoader != null) {
			{ curr, _ -> if (curr is Failed || curr == Empty) Loading(curr, Single.wrap(customLoader)) else curr }
		} else {
			{ curr, loader -> if (curr is Failed || curr == Empty) Loading(curr, loader) else curr }
		}
	}

	/**
	 * Returns an [Updater] that will load a new value, replacing any existing value
	 *
	 * If the store is already loading, it won't do anything.
	 */
	@JvmOverloads
	@JvmStatic
	fun <T : Any> reload(customLoader: SingleSource<T>? = null): Updater<T> {
		return if (customLoader != null) {
			{ curr, _ -> curr as? Loading ?: Loading(curr, Single.wrap(customLoader)) }
		} else {
			{ curr, loader -> curr as? Loading ?: Loading(curr, loader) }
		}
	}

	/**
	 * Returns an [Updater] that will set the store to the given value
	 *
	 * If the [value] is `null`, it will set the store to empty
	 *
	 * Also, if the store is already loading, it will be cancelled.
	 */
	@JvmStatic
	fun <T : Any> set(value: T?): Updater<T> {
		return if (value == null) {
			{ _, _ -> Empty }
		} else {
			{ _, _ -> Loaded(value) }
		}
	}

	/**
	 * Returns an [Updater] that will cancel loading
	 *
	 * The store will be reverted to the previous load status
	 */
	@JvmStatic
	fun <T : Any> cancelLoading(): Updater<T> {
		return { curr, _ -> (curr as? Loading)?.lastStableStatus ?: curr }
	}

	/**
	 * Returns an [Updater] that will reset a failed status to the previous stable status
	 *
	 * The store will be reverted to the previous load status
	 */
	@JvmStatic
	fun <T : Any> resetFailure(): Updater<T> {
		return { curr, _ -> (curr as? Failed)?.lastStableStatus ?: curr }
	}

	/**
	 * Makes the given [updater] execute only if [expectedStatus] is the same as the current load status
	 *
	 * If the expected status isn't the same as the current status, it does nothing
	 */
	@JvmStatic
	inline fun <T : Any> compareAndUpdate(expectedStatus: LoadStatus<T>, crossinline updater: Updater<T>): Updater<T> {
		return { curr, loader -> if (curr === expectedStatus) updater(curr, loader) else curr }
	}
}

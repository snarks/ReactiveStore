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
package io.github.snarks.reactivestore.utils.internal

import io.github.snarks.reactivestore.utils.*
import io.reactivex.disposables.CompositeDisposable


internal interface Content<T : Any> {
	val status: LoadStatus<T>
	fun onRemove() {}
	fun onStart(sink: (Updater<T>) -> Unit) {}

	private class LoadingContent<T : Any>(override val status: Loading<T>) : Content<T> {
		private val disposables = CompositeDisposable()

		override fun onRemove() = disposables.dispose()

		override fun onStart(sink: (Updater<T>) -> Unit) {
			disposables.add(status.loader.subscribe(
					{ result -> sink(Updaters.compareAndUpdate(status) { _, _ -> Loaded(result) }) },
					{ error -> sink(Updaters.compareAndUpdate(status) { curr, _ -> Failed(curr, error) }) }))
		}
	}

	private class SimpleContent<T : Any>(override val status: LoadStatus<T>) : Content<T>

	companion object {
		private val empty = SimpleContent(Empty)

		operator fun <T : Any> invoke(status: LoadStatus<T>): Content<T> = when (status) {
			is Loading<T> -> LoadingContent(status)
			Empty -> @Suppress("UNCHECKED_CAST") (empty as Content<T>)
			else -> SimpleContent(status)
		}
	}
}

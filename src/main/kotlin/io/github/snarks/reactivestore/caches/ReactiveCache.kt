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
package io.github.snarks.reactivestore.caches

import io.github.snarks.reactivestore.utils.LoadStatus
import io.github.snarks.reactivestore.utils.Updater
import io.github.snarks.reactivestore.utils.Updaters
import io.reactivex.Observable


/**
 * An observable container that stores a single value
 */
interface ReactiveCache<T : Any> {

	/**
	 * Updates the contents of this cache with the given [updater]
	 *
	 * @see Updaters
	 */
	fun update(updater: Updater<T>)

	/**
	 * Emits the current status and subsequent updates of this cache
	 *
	 * The first item emitted by the observable is the current status as of the time of its subscription.
	 */
	fun observe(): Observable<LoadStatus<T>>
}

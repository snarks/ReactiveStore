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


/**
 * A function that transitions one [LoadStatus] to the next
 *
 * If the return value of this function is the same as `currentStatus`, it means no change should happen in the store.
 *
 * Updaters can be invoked multiple times and may be called on different threads with no particular order.
 * So preferably, updaters should be **stateless or immutable**.
 */
typealias Updater<T> = (currentStatus: LoadStatus<T>, defaultLoader: Single<out T>) -> LoadStatus<T>

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
package io.github.snarks.reactivestore.overhaul.utils

import io.reactivex.Observable

interface Keyed<out K> {
	val key: K
}

data class KeyPair<out K, out V>(override val key: K, override val value: V) : Keyed<K>, Map.Entry<K, V>

typealias KeyStatus<K, V> = Map.Entry<K, Status<V>>

fun <K, V> Observable<Map.Entry<K, V>>.withKey(key: K): Observable<V> {
	return filter { (k, _) -> k == key }.map { (_, v) -> v }
}

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

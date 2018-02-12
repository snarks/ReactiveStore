package io.github.snarks.reactivestore.utils

import io.github.snarks.reactivestore.caches.ReactiveCache
import io.github.snarks.reactivestore.stores.ReactiveStore


fun <T : Any> ReactiveCache<T>.printLog() {
	observe().subscribe(::println)
}

fun <K : Any, V : Any> ReactiveStore<K, V>.printLog() {
	observeUpdates().subscribe { (k, v) -> println("$k >> $v") }
}

inline fun <T : Any> ReactiveCache<T>.assertContent(vararg expected: LoadStatus<T>, block: ReactiveCache<T>.() -> Unit) {
	printLog()

	val test = observe().test()

	block()
	test.assertValuesOnly(*expected)
}

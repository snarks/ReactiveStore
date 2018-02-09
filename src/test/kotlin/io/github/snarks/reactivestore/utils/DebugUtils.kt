package io.github.snarks.reactivestore.utils

import io.github.snarks.reactivestore.caches.ReactiveCache
import io.github.snarks.reactivestore.stores.ReactiveStore
import io.reactivex.Observable
import io.reactivex.Single

fun <T> Single<T>.withPrettyToString() = DebugSingle(this)

fun <T : Any> ReactiveCache<T>.printLog() {
	observe().subscribe(::println)
}


fun <T : Any> ReactiveCache<T>.assertCurrentContent(expected: LoadStatus<T>) {
	observe().assertCurrentContent(expected)
}

fun <K : Any, V : Any> ReactiveStore<K, V>.assertCurrentContent(key: K, expected: LoadStatus<V>) {
	observe(key).assertCurrentContent(expected)
}

fun <T : Any> Observable<LoadStatus<T>>.assertCurrentContent(expected: LoadStatus<T>) {
	with(test()) {
		onComplete()
		assertResult(expected)
	}
}


inline fun <T : Any> ReactiveCache<T>.assertContent(vararg expected: LoadStatus<T>, block: ReactiveCache<T>.() -> Unit) {
	printLog()

	val test = observe().test()

	block()
	test.onComplete()
	test.assertResult(*expected)
}

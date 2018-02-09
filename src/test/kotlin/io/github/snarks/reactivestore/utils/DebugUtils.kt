package io.github.snarks.reactivestore.utils

import io.github.snarks.reactivestore.caches.ReactiveCache
import io.reactivex.Single

fun <T> Single<T>.withPrettyToString() = DebugSingle(this)

fun <T : Any> ReactiveCache<T>.printLog() {
	observe().subscribe(::println)
}

fun <T : Any> ReactiveCache<T>.assertCurrentContent(expected: LoadStatus<T>) {
	with(observe().test()) {
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

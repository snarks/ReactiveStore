package io.github.snarks.reactivestore.cache

import io.github.snarks.reactivestore.utils.Loaded
import io.github.snarks.reactivestore.utils.Status
import io.github.snarks.reactivestore.utils.contentValue
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

interface CacheSource<T> {

	fun current(): Single<Status<T>>

	fun updates(): Observable<Status<T>>

	fun observe(): Observable<Status<T>> = updates().startWith(current().toObservable())
}

fun <T> CacheSource<T>.currentValue(): Maybe<T> = current().contentValue()

// ------------------------------------------------------------------------------------------------------------------ //

inline fun <T, R> CacheSource<T>.transform(crossinline fn: (Status<T>) -> Status<R>): CacheSource<R> {
	val orig = this

	return object : CacheSource<R> {
		override fun current(): Single<Status<R>> = orig.current().map { it.transform { fn(it) } }
		override fun updates(): Observable<Status<R>> = orig.updates().map { it.transform { fn(it) } }
		override fun observe(): Observable<Status<R>> = orig.observe().map { it.transform { fn(it) } }
	}
}

fun <T, R> CacheSource<T>.flatMap(fn: (T) -> Status<R>): CacheSource<R> = transform { it.flatMap(fn) }

fun <T, R> CacheSource<T>.map(fn: (T) -> R): CacheSource<R> = flatMap { Loaded(fn(it)) }

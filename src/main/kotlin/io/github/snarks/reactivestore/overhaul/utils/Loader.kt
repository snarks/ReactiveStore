package io.github.snarks.reactivestore.overhaul.utils

import io.reactivex.Maybe
import io.reactivex.MaybeSource
import io.reactivex.Single

typealias Loader<T> = Maybe<T>

typealias LoadSource<T> = MaybeSource<T>

fun <T> Loader<T>.asFutureChange(): Single<ImmediateChange<T>> = map<ImmediateChange<T>> { SetValue(it) }
		.toSingle(ClearValue())
		.onErrorReturn { Fail(it) }

inline fun <T> Loader<T>.asFutureUpdater(crossinline condition: UpdateCondition<T>): Single<Updater<T>> =
		asFutureChange().map { Updater.changeIf(it, condition) }

fun <T> LoadSource<T>.toLoader(): Loader<T> = Maybe.wrap(this)

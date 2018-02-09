package io.github.snarks.reactivestore.utils

import io.reactivex.Single
import io.reactivex.SingleObserver

class DebugSingle<T>(val single: Single<T>) : Single<T>() {
	override fun subscribeActual(observer: SingleObserver<in T>) {
		single.subscribe(observer)
	}

	override fun toString() = "${single.javaClass.simpleName}@${single.hashCode()}"
}

package io.github.snarks.reactivestore.utils

import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit


class DebugLoader<T> private constructor(
		val value: T?,
		val error: Exception?,
		val seconds: Long = 0,
		val timer: TestScheduler? = null) : Single<T>() {

	constructor(value: T) : this(value, null, 0, null)

	fun advanceTime(seconds: Long) {
		timer!!.advanceTimeBy(seconds, TimeUnit.SECONDS)
	}

	override fun subscribeActual(observer: SingleObserver<in T>) {
		val s1 = when {
			value != null -> Single.just(value)
			error != null -> Single.error(error)
			else -> throw AssertionError()
		}

		val s2 = if (timer == null) s1 else s1.delay(seconds, TimeUnit.SECONDS, timer)

		s2.subscribe(observer)
	}

	override fun toString(): String {
		return when {
			value != null -> "DebugLoader(value=$value)"
			error != null -> "DebugLoader(error=$error)"
			else -> throw AssertionError()
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as DebugLoader<*>

		if (value != other.value) return false
		if (error != other.error) return false

		return true
	}

	override fun hashCode(): Int {
		var result = value?.hashCode() ?: 0
		result = 31 * result + (error?.hashCode() ?: 0)
		return result
	}


	companion object {
		fun <T> delayed(value: T, seconds: Long, timer: TestScheduler = TestScheduler()): DebugLoader<T> =
				DebugLoader(value, null, seconds, timer)

		fun <T> error(error: Exception): DebugLoader<T> =
				DebugLoader(null, error, 0, null)

		fun <T> error(error: Exception, seconds: Long, timer: TestScheduler = TestScheduler()): DebugLoader<T> =
				DebugLoader(null, error, seconds, timer)

		fun <T> error(msg: String): DebugLoader<T> =
				error(DebugException(msg))

		fun <T> error(msg: String, seconds: Long, timer: TestScheduler = TestScheduler()): DebugLoader<T> =
				error(DebugException(msg), seconds, timer)
	}
}

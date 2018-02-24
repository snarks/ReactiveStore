package io.github.snarks.reactivestore.overhaul.cache

import io.github.snarks.reactivestore.overhaul.utils.Loader
import io.github.snarks.reactivestore.overhaul.utils.Updater

interface CacheSink<T> {
	fun update(updater: Updater<T>)
}

fun <T> CacheSink<T>.clear() {
	update(Updater.clear())
}

fun <T> CacheSink<T>.set(newValue: T) {
	update(Updater.set(newValue))
}

fun <T> CacheSink<T>.fail(error: Throwable) {
	update(Updater.fail(error))
}

fun <T> CacheSink<T>.load(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true) {
	update(Updater.autoLoad(customLoader, ignoreIfUpdated))
}

fun <T> CacheSink<T>.reload(customLoader: Loader<T>? = null, ignoreIfUpdated: Boolean = true) {
	update(Updater.reload(customLoader, ignoreIfUpdated))
}

fun <T> CacheSink<T>.cancelLoad() {
	update(Updater.cancelLoad())
}

fun <T> CacheSink<T>.resetError() {
	update(Updater.resetError())
}

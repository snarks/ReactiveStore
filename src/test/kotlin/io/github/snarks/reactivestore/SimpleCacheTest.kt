package io.github.snarks.reactivestore

import io.github.snarks.reactivestore.caches.*
import io.github.snarks.reactivestore.utils.*
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS

class SimpleCacheTest {

	@Test
	fun update_withUpdaters() {
		val loader = Single.just("Hello").withPrettyToString()
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.assertContent(
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"),
				Loaded("Hi"),
				Loading(Loaded("Hi"), loader),
				Loaded("Hello"),
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"),
				Loading(Loaded("Hello"), loader),
				Loaded("Hello")) {

			update(Updaters.auto())
			update(Updaters.set("Hi"))
			update(Updaters.reload())
			update(Updaters.set(null))
			update(Updaters.auto())
			update(Updaters.auto())
			update(Updaters.reload())
			update(Updaters.cancelLoading())
		}
	}

	@Test
	fun update_withExtensions() {
		val loader = Single.just("Hello").withPrettyToString()
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.assertContent(
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"),
				Loaded("Hi"),
				Loading(Loaded("Hi"), loader),
				Loaded("Hello"),
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"),
				Loading(Loaded("Hello"), loader),
				Loaded("Hello")) {

			load()
			set("Hi")
			reload()
			set(null)
			load()
			load()
			reload()
			cancelLoading()
		}
	}

	@Test
	fun update_withErrors() {
		val exception = DebugException("load failed!")
		val loader = Single.error<String>(exception).withPrettyToString()
		val cache = SimpleCache<String>(loader, Schedulers.trampoline())

		cache.assertContent(
				Empty,
				Loading(Empty, loader),
				Failed(Empty, exception),
				Loaded("Hi"),
				Loading(Loaded("Hi"), loader),
				Failed(Loaded("Hi"), exception),
				Empty,
				Loading(Empty, loader),
				Failed(Empty, exception),
				Loading(Empty, loader),
				Failed(Empty, exception),
				Loading(Failed(Empty, exception), loader),
				Failed(Empty, exception),
				Empty) {

			update(Updaters.auto())
			set("Hi")
			reload()
			set(null)
			load()
			load()
			reload()
			resetFailure()
		}
	}

	@Test
	fun observe() {
		val loader = Single.just("Hello").withPrettyToString()
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		cache.assertCurrentContent(Empty)

		cache.set("Hi")
		cache.assertCurrentContent(Loaded("Hi"))

		cache.set(null)
		cache.assertCurrentContent(Empty)

		cache.load()
		cache.assertCurrentContent(Loaded("Hello"))

		cache.set("World")
		cache.assertCurrentContent(Loaded("World"))

		cache.load()
		cache.assertCurrentContent(Loaded("World"))
	}

	@Test
	fun load() {
		val timer = TestScheduler()
		val loader = Single.just("Hello").delay(10, SECONDS, timer).withPrettyToString()
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		val observeTest = cache.observe().test()

		val observable = cache.load()
		val loadTest = observable.test()

		timer.advanceTimeBy(20, SECONDS)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"))

		loadTest.assertValuesOnly(
				Loading(Empty, loader),
				Loaded("Hello"))
	}

	@Test
	fun loadThen() {
		val timer = TestScheduler()
		val loader = Single.just("Hello").delay(10, SECONDS, timer).withPrettyToString()
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		val observeTest = cache.observe().test()

		// This is done in a controlled test environment
		// DON'T mix mutation and concurrency like this in production code!
		val sink = mutableListOf<LoadStatus<String>>()
		cache.loadThen { sink.add(it) }

		timer.advanceTimeBy(20, SECONDS)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"))

		val loadTest = sink.toObservable().test()
		loadTest.assertResult(
				Loading(Empty, loader),
				Loaded("Hello"))
	}
}

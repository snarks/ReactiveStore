package io.github.snarks.reactivestore

import io.github.snarks.reactivestore.caches.*
import io.github.snarks.reactivestore.utils.*
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import java.util.concurrent.TimeUnit

class SimpleCacheTest {

	@Test
	fun update_withUpdaters() {
		val loader = DebugLoader("Hello")
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
		val loader = DebugLoader("Hello")
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
		val loader = DebugLoader.error<String>(exception)
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
		val loader = DebugLoader.delayed("Hello", 10)
		val cache = SimpleCache(loader, Schedulers.trampoline())
		val test = cache.observe().test()

		cache.printLog()

		test.assertValuesOnly(
				Empty)

		cache.set("Hi")
		test.assertValuesOnly(
				Empty, Loaded("Hi"))

		cache.set(null)
		test.assertValuesOnly(
				Empty, Loaded("Hi"), Empty)

		cache.load()
		test.assertValuesOnly(
				Empty, Loaded("Hi"), Empty, Loading(Empty, DebugLoader("Hello")))

		loader.advanceTime(11)
		test.assertValuesOnly(
				Empty, Loaded("Hi"), Empty, Loading(Empty, DebugLoader("Hello")), Loaded("Hello"))

		cache.set("World")
		test.assertValuesOnly(
				Empty, Loaded("Hi"), Empty, Loading(Empty, DebugLoader("Hello")), Loaded("Hello"), Loaded("World"))

		cache.load()
		test.assertValuesOnly(
				Empty, Loaded("Hi"), Empty, Loading(Empty, DebugLoader("Hello")), Loaded("Hello"), Loaded("World"))

		loader.advanceTime(11)
		test.assertValuesOnly(
				Empty, Loaded("Hi"), Empty, Loading(Empty, DebugLoader("Hello")), Loaded("Hello"), Loaded("World"))
	}

	@Test
	fun load() {
		val loader = DebugLoader.delayed("Hello", 10)
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		val observeTest = cache.observe().test()
		val loadTest = cache.load().test()

		loader.advanceTime(20)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"))

		loadTest.assertValuesOnly(
				Loading(Empty, loader),
				Loaded("Hello"))
	}

	@Test
	fun load_withCustomLoader() {
		val timer = TestScheduler()
		val loader = DebugLoader.delayed("Hello", 10, timer)
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		val observeTest = cache.observe().test()

		val customLoader = DebugLoader.delayed("Hi", 10, timer)
		val observable = cache.load(Updaters.auto(customLoader))
		val loadTest = observable.test()

		timer.advanceTimeBy(20, TimeUnit.SECONDS)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, customLoader),
				Loaded("Hi"))

		loadTest.assertValuesOnly(
				Loading(Empty, customLoader),
				Loaded("Hi"))
	}

	@Test
	fun loadThen() {
		val loader = DebugLoader.delayed("Hello", 10)
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		val observeTest = cache.observe().test()

		val relay = PublishSubject.create<LoadStatus<String>>()
		val loadTest = relay.test()

		cache.loadThen { relay.onNext(it) }

		loader.advanceTime(20)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, loader),
				Loaded("Hello"))

		loadTest.assertValuesOnly(
				Loading(Empty, loader),
				Loaded("Hello"))
	}

	@Test
	fun loadThen_withCustomLoader() {
		val timer = TestScheduler()
		val loader = DebugLoader.delayed("Hello", 10, timer)
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		val observeTest = cache.observe().test()

		val customLoader = DebugLoader.delayed("Hi", 10, timer)
		val relay = PublishSubject.create<LoadStatus<String>>()
		val loadTest = relay.test()

		cache.loadThen(Updaters.auto(customLoader)) { relay.onNext(it) }

		timer.advanceTimeBy(20, TimeUnit.SECONDS)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, customLoader),
				Loaded("Hi"))

		loadTest.assertValuesOnly(
				Loading(Empty, customLoader),
				Loaded("Hi"))
	}

	@Test
	fun currentStatus() {
		val loader = DebugLoader.delayed("Hello", 10)
		val cache = SimpleCache(loader, Schedulers.trampoline())

		cache.printLog()

		cache.currentStatus().test().assertResult(Empty)

		cache.set("Hi")
		cache.currentStatus().test().assertResult(Loaded("Hi"))

		cache.set(null)
		cache.currentStatus().test().assertResult(Empty)

		cache.load()
		cache.currentStatus().test().assertResult(Loading(Empty, DebugLoader("Hello")))

		loader.advanceTime(11)
		cache.currentStatus().test().assertResult(Loaded("Hello"))

		cache.set("World")
		cache.currentStatus().test().assertResult(Loaded("World"))

		cache.load()
		cache.currentStatus().test().assertResult(Loaded("World"))

		loader.advanceTime(11)
		cache.currentStatus().test().assertResult(Loaded("World"))
	}
}

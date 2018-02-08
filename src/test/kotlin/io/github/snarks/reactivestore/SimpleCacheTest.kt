package io.github.snarks.reactivestore

import io.github.snarks.reactivestore.caches.*
import io.github.snarks.reactivestore.utils.*
import io.reactivex.Single
import org.junit.Test

class SimpleCacheTest {

	@Test
	fun update() {
		val loader = Single.just("Hello")
		val cache = SimpleCache(loader)
		val test = cache.observe().test()

		cache.update(Updaters.auto())
		Thread.sleep(50)
		cache.update(Updaters.set("Hi"))
		cache.update(Updaters.reload())
		Thread.sleep(50)
		cache.update(Updaters.set(null))
		cache.update(Updaters.auto())
		Thread.sleep(50)
		cache.update(Updaters.auto())
		Thread.sleep(50)
		cache.update(Updaters.reload())
		cache.update(Updaters.cancelLoading())
		Thread.sleep(50)

		test.onComplete()
		test.assertResult(
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
				Loaded("Hello"))
	}

	@Test
	fun update_withExtensions() {
		val loader = Single.just("Hello")
		val cache = SimpleCache(loader)
		val test = cache.observe().test()

		cache.update(Updaters.auto())
		Thread.sleep(50)
		cache.set("Hi")
		cache.reload()
		Thread.sleep(50)
		cache.set(null)
		cache.load()
		Thread.sleep(50)
		cache.load()
		Thread.sleep(50)
		cache.reload()
		cache.cancelLoading()
		Thread.sleep(50)

		test.onComplete()
		test.assertResult(
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
				Loaded("Hello"))
	}

	@Test
	fun update_withErrors() {
		val exception = Exception()
		val loader = Single.error<String>(exception)
		val cache = SimpleCache<String>(loader)
		val test = cache.observe().test()

		cache.update(Updaters.auto())
		Thread.sleep(50)
		cache.set("Hi")
		cache.reload()
		Thread.sleep(50)
		cache.set(null)
		cache.load()
		Thread.sleep(50)
		cache.load()
		Thread.sleep(50)
		cache.reload()
		cache.resetFailure()
		Thread.sleep(50)

		test.onComplete()
		test.assertResult(
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
				Empty)
	}

	@Test
	fun observe() {
		val cache = SimpleCache(Single.just("Hello"))

		cache.observe().test().apply { onComplete() }
				.assertResult(Empty)

		cache.set("Hi")
		Thread.sleep(50)

		cache.observe().test().apply { onComplete() }
				.assertResult(Loaded("Hi"))

		cache.set(null)
		Thread.sleep(50)

		cache.observe().test().apply { onComplete() }
				.assertResult(Empty)

		cache.load()
		Thread.sleep(50)

		cache.observe().test().apply { onComplete() }
				.assertResult(Loaded("Hello"))

		cache.set("World")
		Thread.sleep(50)

		cache.observe().test().apply { onComplete() }
				.assertResult(Loaded("World"))

		cache.load()
		Thread.sleep(50)

		cache.observe().test().apply { onComplete() }
				.assertResult(Loaded("World"))
	}
}
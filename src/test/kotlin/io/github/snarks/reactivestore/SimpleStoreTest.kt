package io.github.snarks.reactivestore

import io.github.snarks.reactivestore.stores.*
import io.github.snarks.reactivestore.utils.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class SimpleStoreTest {

	@Test
	fun update_withUpdaters() {
		val loaderSupplier = { key: String -> DebugLoader("Hello $key") }
		val store = SimpleStore(loaderSupplier, Schedulers.trampoline())

		val testA = store.observe("A").test()
		val testB = store.observe("B").test()
		val testC = store.observe("C").test()

		store.printLog()

		store.update("B", Updaters.auto())
		store.update("A", Updaters.auto())
		store.update("A", Updaters.set("Custom Value"))
		store.update("A", Updaters.reload())
		store.update("A", Updaters.set(null))
		store.update("A", Updaters.auto())
		store.update("A", Updaters.auto())
		store.update("A", Updaters.reload())
		store.update("A", Updaters.cancelLoading())
		store.update("C", Updaters.auto())

		testA.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"),
				Loaded("Custom Value"),
				Loading(Loaded("Custom Value"), DebugLoader("Hello A")),
				Loaded("Hello A"),
				Empty,
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"),
				Loading(Loaded("Hello A"), DebugLoader("Hello A")),
				Loaded("Hello A"))

		testB.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello B")),
				Loaded("Hello B"))

		testC.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello C")),
				Loaded("Hello C"))
	}

	@Test
	fun update_withExtensions() {
		val loaderSupplier = { key: String -> DebugLoader("Hello $key") }
		val store = SimpleStore(loaderSupplier, Schedulers.trampoline())

		val testA = store.observe("A").test()
		val testB = store.observe("B").test()
		val testC = store.observe("C").test()

		store.printLog()

		store.load("B")
		store.load("A")
		store["A"] = "Custom Value"
		store.reload("A")
		store["A"] = null
		store.load("A")
		store.load("A")
		store.reload("A")
		store.cancelLoading("A")
		store.load("C")

		testA.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"),
				Loaded("Custom Value"),
				Loading(Loaded("Custom Value"), DebugLoader("Hello A")),
				Loaded("Hello A"),
				Empty,
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"),
				Loading(Loaded("Hello A"), DebugLoader("Hello A")),
				Loaded("Hello A"))

		testB.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello B")),
				Loaded("Hello B"))

		testC.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello C")),
				Loaded("Hello C"))
	}

	@Test
	fun update_withErrors() {
		val loaderSupplier = { key: String -> DebugLoader.error<String>(DebugException("Failed $key!")) }
		val store = SimpleStore(loaderSupplier, Schedulers.trampoline())

		val testA = store.observe("A").test()
		val testB = store.observe("B").test()
		val testC = store.observe("C").test()

		store.printLog()

		store.load("B")
		store.load("A")
		store["A"] = "Custom Value"
		store.reload("A")
		store["A"] = null
		store.load("A")
		store.load("A")
		store.reload("A")
		store.resetFailure("A")
		store.load("C")

		testA.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader.error("Failed A!")),
				Failed(Empty, DebugException("Failed A!")),
				Loaded("Custom Value"),
				Loading(Loaded("Custom Value"), DebugLoader.error("Failed A!")),
				Failed(Loaded("Custom Value"), DebugException("Failed A!")),
				Empty,
				Loading(Empty, DebugLoader.error("Failed A!")),
				Failed(Empty, DebugException("Failed A!")),
				Loading(Empty, DebugLoader.error("Failed A!")),
				Failed(Empty, DebugException("Failed A!")),
				Loading(Failed(Empty, DebugException("Failed A!")), DebugLoader.error("Failed A!")),
				Failed(Empty, DebugException("Failed A!")),
				Empty)

		testB.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader.error("Failed B!")),
				Failed(Empty, DebugException("Failed B!")))

		testC.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader.error("Failed C!")),
				Failed(Empty, DebugException("Failed C!")))
	}

	@Test
	fun contents() {
		fun <K : Any, V : Any> SimpleStore<K, V>.assertContents(vararg expected: V) {
			val contentSet = contents().toList().blockingGet().toSet()
			Assert.assertEquals(expected.toSet(), contentSet)
		}

		val store = SimpleStore<String, String>({ Single.just("Hello $it") }, Schedulers.trampoline())

		store.printLog()

		store.load("A")
		store.load("B")
		store.load("C")
		store.assertContents("Hello A", "Hello B", "Hello C")

		store["C"] = null
		store.assertContents("Hello A", "Hello B")

		store.reload("B", Single.error(DebugException("error!")))
		store.assertContents("Hello A", "Hello B")

		store["A"] = null
		store.assertContents("Hello B")

		store.load("A")
		store.load("C")
		store.assertContents("Hello A", "Hello B", "Hello C")

		store["C"] = "Hi C"
		store.assertContents("Hello A", "Hello B", "Hi C")
	}

	@Test
	fun keys() {
		fun <K : Any, V : Any> SimpleStore<K, V>.assertKeys(vararg expected: V) {
			val keySet = keys().toList().blockingGet().toSet()
			Assert.assertEquals(expected.toSet(), keySet)
		}

		val store = SimpleStore<String, String>({ Single.just("Hello $it") }, Schedulers.trampoline())

		store.printLog()

		store.load("A")
		store.load("B")
		store.load("C")
		store.assertKeys("A", "B", "C")

		store["C"] = null
		store.assertKeys("A", "B")

		store.reload("B", Single.error(DebugException("error!")))
		store.assertKeys("A", "B")

		store["A"] = null
		store.assertKeys("B")

		store.load("A")
		store.load("C")
		store.assertKeys("A", "B", "C")

		store["C"] = "Hi C"
		store.assertKeys("A", "B", "C")
	}

	@Test
	fun load() {
		val timer = TestScheduler()
		val store = SimpleStore<String, String>({ DebugLoader.delayed("Hello $it", 10, timer) }, Schedulers.trampoline())

		store.printLog()

		val observeTest = store.observe("A").test()
		val loadTest = store.load("A").test()

		timer.advanceTimeBy(20, TimeUnit.SECONDS)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"))

		loadTest.assertValuesOnly(
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"))
	}

	@Test
	fun load_withCustomLoader() {
		val timer = TestScheduler()
		val store = SimpleStore<String, String>({ DebugLoader.delayed("Hello $it", 10, timer) }, Schedulers.trampoline())

		store.printLog()

		val customLoader = DebugLoader.delayed("Custom Value!", 10, timer)
		val observeTest = store.observe("A").test()
		val loadTest = store.load("A", Updaters.auto(customLoader)).test()

		timer.advanceTimeBy(20, TimeUnit.SECONDS)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Custom Value!")),
				Loaded("Custom Value!"))

		loadTest.assertValuesOnly(
				Loading(Empty, DebugLoader("Custom Value!")),
				Loaded("Custom Value!"))
	}

	@Test
	fun loadThen() {
		val timer = TestScheduler()
		val store = SimpleStore<String, String>(
				{ key: String -> DebugLoader.delayed("Hello $key", 10, timer) },
				Schedulers.trampoline())

		store.printLog()

		val observeTest = store.observe("A").test()

		val relay = PublishSubject.create<LoadStatus<String>>()
		val loadTest = relay.test()

		store.loadThen("A") { relay.onNext(it) }

		timer.advanceTimeBy(20, TimeUnit.SECONDS)

		observeTest.assertValuesOnly(
				Empty,
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"))

		loadTest.assertValuesOnly(
				Loading(Empty, DebugLoader("Hello A")),
				Loaded("Hello A"))
	}

	@Test
	fun loadThen_withCustomLoader() {
		val timer = TestScheduler()
		val store = SimpleStore<String, String>({ DebugLoader.delayed("Hello $it", 10, timer) }, Schedulers.trampoline())

		store.printLog()

		val observeTest = store.observe("A").test()

		val customLoader = DebugLoader.delayed("Hi", 10, timer)
		val relay = PublishSubject.create<LoadStatus<String>>()
		val loadTest = relay.test()

		store.loadThen("A", Updaters.auto(customLoader)) { relay.onNext(it) }

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
	fun observeUpdates() {
		val store = SimpleStore<String, String>({ DebugLoader("Hello $it") }, Schedulers.trampoline())

		store.printLog()

		val test = store.observeUpdates().test()

		store.load("B")
		store.load("A")
		store["A"] = "Custom Value"
		store.reload("A")
		store["A"] = null
		store.load("A")
		store.load("A")
		store.reload("A")
		store.resetFailure("A")
		store.load("C")

		test.assertValuesOnly(
				"B" to Loading(Empty, DebugLoader("Hello B")),
				"B" to Loaded("Hello B"),
				"A" to Loading(Empty, DebugLoader("Hello A")),
				"A" to Loaded("Hello A"),
				"A" to Loaded("Custom Value"),
				"A" to Loading(Loaded("Custom Value"), DebugLoader("Hello A")),
				"A" to Loaded("Hello A"),
				"A" to Empty,
				"A" to Loading(Empty, DebugLoader("Hello A")),
				"A" to Loaded("Hello A"),
				"A" to Loading(Loaded("Hello A"), DebugLoader("Hello A")),
				"A" to Loaded("Hello A"),
				"C" to Loading(Empty, DebugLoader("Hello C")),
				"C" to Loaded("Hello C"))
	}

	@Test
	fun currentStatus() {
		fun <K : Any, V : Any> ReactiveStore<K, V>.assertStatus(vararg expected: Pair<K, LoadStatus<V>>) {
			val statuses = currentStatus().toList().blockingGet().toSet()
			Assert.assertEquals(expected.toSet(), statuses)
		}

		val timer = TestScheduler()
		val store = SimpleStore<String, String>({ DebugLoader.delayed("Hello $it", 10, timer) }, Schedulers.trampoline())

		store.printLog()

		store.load("A")
		store.load("B")
		store.load("C")
		store.assertStatus(
				"A" to Loading(Empty, DebugLoader("Hello A")),
				"B" to Loading(Empty, DebugLoader("Hello B")),
				"C" to Loading(Empty, DebugLoader("Hello C")))

		timer.advanceTimeBy(20, TimeUnit.SECONDS)
		store.assertStatus(
				"A" to Loaded("Hello A"),
				"B" to Loaded("Hello B"),
				"C" to Loaded("Hello C"))

		store["C"] = null
		store.assertStatus(
				"A" to Loaded("Hello A"),
				"B" to Loaded("Hello B"))

		store.reload("B", DebugLoader.error("error!", 10, timer))
		store.assertStatus(
				"A" to Loaded("Hello A"),
				"B" to Loading(Loaded("Hello B"), DebugLoader.error("error!")))

		timer.advanceTimeBy(20, TimeUnit.SECONDS)
		store.assertStatus(
				"A" to Loaded("Hello A"),
				"B" to Failed(Loaded("Hello B"), DebugException("error!")))

		store["A"] = null
		store.assertStatus(
				"B" to Failed(Loaded("Hello B"), DebugException("error!")))

		store.load("A")
		store.load("C")
		store.assertStatus(
				"A" to Loading(Empty, DebugLoader("Hello A")),
				"B" to Failed(Loaded("Hello B"), DebugException("error!")),
				"C" to Loading(Empty, DebugLoader("Hello C")))

		timer.advanceTimeBy(20, TimeUnit.SECONDS)
		store.assertStatus(
				"A" to Loaded("Hello A"),
				"B" to Failed(Loaded("Hello B"), DebugException("error!")),
				"C" to Loaded("Hello C"))

		store["C"] = "Hi C"
		store.assertStatus(
				"A" to Loaded("Hello A"),
				"B" to Failed(Loaded("Hello B"), DebugException("error!")),
				"C" to Loaded("Hi C"))

		store.update("B", Updaters.resetFailure())
		store.assertStatus(
				"A" to Loaded("Hello A"),
				"B" to Loaded("Hello B"),
				"C" to Loaded("Hi C"))
	}
}

package io.github.snarks.reactivestore

import io.github.snarks.reactivestore.stores.*
import io.github.snarks.reactivestore.utils.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Test

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
}

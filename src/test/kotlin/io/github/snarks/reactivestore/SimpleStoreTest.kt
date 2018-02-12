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
		val loaderA = Single.just("A Loader's value").withPrettyToString()
		val loaderSupplier = { key: String -> if (key == "A") loaderA else Single.just("Hello $key").withPrettyToString() }
		val store = SimpleStore(loaderSupplier, Schedulers.trampoline())

		val testA = store.observe("A").test()
		val testB = store.observe("B").filter { it !is Loading }.test()
		val testC = store.observe("C").filter { it !is Loading }.test()

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
				Loading(Empty, loaderA),
				Loaded("A Loader's value"),
				Loaded("Custom Value"),
				Loading(Loaded("Custom Value"), loaderA),
				Loaded("A Loader's value"),
				Empty,
				Loading(Empty, loaderA),
				Loaded("A Loader's value"),
				Loading(Loaded("A Loader's value"), loaderA),
				Loaded("A Loader's value"))

		testB.assertValuesOnly(Empty, Loaded("Hello B"))

		testC.assertValuesOnly(Empty, Loaded("Hello C"))
	}

	@Test
	fun update_withExtensions() {
		val loaderA = Single.just("A Loader's value").withPrettyToString()
		val loaderSupplier = { key: String -> if (key == "A") loaderA else Single.just("Hello $key").withPrettyToString() }
		val store = SimpleStore(loaderSupplier, Schedulers.trampoline())

		val testA = store.observe("A").test()
		val testB = store.observe("B").filter { it !is Loading }.test()
		val testC = store.observe("C").filter { it !is Loading }.test()

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
				Loading(Empty, loaderA),
				Loaded("A Loader's value"),
				Loaded("Custom Value"),
				Loading(Loaded("Custom Value"), loaderA),
				Loaded("A Loader's value"),
				Empty,
				Loading(Empty, loaderA),
				Loaded("A Loader's value"),
				Loading(Loaded("A Loader's value"), loaderA),
				Loaded("A Loader's value"))

		testB.assertValuesOnly(Empty, Loaded("Hello B"))

		testC.assertValuesOnly(Empty, Loaded("Hello C"))
	}

	@Test
	fun update_withErrors() {
		val loaderA = Single.error<String>(DebugException("load failed for A!")).withPrettyToString()
		val loaderSupplier = { key: String ->
			if (key == "A") loaderA
			else Single.error<String>(DebugException("error $key")).withPrettyToString()
		}
		val store = SimpleStore(loaderSupplier, Schedulers.trampoline())

		val testA = store.observe("A").test()
		val testB = store.observe("B").filter { it !is Loading }.test()
		val testC = store.observe("C").filter { it !is Loading }.test()

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
				Loading(Empty, loaderA),
				Failed(Empty, DebugException("load failed for A!")),
				Loaded("Custom Value"),
				Loading(Loaded("Custom Value"), loaderA),
				Failed(Loaded("Custom Value"), DebugException("load failed for A!")),
				Empty,
				Loading(Empty, loaderA),
				Failed(Empty, DebugException("load failed for A!")),
				Loading(Empty, loaderA),
				Failed(Empty, DebugException("load failed for A!")),
				Loading(Failed(Empty, DebugException("load failed for A!")), loaderA),
				Failed(Empty, DebugException("load failed for A!")),
				Empty)

		testB.assertValuesOnly(Empty, Failed(Empty, DebugException("error B")))

		testC.assertValuesOnly(Empty, Failed(Empty, DebugException("error C")))
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

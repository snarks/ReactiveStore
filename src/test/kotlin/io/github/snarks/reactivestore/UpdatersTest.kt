package io.github.snarks.reactivestore

import io.github.snarks.reactivestore.utils.*
import io.github.snarks.reactivestore.utils.Updaters.auto
import io.github.snarks.reactivestore.utils.Updaters.cancelLoading
import io.github.snarks.reactivestore.utils.Updaters.compareAndUpdate
import io.github.snarks.reactivestore.utils.Updaters.reload
import io.github.snarks.reactivestore.utils.Updaters.resetFailure
import io.github.snarks.reactivestore.utils.Updaters.set
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Test

class UpdatersTest {

	private val exception = Exception()
	private val default = Single.just("Hello")
	private val custom = Single.just("Hi")

	private fun assertUnchanged(input: LoadStatus<String>, updater: Updater<String>) {
		assertSame(input, updater(input, default))
	}

	private fun assertChanged(input: LoadStatus<String>, updater: Updater<String>, expected: LoadStatus<String>) {
		val new = updater(input, default)
		assertNotSame(input, new)
		assertEquals(expected, new)
	}

	@Test
	fun auto_whenEmpty() {
		assertChanged(Empty, auto(), Loading(Empty, default))
		assertChanged(Empty, auto(custom), Loading(Empty, custom))
	}

	@Test
	fun auto_whenLoading() {
		assertUnchanged(Loading(Empty, default), auto())
		assertUnchanged(Loading(Empty, default), auto(custom))
	}

	@Test
	fun auto_whenLoaded() {
		assertUnchanged(Loaded("loaded"), auto())
		assertUnchanged(Loaded("loaded"), auto(custom))
	}

	@Test
	fun auto_whenFailed() {
		assertChanged(Failed(Empty, exception), auto(), Loading(Empty, default))
		assertChanged(Failed(Empty, exception), auto(custom), Loading(Empty, custom))
	}

	@Test
	fun reload_whenEmpty() {
		assertChanged(Empty, reload(), Loading(Empty, default))
		assertChanged(Empty, reload(custom), Loading(Empty, custom))
	}

	@Test
	fun reload_whenLoading() {
		assertUnchanged(Loading(Empty, default), reload())
		assertUnchanged(Loading(Empty, default), reload(custom))
	}

	@Test
	fun reload_whenLoaded() {
		assertChanged(Loaded("loaded"), reload(), Loading(Loaded("loaded"), default))
		assertChanged(Loaded("loaded"), reload(custom), Loading(Loaded("loaded"), custom))
	}

	@Test
	fun reload_whenFailed() {
		assertChanged(Failed(Empty, exception), reload(), Loading(Empty, default))
		assertChanged(Failed(Empty, exception), reload(custom), Loading(Empty, custom))
	}

	@Test
	fun set_whenEmpty() {
		assertChanged(Empty, set("hello"), Loaded("hello"))
		assertUnchanged(Empty, set(null))
	}

	@Test
	fun set_whenLoading() {
		assertChanged(Loading(Empty, default), set("hello"), Loaded("hello"))
		assertChanged(Loading(Empty, default), set(null), Empty)
	}

	@Test
	fun set_whenLoaded() {
		assertChanged(Loaded("loaded"), set("hello"), Loaded("hello"))
		assertChanged(Loaded("loaded"), set(null), Empty)
	}

	@Test
	fun set_whenFailed() {
		assertChanged(Failed(Empty, exception), set("hello"), Loaded("hello"))
		assertChanged(Failed(Empty, exception), set(null), Empty)
	}

	@Test
	fun cancelLoading_whenEmpty() {
		assertUnchanged(Empty, cancelLoading())
	}

	@Test
	fun cancelLoading_whenLoading() {
		assertChanged(Loading(Empty, default), cancelLoading(), Empty)
	}

	@Test
	fun cancelLoading_whenLoaded() {
		assertUnchanged(Loaded("loaded"), cancelLoading())
	}

	@Test
	fun cancelLoading_whenFailed() {
		assertUnchanged(Failed(Empty, exception), cancelLoading())
	}

	@Test
	fun resetFailure_whenEmpty() {
		assertUnchanged(Empty, resetFailure())
	}

	@Test
	fun resetFailure_whenLoading() {
		assertUnchanged(Loading(Empty, default), resetFailure())
	}

	@Test
	fun resetFailure_whenLoaded() {
		assertUnchanged(Loaded("loaded"), resetFailure())
	}

	@Test
	fun resetFailure_whenFailed() {
		assertChanged(Failed(Empty, exception), resetFailure(), Empty)
	}

	@Test
	fun compareAndUpdate_withExpectedValue() {
		val l = Loaded("loaded")
		assertChanged(l, compareAndUpdate(l) { _, _ -> Empty }, Empty)
		assertChanged(Empty, compareAndUpdate<String>(Empty) { _, _ -> Loading(Empty, default) }, Loading(Empty, default))
		assertChanged(Empty, compareAndUpdate<String>(Empty) { _, _ -> Loaded("loaded") }, Loaded("loaded"))
		assertChanged(Empty, compareAndUpdate<String>(Empty) { _, _ -> Failed(Empty, exception) }, Failed(Empty, exception))
	}

	@Test
	fun compareAndUpdate_withUnexpectedValue() {
		assertUnchanged(Loading(Empty, default), compareAndUpdate<String>(Empty) { _, _ -> Loaded("loaded") })
		assertUnchanged(Empty, compareAndUpdate<String>(Loaded("loaded")) { _, _ -> Loading(Empty, default) })
		assertUnchanged(Empty, compareAndUpdate<String>(Loading(Empty, default)) { _, _ -> Loading(Empty, default) })
		assertUnchanged(Empty, compareAndUpdate<String>(Failed(Empty, exception)) { _, _ -> Loading(Empty, default) })
	}
}

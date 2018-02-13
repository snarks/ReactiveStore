[![Release](https://jitpack.io/v/snarks/ReactiveStore.svg)](https://jitpack.io/#snarks/RxProperty)

# ReactiveStore
Containers with Observable callbacks.

This library provides 2 main types:
- [ReactiveCache](https://snarks.github.io/ReactiveStore/kotlin-docs/1.0.1/io.github.snarks.reactivestore.caches/-reactive-cache/index.html)
  for loading & storing a single value
- [ReactiveStore](https://snarks.github.io/ReactiveStore/kotlin-docs/1.0.1/io.github.snarks.reactivestore.stores/-reactive-store/index.html)
  for loading & storing multiple values

## Example
Single values with `ReactiveCache`:
```kotlin
val userCache: ReactiveCache = SimpleCache(userApi.getUser(), updateScheduler = uiScheduler)

userCache.load().subscribe { status ->
  when (status) {
    Empty   -> showLoggedOut()
    Loading -> showLoading()
    Loaded  -> showUserPage(status.value)
    Failed  -> showError(status.error)
  }
}

reloadButton.setOnClickListener { userCache.reload() }
```

Multiple values with `ReactiveStore`:
```kotlin
val posts: ReactiveStore = SimpleStore({ id -> postApi.getPost(id) }, updateScheduler = uiScheduler)

posts.load(postId).subscribe { status ->
  when (status) {
    Empty   -> showEmptyCard()
    Loading -> showLoading()
    Loaded  -> populateCard(status.value)
    Failed  -> showErrorCard(status.error)
  }
}

reloadButton.setOnClickListener { posts.reload(postId) }

posts.contents().toList().subscribe { loadedPosts ->
  postList.populate(loadedPosts)
}
```

## Documentation
The Kotlin docs can be found [here](https://snarks.github.io/ReactiveStore/kotlin-docs/1.0.1/).

## Adding ReactiveStore to your Project
You can add this project as a dependency via [JitPack](https://jitpack.io/).

```gradle
repositories {
    jcenter()
    maven { url "https://jitpack.io" }
}
dependencies {
     compile 'io.github.snarks:ReactiveStore:1.0.1'
}
```
(_`com.github.snarks` will also work_)

package io.github.snarks.reactivestore.cache

interface Cache<T> : CacheSink<T>, CacheSource<T>

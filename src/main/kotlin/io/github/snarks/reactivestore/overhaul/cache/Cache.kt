package io.github.snarks.reactivestore.overhaul.cache

interface Cache<T> : CacheSink<T>, CacheSource<T>

package com.example.walklogger.core.common.di

import javax.inject.Qualifier

/** Qualifier so coroutine dispatchers can be injected (and swapped in tests). */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: WalkLoggerDispatchers)

enum class WalkLoggerDispatchers {
    Default,
    IO,
}

package org.nobledev.kingapi.events

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface IDispatcherProvider {
    fun getDispatcher() : CoroutineDispatcher
}

internal class DefaultDispatcherProvider: IDispatcherProvider {
    override fun getDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

internal object DispatcherProvider {
    var provider = DefaultDispatcherProvider()

    fun get() : CoroutineDispatcher = provider.getDispatcher()
}
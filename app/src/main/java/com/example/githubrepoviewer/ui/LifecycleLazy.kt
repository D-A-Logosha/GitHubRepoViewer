package com.example.githubrepoviewer.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T : Any> LifecycleOwner.lifecycleLazy(): ReadWriteProperty<LifecycleOwner, T> =
    LifecycleLazySet(this)

fun <T : Any> LifecycleOwner.lifecycleLazy(initializer: () -> T): ReadOnlyProperty<LifecycleOwner, T> =
    LifecycleLazyInit(this, initializer)

private class LifecycleLazySet<T : Any>(
    private val lifecycleOwner: LifecycleOwner
) : ReadWriteProperty<LifecycleOwner, T>, DefaultLifecycleObserver {

    private var value: T? = null

    override fun onDestroy(owner: LifecycleOwner) {
        value = null
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Value is not initialized")
    }

    override fun setValue(thisRef: LifecycleOwner, property: KProperty<*>, value: T) {
        this.value ?: run {
            lifecycleOwner.lifecycle.addObserver(this)
            this.value = value
        }
    }
}

private class LifecycleLazyInit<T : Any>(
    private val lifecycleOwner: LifecycleOwner,
    private val initializer: () -> T
) : ReadOnlyProperty<LifecycleOwner, T>, DefaultLifecycleObserver {

    private var value: T? = null

    override fun onDestroy(owner: LifecycleOwner) {
        value = null
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {
        return this.value ?: run {
            lifecycleOwner.lifecycle.addObserver(this)
            this.value = initializer()
            this.value!!
        }
    }
}

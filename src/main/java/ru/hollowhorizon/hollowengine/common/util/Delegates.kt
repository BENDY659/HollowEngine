package ru.hollowhorizon.hollowengine.common.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class SafeGetter<V>(val property: () -> V) : ReadWriteProperty<Any?, Safe<V>> {
    val value = Safe(property)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Safe<V> {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Safe<V>) {
        this.value.data = value.data
    }
}

open class Safe<T: Any?>(var data: () -> T?): () -> T {
    val isLoaded get() = data() != null

    override operator fun invoke(): T = data()!!
}

fun <T, V : List<T>> SafeGetter<V>.filter(filter: (T) -> Boolean) = SafeGetter { this.property().filter(filter) }
fun <T, N, V : List<T>> SafeGetter<V>.map(transform: (T) -> N) = SafeGetter { this.property().map(transform) }
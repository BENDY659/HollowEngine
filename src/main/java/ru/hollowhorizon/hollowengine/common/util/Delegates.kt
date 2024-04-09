package ru.hollowhorizon.hollowengine.common.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class SafeGetter<V>(val property: () -> V) : ReadOnlyProperty<Any?, Safe<V>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Safe<V> {
        return Safe(this.property)
    }
}

open class Safe<T>(val value: () -> T): () -> T {
    override operator fun invoke() = value()
}

fun <T, V : List<T>> SafeGetter<V>.filter(filter: (T) -> Boolean) = SafeGetter { this.property().filter(filter) }
fun <T, N, V : List<T>> SafeGetter<V>.map(transform: (T) -> N) = SafeGetter { this.property().map(transform) }
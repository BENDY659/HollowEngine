/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
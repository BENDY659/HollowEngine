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

package ru.hollowhorizon.hollowengine.common.scripting.story

import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.util.INBTSerializable
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class StoryVariable<T : Any>(var value: () -> T, val clazz: Class<T>, val manager: StoryStateMachine) :
    INBTSerializable<CompoundTag>, ReadWriteProperty<Any?, T> {

    override fun serializeNBT() = CompoundTag().apply {
        put("value", NBTFormat.serializeNoInline(value(), clazz))
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        val res = nbt.get("value") ?: return
        value = { NBTFormat.deserializeNoInline(res, clazz) }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        assert(!manager.isStarted) { "Variable $property is used before starting the story!" }
        return value()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        assert(!manager.isStarted) { "Variable $property is used before starting the story!" }
        this.value = { value }
    }
}

class GlobalProperty<T : Any>(val value: () -> T, val clazz: Class<T>, val manager: StoryStateMachine) :
    ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val extra = manager.extra["hollowengine_global_properties"] as? CompoundTag
        val variable = extra?.get(property.name)
        return if (variable != null) {
            NBTFormat.deserializeNoInline(variable, clazz)
        } else {
            value()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val extra = (manager.extra["hollowengine_global_properties"] as? CompoundTag) ?: CompoundTag().apply {
            manager.extra.put("hollowengine_global_properties", this)
        }
        extra.put(property.name, NBTFormat.serializeNoInline(value, clazz))
    }

}

inline fun <reified T : Any> IContextBuilder.global(noinline any: () -> T) =
    GlobalProperty<T>(any, T::class.java, stateMachine)

inline fun <reified T : Any> IContextBuilder.saveable(noinline any: () -> T): StoryVariable<T> {
    return StoryVariable(any, T::class.java, stateMachine).apply {
        this@saveable.stateMachine.variables += this
    }
}
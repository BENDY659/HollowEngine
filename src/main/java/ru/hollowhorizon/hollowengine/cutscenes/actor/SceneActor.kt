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

package ru.hollowhorizon.hollowengine.cutscenes.actor

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import ru.hollowhorizon.hollowengine.cutscenes.actor.animation.ActorAnimation

@Serializable
class SceneActor(val entity: String) {
    private val actorTransform = ActorTransform()
    private val actorAnimation = ActorAnimation()

    @Transient
    var actorEntity: Entity? = null

    @Transient
    var level: Level? = null

    fun maxIndex(): Int {
        val transform = actorTransform.transformMap.keys.maxOrNull() ?: 0
        val animation = actorAnimation.animationMap.keys.maxOrNull() ?: 0
        return maxOf(transform, animation)
    }

    fun init(level: Level) {
        this.level = level

        //actorEntity = EntityType.loadEntityRecursive(generateEntityNBT(entity), level) { it }
    }

    fun update(index: Int) {
        //val transform = TransformationMatrix(actorTransform.transformMap[index])

        actorEntity?.let {
//            it.setPos(
//                transform.translation.x().toDouble(),
//                transform.translation.y().toDouble(),
//                transform.translation.z().toDouble()
//            )
//            it.xRot = transform.leftRotation.i() * transform.leftRotation.j()
//            it.yRot = transform.leftRotation.i() * transform.leftRotation.k()


        }
    }
}
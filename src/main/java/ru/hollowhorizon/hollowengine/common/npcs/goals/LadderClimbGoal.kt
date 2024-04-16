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

package ru.hollowhorizon.hollowengine.common.npcs.goals

import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.level.pathfinder.Path
import java.util.*


class LadderClimbGoal(private val entity: Mob) : Goal() {
    private var path: Path? = null

    init {
        setFlags(EnumSet.of(Flag.MOVE))
    }

    override fun canUse(): Boolean {
        if (!entity.navigation.isDone) {
            path = entity.navigation.path
            return path != null && entity.onClimbable()
        }
        return false
    }

    override fun tick() {
        val path = path ?: return
        val i: Int = path.nextNodeIndex
        if (i + 1 < path.nodeCount) {
            val y: Int = path.getNode(i).y
            val pointNext = path.getNode(i + 1)
            val down = entity.level.getBlockState(entity.blockPosition().below())
            val yMotion = if (pointNext.y < y || (pointNext.y == y && down.isLadder(
                    entity.level,
                    entity.blockPosition().below(),
                    entity
                ))
            ) -0.15 else 0.15
            entity.deltaMovement = entity.deltaMovement.multiply(0.1, 1.0, 0.1)
            entity.deltaMovement = entity.deltaMovement.add(0.0, yMotion, 0.0)
        }
    }
}
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

package ru.hollowhorizon.hollowengine.common.npcs.pathing

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.level.PathNavigationRegion
import net.minecraft.world.level.pathfinder.Node
import net.minecraft.world.level.pathfinder.NodeEvaluator
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.level.pathfinder.PathFinder
import net.minecraft.world.phys.Vec3


class NPCPathFinder(processor: NodeEvaluator, maxVisitedNodes: Int) : PathFinder(processor, maxVisitedNodes) {
    override fun findPath(
        regionIn: PathNavigationRegion, mob: Mob, targetPositions: Set<BlockPos>, maxRange: Float,
        accuracy: Int, searchDepthMultiplier: Float
    ): Path? {
        val path = super.findPath(regionIn, mob, targetPositions, maxRange, accuracy, searchDepthMultiplier)
        return if (path == null) null else PatchedPath(path)
    }

    override fun distance(first: Node, second: Node): Float {
        return first.distanceToXZ(second)
    }

    internal class PatchedPath(original: Path) : Path(
        copyPathPoints(original), original.target, original.canReach()
    ) {
        override fun getEntityPosAtNode(entity: Entity, index: Int): Vec3 {
            val point = getNode(index)
            val d0 = point.x + Mth.floor(entity.bbWidth + 1.0f) * 0.5
            val d1 = point.y.toDouble()
            val d2 = point.z + Mth.floor(entity.bbWidth + 1.0f) * 0.5
            return Vec3(d0, d1, d2)
        }

        companion object {
            private fun copyPathPoints(original: Path): List<Node> {
                val points = ArrayList<Node>()
                for (i in 0 until original.nodeCount) points.add(original.getNode(i))
                return points
            }
        }
    }
}
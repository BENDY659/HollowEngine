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

package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.camera

import com.mojang.math.Vector3f
import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.loadAsNBT
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.client.camera.CameraPath
import ru.hollowhorizon.hollowengine.client.camera.ScreenShakePacket
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager
import ru.hollowhorizon.hollowengine.common.scripting.players
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.SimpleNode


@Serializable
@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
class CameraPathPacket(val path: ICameraPath) : HollowPacketV3<CameraPathPacket> {
    override fun handle(player: Player, data: CameraPathPacket) {
        path.onStartClient()
    }
}

class CameraNode(val builder: CameraContainer.() -> Unit) : Node() {
    private val container by lazy {
        manager.server.playerList.players.forEach {
            it.persistentData.putDouble("start_x", it.x)
            it.persistentData.putDouble("start_y", it.y)
            it.persistentData.putDouble("start_z", it.z)
        }
        CameraContainer(manager.server.playerList.players).apply(builder)
    }

    override fun tick(): Boolean {
        container.update()
        val shouldEnd = !container.isEnd

        if (!shouldEnd) {
            manager.server.playerList.players.forEach {
                val x = it.persistentData.getDouble("start_x")
                val y = it.persistentData.getDouble("start_y")
                val z = it.persistentData.getDouble("start_z")
                it.teleportTo(x, y, z)
                it.setGameMode(GameType.SURVIVAL)
            }
        }

        return shouldEnd
    }

    override fun serializeNBT() = CompoundTag()

    override fun deserializeNBT(nbt: CompoundTag) {}

}

class CameraContainer(val players: List<ServerPlayer>) {
    private var isStarted = false
    private val paths = ArrayDeque<ICameraPath>()

    fun spline(
        time: Int,
        path: String,
        interpolation: Interpolation = Interpolation.LINEAR,
        enableBoarders: Boolean = false,
        boardersInterpolation: Interpolation = Interpolation.LINEAR,
        rotationInterpolation: Interpolation = Interpolation.LINEAR,
    ) {
        val nbt = DirectoryManager.HOLLOW_ENGINE.resolve("camera/${path}").inputStream().loadAsNBT()
        val cameraPath = NBTFormat.deserialize<CameraPath>(nbt)
        paths.add(
            CurveCameraPath(
                time,
                cameraPath,
                interpolation,
                enableBoarders,
                boardersInterpolation,
                rotationInterpolation
            )
        )
    }

    fun static(time: Int, pos: Vec3, rotation: Vec3) {
        paths.add(
            StaticCameraPath(
                time,
                pos,
                Vector3f(rotation.x.toFloat(), rotation.y.toFloat(), rotation.z.toFloat())
            )
        )
    }

    fun entity(time: Int, pos: Vec3, entity: Entity) {
        paths.add(EntityCameraPath(time, pos, entity))
    }

    fun entity(time: Int, pos: Vec3, entity: () -> Entity) {
        entity(time, pos, entity())
    }

    fun update() {
        if (!isStarted) {
            paths.firstOrNull()?.reset()
            paths.firstOrNull()?.onStartServer(players)
            isStarted = true
        } else if (paths.firstOrNull()?.isEnd == true) {
            paths.removeFirst()
            isStarted = false
        }
        paths.firstOrNull()?.serverUpdate(players)
    }

    val isEnd get() = paths.isEmpty()
}

fun IContextBuilder.camera(body: CameraContainer.() -> Unit) = +CameraNode(body)

fun IContextBuilder.shake(config: ScreenShakePacket.() -> Unit) = +SimpleNode {
    val packet = ScreenShakePacket().apply(config)

    stateMachine.server.playerList.players.forEach {
        packet.send(PacketDistributor.PLAYER.with { it })
    }
}
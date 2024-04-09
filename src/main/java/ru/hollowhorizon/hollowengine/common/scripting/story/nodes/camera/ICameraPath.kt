package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.camera

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraftforge.network.PacketDistributor

interface ICameraPath {
    val maxTime: Int
    fun serverUpdate(players: List<Player>)

    fun onStartServer(players: List<ServerPlayer>) {
        players.forEach {
            it.setGameMode(GameType.SPECTATOR)
            CameraPathPacket(this).send(PacketDistributor.PLAYER.with { it })
        }
    }

    fun onStartClient() {

    }

    fun reset()

    val isEnd: Boolean
}
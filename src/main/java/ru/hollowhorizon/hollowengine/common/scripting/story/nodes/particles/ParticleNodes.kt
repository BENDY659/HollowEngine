package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.particles

import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.render.particles.DiscardType
import ru.hollowhorizon.hc.client.render.particles.HollowParticleBuilder
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.particles.api.common.ParticleEmitterInfo
import ru.hollowhorizon.hc.particles.api.common.ParticleHelper
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.SimpleNode
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.next
import kotlin.math.cos
import kotlin.math.sin


class ParticleContainer(val server: MinecraftServer) {
    private lateinit var settings: HollowParticleBuilder
    var world = "minecraft:overworld"
    var particle = "hc:circle"

    fun settings(builder: HollowParticleBuilder.() -> Unit) {
        val dimension = server.levelKeys().find { it.location() == world.rl }
            ?: throw IllegalStateException("Dimension $world not found. Or not loaded!")

        settings = HollowParticleBuilder.create(server.getLevel(dimension)!!, particle, builder)
    }
}

class EffekseerContainer {
    var world = "minecraft:overworld"
    var path = "hc:lightning/lightning"
    var pos = Vec3.ZERO
    internal var scale = Vec3(1.0, 1.0, 1.0)
    var target = ""
    var entity: Entity? = null

    fun scale(x: Number, y: Number, z: Number) {
        scale = Vec3(x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun scale(size: Number) {
        scale = Vec3(size.toDouble(), size.toDouble(), size.toDouble())
    }
}

fun IContextBuilder.effekseer(path: EffekseerContainer.() -> Unit) = next {
    val container = EffekseerContainer().apply(path)
    val dimension = manager.server.levelKeys().find { it.location() == container.world.rl }
        ?: throw IllegalStateException("Dimension ${container.world} not found. Or not loaded!")

    val info =
        ParticleEmitterInfo(container.path.removeSuffix(".efkefc").rl).apply {
            if(container.entity != null) {
                bindOnEntity(container.entity!!)
                if(container.target.isNotEmpty()) bindOnTarget(container.target)
            }
            else position(container.pos)
            scale(container.scale.x.toFloat(), container.scale.y.toFloat(), container.scale.z.toFloat())
        }

    ParticleHelper.addParticle(manager.server.getLevel(dimension)!!, true, info)
}

fun IContextBuilder.particles(builder: ParticleContainer.() -> Unit) = +SimpleNode {
    ParticleContainer(this@particles.stateMachine.server).apply(builder)
}
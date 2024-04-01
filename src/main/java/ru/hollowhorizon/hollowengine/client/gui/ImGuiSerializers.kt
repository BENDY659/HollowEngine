package ru.hollowhorizon.hollowengine.client.gui

import imgui.type.ImFloat
import imgui.type.ImString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ImFloatSerializer: KSerializer<ImFloat> {
    override val descriptor = PrimitiveSerialDescriptor("FloatNBT", PrimitiveKind.FLOAT)

    override fun deserialize(decoder: Decoder) = ImFloat().apply { set(decoder.decodeFloat()) }
    override fun serialize(encoder: Encoder, value: ImFloat) = encoder.encodeFloat(value.get())
}
object ImStringSerializer: KSerializer<ImString> {
    override val descriptor = PrimitiveSerialDescriptor("StringNBT", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = ImString().apply { set(decoder.decodeString()) }
    override fun serialize(encoder: Encoder, value: ImString) = encoder.encodeString(value.get())
}
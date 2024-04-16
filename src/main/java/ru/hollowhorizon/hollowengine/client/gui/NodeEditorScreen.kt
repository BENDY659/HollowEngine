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

package ru.hollowhorizon.hollowengine.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.ImNodesContext
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation
import imgui.extension.imnodes.flag.ImNodesPinShape
import imgui.extension.nodeditor.NodeEditor
import imgui.extension.nodeditor.NodeEditorContext
import imgui.extension.nodeditor.flag.NodeEditorPinKind
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiMouseButton
import imgui.type.ImFloat
import imgui.type.ImString
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen
import java.awt.Desktop
import java.net.URI


class NodeEditorScreen : HollowScreen() {

    private val nodeContext = ImNodesContext()
    private val context = NodeEditorContext()
    private val graph = Graph()

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {

        ImguiHandler.drawFrame {
            ImGui.setNextWindowSize(500f, 400f, ImGuiCond.Once)
            ImGui.setNextWindowPos(
                ImGui.getMainViewport().posX + 100,
                ImGui.getMainViewport().posY + 100,
                ImGuiCond.Once
            )
            if (ImGui.begin("ImNodes Demo")) {
                ImGui.text("This a demo graph editor for ImNodes")

                ImGui.alignTextToFramePadding()
                ImGui.text("Repo:")
                ImGui.sameLine()
                if (ImGui.button(URL)) {
                    try {
                        Desktop.getDesktop().browse(URI(URL))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                ImNodes.editorContextSet(nodeContext)
                ImNodes.beginNodeEditor()

                for (node in graph.nodes.values.map { it as Graph.Node }) {
                    ImNodes.beginNode(node.nodeId)

                    ImNodes.beginNodeTitleBar()
                    ImGui.text(node.name)
                    ImNodes.endNodeTitleBar()

                    ImNodes.beginInputAttribute(node.inputPinId, ImNodesPinShape.CircleFilled)
                    ImGui.text("In")
                    ImNodes.endInputAttribute()

                    ImGui.sameLine()

                    ImNodes.beginOutputAttribute(node.outputPinId)
                    ImGui.text("Out")
                    ImNodes.endOutputAttribute()

                    node.render()

                    ImNodes.endNode()
                }

                var uniqueLinkId = 1
                for (node in graph.nodes.values.map { it as Graph.Node }) {
                    if (graph.nodes.containsKey(node.outputPinId)) {
                        ImNodes.link(
                            uniqueLinkId++,
                            node.outputPinId,
                            (graph.nodes[node.outputNodes[node.outputPinId]] as Graph.Node).nodeId
                        )
                    }
                }

                val isEditorHovered = ImNodes.isEditorHovered()

                ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight)
                ImNodes.endNodeEditor()

//                if (ImNodes.isLinkCreated(LINK_A, LINK_B)) {
//                    val source: Graph.Node? = graph.findByOutput(LINK_A.get())
//                    val target: Graph.Node? = graph.findByInput(LINK_B.get())
//                    if (source != null && target != null && source.outputNodeId !== target.nodeId) {
//                        source.outputNodeId = target.nodeId
//                    }
//                }


                if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                    val hoveredNode = ImNodes.getHoveredNode()
                    if (hoveredNode != -1) {
                        ImGui.openPopup("node_context")
                        ImGui.getStateStorage().setInt(ImGui.getID("delete_node_id"), hoveredNode)
                    } else if (isEditorHovered) {
                        ImGui.openPopup("node_editor_context")
                    }
                }

                if (ImGui.isPopupOpen("node_context")) {
                    val targetNode = ImGui.getStateStorage().getInt(ImGui.getID("delete_node_id"))
                    if (ImGui.beginPopup("node_context")) {
                        if (ImGui.button("Delete ")) {
                            graph.nodes.remove(targetNode)
                            ImGui.closeCurrentPopup()
                        }
                        ImGui.endPopup()
                    }
                }

                if (ImGui.beginPopup("node_editor_context")) {
                    if (ImGui.button("Create New Node")) {
                        val node: Graph.Node = graph.createGraphNode(::NPCNode)
                        ImNodes.setNodeScreenSpacePos(node.nodeId, ImGui.getMousePosX(), ImGui.getMousePosY())
                        ImGui.closeCurrentPopup()
                    }
                    ImGui.endPopup()
                }
            }
            ImGui.end()
        }
    }
}

interface GraphNode

@Serializable
class Graph {
    var nextNodeId = 1
    var nextPinId = 100
    val nodes: MutableMap<Int, GraphNode> = HashMap()

    fun createGraphNode(builder: (id: Int, input: Int, output: Int) -> Node): Node {
        val node = builder(nextNodeId++, nextPinId++, nextPinId++)
        nodes[node.nodeId] = node
        return node
    }

    fun findByInput(inputPinId: Long): Node? {
        for (node in nodes.values) {
            if (node !is Node) continue
            if (node.inputPinId.toLong() == inputPinId) {
                return node
            }
        }
        return null
    }

    fun findByOutput(outputPinId: Long): Node? {
        for (node in nodes.values) {
            if (node !is Node) continue
            if (node.outputPinId.toLong() == outputPinId) {
                return node
            }
        }
        return null
    }

    @Serializable
    @Polymorphic(GraphNode::class)
    open class Node(val nodeId: Int, val inputPinId: Int, val outputPinId: Int) : GraphNode {
        var outputNodes = arrayListOf<Int>()

        var name = "Узел " + (64 + nodeId).toChar()
        var width = 200f

        fun render() {
            ImGui.pushID(nodeId)
            renderNode()
            ImGui.popID()
        }

        open fun renderNode() {

        }
    }
}

@Polymorphic(GraphNode::class)
@Serializable
class NPCNode(private val nodeId0: Int, private val inputPinId0: Int, private val outputPinId0: Int) :
    Graph.Node(nodeId0, inputPinId0, outputPinId0) {
    val npcName: @Serializable(ImStringSerializer::class) ImString = ImString()
    val npcModel: @Serializable(ImStringSerializer::class) ImString = ImString()
    val x: @Serializable(ImFloatSerializer::class) ImFloat = ImFloat(0f)
    val y: @Serializable(ImFloatSerializer::class) ImFloat = ImFloat(0f)
    val z: @Serializable(ImFloatSerializer::class) ImFloat = ImFloat(0f)

    init {
        name = "Создание персонажа"
        width = 630f
        npcName.set("Виталик")
        npcModel.set("hollowengine:models/entity/player_model.gltf")
    }

    override fun renderNode() {
        ImGui.pushItemWidth(400f)
        ImGui.inputText("Имя персонажа", npcName)
        ImGui.inputText("Модель персонажа", npcModel)
        ImGui.popItemWidth()

        ImGui.pushItemWidth(150f)
        ImGui.inputFloat("x", x)
        ImGui.sameLine()
        ImGui.inputFloat("y", y)
        ImGui.sameLine()
        ImGui.inputFloat("z", z)
        ImGui.popItemWidth()
    }
}

@Polymorphic(GraphNode::class)
@Serializable
class MoveToBlockNode(private val nodeId0: Int, private val inputPinId0: Int, private val outputPinId0: Int) :
    Graph.Node(nodeId0, inputPinId0, outputPinId0) {
    val x: @Serializable(ImFloatSerializer::class) ImFloat = ImFloat(0f)
    val y: @Serializable(ImFloatSerializer::class) ImFloat = ImFloat(0f)
    val z: @Serializable(ImFloatSerializer::class) ImFloat = ImFloat(0f)

    init {
        name = "Идти к блоку"
        width = 300f
    }

    override fun renderNode() {
        ImGui.pushItemWidth(150f)
        ImGui.inputFloat("x", x)
        ImGui.inputFloat("y", y)
        ImGui.inputFloat("z", z)
        ImGui.popItemWidth()
    }
}

@Polymorphic(GraphNode::class)
@Serializable
class SayNode(private val nodeId0: Int, private val inputPinId0: Int, private val outputPinId0: Int) :
    Graph.Node(nodeId0, inputPinId0, outputPinId0) {
    val nameTalker: @Serializable(ImStringSerializer::class) ImString = ImString()
    val text: @Serializable(ImStringSerializer::class) ImString = ImString()
    val color = floatArrayOf(1f, 1f, 1f, 1f)

    init {
        name = "Сказать"
        width = 600f
    }

    override fun renderNode() {
        ImGui.pushItemWidth(400f)
        ImGui.inputText("Имя говорящего", nameTalker)
        ImGui.colorPicker3("Цвет ника", color)
        ImGui.inputText("Текст", text)
    }
}
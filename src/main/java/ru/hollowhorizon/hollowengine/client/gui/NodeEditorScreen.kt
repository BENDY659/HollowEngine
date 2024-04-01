package ru.hollowhorizon.hollowengine.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.extension.imnodes.ImNodesContext
import imgui.extension.nodeditor.NodeEditor
import imgui.extension.nodeditor.NodeEditorContext
import imgui.extension.nodeditor.flag.NodeEditorPinKind
import imgui.type.ImFloat
import imgui.type.ImLong
import imgui.type.ImString
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen

class NodeEditorScreen : HollowScreen() {

    private val nodeContext = ImNodesContext()
    private val context = NodeEditorContext()
    private val graph = Graph()

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {

        ImguiHandler.drawFrame {
            NodeEditor.setCurrentEditor(context)
            NodeEditor.begin("Редактор Узлов")

            for (node in graph.nodes.values) {
                if (node !is Graph.Node) continue

                NodeEditor.beginNode(node.nodeId.toLong())

                node.render()

                NodeEditor.endNode()
            }

            if (NodeEditor.beginCreate()) {
                val a = ImLong()
                val b = ImLong()
                if (NodeEditor.queryNewLink(a, b)) {
                    val source = graph.findByOutput(a.get())
                    val target = graph.findByInput(b.get())
                    if (source != null && target != null && !source.outputNodes.contains(target.nodeId) && NodeEditor.acceptNewItem()) {
                        source.outputNodes.add(target.nodeId)
                    }
                }
            }
            NodeEditor.endCreate()

            var uniqueLinkId = 1
            for (node in graph.nodes.values) {
                if (node !is Graph.Node) continue
                for (outputNodeId in node.outputNodes) {
                    val outputNode = graph.nodes[outputNodeId] ?: continue
                    if (outputNode !is Graph.Node) continue
                    NodeEditor.link(uniqueLinkId++.toLong(), node.outputPinId.toLong(), outputNode.inputPinId.toLong())
                    uniqueLinkId++
                }
            }

            NodeEditor.suspend()

            val nodeWithContextMenu = NodeEditor.getNodeWithContextMenu()
            if (nodeWithContextMenu >= 0) {
                ImGui.openPopup("node_context")
                ImGui.getStateStorage().setInt(ImGui.getID("delete_node_id"), nodeWithContextMenu.toInt());
            }

            if (ImGui.isPopupOpen("node_context")) {
                val targetNode = ImGui.getStateStorage().getInt(ImGui.getID("delete_node_id"));
                if (ImGui.beginPopup("node_context")) {
                    if (ImGui.button("Удалить " + (graph.nodes[targetNode] as? Graph.Node)?.name)) {
                        graph.nodes.remove(targetNode)
                        ImGui.closeCurrentPopup()
                    }
                    ImGui.endPopup()
                }
            }

            if (NodeEditor.showBackgroundContextMenu()) {
                ImGui.openPopup("node_editor_context")
            }

            if (ImGui.beginPopup("node_editor_context")) {
                if (ImGui.button("Создать персонажа")) {
                    val node = graph.createGraphNode { id, input, output -> NPCNode(id, input, output) }
                    val canvasX = NodeEditor.toCanvasX(ImGui.getMousePosX())
                    val canvasY = NodeEditor.toCanvasY(ImGui.getMousePosY())
                    NodeEditor.setNodePosition(node.nodeId.toLong(), canvasX, canvasY)
                    ImGui.closeCurrentPopup()
                }
                if (ImGui.button("Идти к блоку")) {
                    val node = graph.createGraphNode { id, input, output -> MoveToBlockNode(id, input, output) }
                    val canvasX = NodeEditor.toCanvasX(ImGui.getMousePosX())
                    val canvasY = NodeEditor.toCanvasY(ImGui.getMousePosY())
                    NodeEditor.setNodePosition(node.nodeId.toLong(), canvasX, canvasY)
                    ImGui.closeCurrentPopup()
                }
                if (ImGui.button("Сказать")) {
                    val node = graph.createGraphNode { id, input, output -> SayNode(id, input, output) }
                    val canvasX = NodeEditor.toCanvasX(ImGui.getMousePosX())
                    val canvasY = NodeEditor.toCanvasY(ImGui.getMousePosY())
                    NodeEditor.setNodePosition(node.nodeId.toLong(), canvasX, canvasY)
                    ImGui.closeCurrentPopup()
                }
                ImGui.endPopup()
            }

            NodeEditor.resume()
            NodeEditor.end()
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
            ImGui.text(name)

            ImGui.pushID(nodeId)
            renderNode()
            ImGui.popID()

            NodeEditor.beginPin(inputPinId.toLong(), NodeEditorPinKind.Input)
            ImGui.text(">")
            NodeEditor.endPin()
            ImGui.sameLine()

            ImGui.setCursorPosX(ImGui.getCursorPosX() + width - 10f)
            NodeEditor.beginPin(outputPinId.toLong(), NodeEditorPinKind.Output)
            ImGui.text("<")
            NodeEditor.endPin()
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
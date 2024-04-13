package ru.hollowhorizon.hollowengine.client.gui.scripting

import imgui.ImGui
import imgui.ImGuiWindowClass
import imgui.extension.texteditor.TextEditor
import imgui.extension.texteditor.TextEditorLanguageDefinition
import imgui.flag.*
import imgui.type.ImBoolean
import imgui.type.ImInt
import imgui.type.ImString
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.ModList
import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toTexture
import ru.hollowhorizon.hollowengine.client.gui.height
import ru.hollowhorizon.hollowengine.client.gui.width
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager.toReadablePath
import java.io.File

object CodeEditor {
    val files = HashSet<ScriptData>()
    var currentFile = ""
    var currentPath = ""
    var selectedPath = ""
    val editor = TextEditor().apply {
        setLanguageDefinition(KOTLIN_LANG)

        text = """
            val npc by NPCEntity.creating {
                model = "hollowengine:models/model.gltf"
                textures["default_color_map"] = skin("TheHollowHorizon")
                pos = team.randomPos(5f)
            }

            npc moveTo { team }

            npc lookAt { team.randomPos(5f) }

            npc requestItems {
                +item("minecraft:stone", 10)
                +item("minecraft:dirt", 10)
            }

            npc.name = "Виталик"

            npc say { "Привет, я \$\{npc()!!.name.string}" }
        """.trimIndent()
    }
    var tree = Tree("Загрузка", "null")
    var updateTime = 0
    val input = ImString()
    var inputText = ""
    var inputAction = -1
    var shouldClose = false

    fun draw() {
        if (TickHandler.currentTicks - updateTime > 100) {
            updateTime = TickHandler.currentTicks
            RequestTreePacket().send()
        }

        ImGui.setNextWindowPos(0f, 0f)
        ImGui.setNextWindowSize(width, height)
        val shouldDrawWindowContents = ImGui.begin(
            "CodeEditorSpace",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar
        )
        val dockspaceID = ImGui.getID("MyWindow_DockSpace")
        val workspaceWindowClass = ImGuiWindowClass()
        workspaceWindowClass.setClassId(dockspaceID)
        workspaceWindowClass.dockingAllowUnclassed = false

        if (imgui.internal.ImGui.dockBuilderGetNode(dockspaceID).ptr == 0L) {
            imgui.internal.ImGui.dockBuilderAddNode(
                dockspaceID, imgui.internal.flag.ImGuiDockNodeFlags.DockSpace or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoWindowMenuButton or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoCloseButton
            )
            val region = ImGui.getContentRegionAvail()
            imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceID, region.x, region.y)

            val leftDockID = ImInt(0)
            val rightDockID = ImInt(0)
            imgui.internal.ImGui.dockBuilderSplitNode(dockspaceID, ImGuiDir.Left, 0.4f, leftDockID, rightDockID);

            val pLeftNode = imgui.internal.ImGui.dockBuilderGetNode(leftDockID.get())
            val pRightNode = imgui.internal.ImGui.dockBuilderGetNode(rightDockID.get())
            pLeftNode.localFlags = pLeftNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar
            pRightNode.localFlags = pRightNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

            // Dock windows
            imgui.internal.ImGui.dockBuilderDockWindow("File Tree", leftDockID.get())
            imgui.internal.ImGui.dockBuilderDockWindow("Code Editor", rightDockID.get())

            //

            imgui.internal.ImGui.dockBuilderFinish(dockspaceID)
        }

        val dockFlags = if (shouldDrawWindowContents) ImGuiDockNodeFlags.None
        else ImGuiDockNodeFlags.KeepAliveOnly
        val region = ImGui.getContentRegionAvail()
        ImGui.dockSpace(dockspaceID, region.x, region.y, dockFlags, workspaceWindowClass)
        ImGui.end()

        val windowClass = ImGuiWindowClass()
        windowClass.dockNodeFlagsOverrideSet = imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

        ImGui.setNextWindowClass(windowClass)

        ImGui.begin(
            "File Tree",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar
        )
        drawTree(tree)
        ImGui.end()

        ImGui.begin(
            "Code Editor",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar
        )

        if (currentPath.endsWith(".kts")) {
            val engine = ModList.get().getModContainerById("hollowengine").get().modInfo
            val compiler = ModList.get().getModContainerById("kotlinscript").get().modInfo
            ImGui.text("Minecraft ${Minecraft.getInstance().game.version.name} | ${engine.displayName} ${engine.version} | ${compiler.displayName} ${compiler.version}")
            ImGui.sameLine()
            ImGui.setCursorPosX(ImGui.getWindowWidth() - 100f)
            if (ImGui.imageButton("hollowengine:textures/gui/play.png".rl.toTexture().id, 32f, 32f)) {
                RunScriptPacket(currentPath).send()
            }
            if (ImGui.isItemHovered()) ImGui.setTooltip("Запустить скрипт")
            ImGui.sameLine()
            if (ImGui.imageButton("hollowengine:textures/gui/stop.png".rl.toTexture().id, 32f, 32f)) {
                StopScriptPacket(currentPath).send()
            }
            if (ImGui.isItemHovered()) ImGui.setTooltip("Остановить скрипт")
        }

        ImGui.beginTabBar("##Files")
        files.forEach { file ->
            if (ImGui.beginTabItem(file.name, file.open, ImGuiTabItemFlags.None)) {
                if(currentFile != file.name) {
                    editor.text = file.code
                }
                currentFile = file.name
                currentPath = file.path
                editor.render("Code Editor")
                if(shouldClose) {
                    ImGui.setKeyboardFocusHere(-1)
                    shouldClose = false
                }
                if (editor.isTextChanged) {
                    file.code = editor.text.substringBeforeLast("\n")
                    SaveFilePacket(file.path, file.code).send()
                }



                ImGui.endTabItem()
            }
        }
        ImGui.endTabBar()
        ImGui.end()

        drawModalInput()
    }

    fun drawTree(tree: Tree) {
        val flags =
            if (tree.drawArrow) ImGuiTreeNodeFlags.SpanFullWidth else ImGuiTreeNodeFlags.NoTreePushOnOpen or ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.SpanFullWidth

        drawFolderPopup(tree.path)
        drawFilePopup(tree.path)
        var hovered = false
        var ignore = false
        if (ImGui.treeNodeEx(tree.value, flags)) {
            hovered = ImGui.isItemHovered()
            tree.children.forEach { drawTree(it) }

            ignore = true
            if (tree.drawArrow) ImGui.treePop()
        }
        hovered = hovered || (ImGui.isItemHovered() && !ignore)
        if (hovered && ImGui.isMouseClicked(1)) {
            selectedPath = tree.path
            if (tree.drawArrow) ImGui.openPopup("FolderTreePopup##" + tree.path)
            else ImGui.openPopup("FileTreePopup##" + tree.path)
        }

        if (ImGui.isItemActivated() && ImGui.isMouseDoubleClicked(0) && !tree.drawArrow) {
            RequestFilePacket(tree.path).send()
        }
    }

    fun drawFolderPopup(folder: String) {
        if (ImGui.beginPopup("FileTreePopup##$folder")) {
            if (ImGui.menuItem("Переименовать")) {
                inputAction = 0
                inputText = "Введите новое название скрипта:"
                ImGui.closeCurrentPopup()
            }
            if (ImGui.menuItem("Удалить")) {
                inputAction = 1
                inputText = "Вы уверены, что хотите\nудалить этот скрипт?"
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }
    }

    fun drawFilePopup(file: String) {
        if (ImGui.beginPopup("FolderTreePopup##$file")) {
            if (ImGui.menuItem("Создать папку")) {
                inputAction = 2
                inputText = "Введите название папки:"
                ImGui.closeCurrentPopup()
            }

            if (ImGui.menuItem("Создать Сюжетное события")) {
                inputAction = 3
                inputText = "Введите название скрипта:"
                ImGui.closeCurrentPopup()
            }

            if (ImGui.menuItem("Создать Контент-скрипт")) {
                inputAction = 4
                inputText = "Введите название скрипта:"
                ImGui.closeCurrentPopup()
            }
            if (ImGui.menuItem("Создать Мод-скрипт")) {
                inputAction = 5
                inputText = "Введите название скрипта:"
                ImGui.closeCurrentPopup()
            }

            if (ImGui.menuItem("Удалить папку")) {
                inputAction = 6
                inputText = "Вы уверены, что хотите\nудалить эту папку?"
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }
    }

    fun drawModalInput() {
        val center = ImGui.getMainViewport().center
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);

        if (inputAction != -1) {
            ImGui.openPopup("Input")
        }

        if (ImGui.beginPopupModal(
                "Input", ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize or
                        ImGuiWindowFlags.NoTitleBar
            )
        ) {
            ImGui.text(inputText)
            ImGui.separator()

            if (inputAction == 1 || inputAction == 6) {
                if (ImGui.button("Да", 120f, 0f)) {
                    inputAction = -1
                    files.removeIf { it.path.startsWith(selectedPath) }
                    if(selectedPath.isNotEmpty()) DeleteFilePacket(selectedPath).send()
                    ImGui.closeCurrentPopup()
                    input.set("")
                }
                ImGui.sameLine()
                if (ImGui.button("Отмена", 120f, 0f)) {
                    inputAction = -1
                    ImGui.closeCurrentPopup()
                    input.set("")
                }
            } else {
                ImGui.inputText("##Filename", input)

                if (ImGui.button("OK", 120f, 0f)) {
                    val input = input.get()

                    when(inputAction) {
                        0 -> {
                            RenameFilePacket(selectedPath, input).send()
                            files.removeIf { it.path == selectedPath }
                        }
                        2 -> CreateFilePacket("$selectedPath/$input").send()
                        3 -> CreateFilePacket("$selectedPath/$input.se.kts").send()
                        4 -> CreateFilePacket("$selectedPath/$input.content.kts").send()
                        5 -> CreateFilePacket("$selectedPath/$input.mod.kts").send()
                    }

                    inputAction = -1
                    ImGui.closeCurrentPopup()
                    this.input.set("")
                }
                ImGui.sameLine()
                if (ImGui.button("Отмена", 120f, 0f)) {
                    inputAction = -1
                    ImGui.closeCurrentPopup()
                    input.set("")
                }
            }
            ImGui.endPopup()
        }
    }

    fun tree(file: File): Tree {
        val tree = Tree(file.name, file.toReadablePath())
        tree.drawArrow = file.isDirectory
        file.listFiles()?.sortedBy { if (it.isDirectory) 0 else 1 }?.forEach { tree.children.add(tree(it)) }
        return tree
    }

    fun onClose() {
        files.forEach { SaveFilePacket(it.path, it.code).send() }
        files.clear()
    }
}

@Serializable
class Tree(val value: String, val path: String) {
    var drawArrow = true
    val children: MutableList<Tree> = ArrayList()
}

val KOTLIN_LANG = TextEditorLanguageDefinition.c().apply {
    setKeywords(
        arrayOf(
            "break", "continue", "switch", "case", "try",
            "catch", "delete", "do", "while", "else", "finally", "if",
            "else", "for", "is", "as", "in", "instanceof",
            "new", "throw", "typeof", "with", "yield", "when", "return",
            "by", "constructor", "delegate", "dynamic", "field", "get", "set", "init", "value",
            "where", "actual", "annotation", "companion", "field", "external", "infix", "inline", "inner", "internal",
            "open", "operator", "out", "override", "suspend", "vararg",
            "abstract", "extends", "final", "implements", "interface", "super", "throws",
            "data", "class", "fun", "var", "val", "import", "Java", "JSON"
        )
    )

    setName("KotlinScript")

    setSingleLineComment("//")
    setCommentStart("/*")
    setCommentEnd("*/")

    setAutoIdentation(true)
}


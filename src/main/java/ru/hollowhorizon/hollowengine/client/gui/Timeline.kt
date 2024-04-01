package ru.hollowhorizon.hollowengine.client.gui

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.type.ImFloat


object TimelineExample {
    var sMaxTimelineValue: ImFloat = ImFloat()

    fun beginTimeline(strId: String, maxValue: Float): Boolean {
        sMaxTimelineValue.set(maxValue)
        return ImGui.beginChild(strId)
    }

    const val TIMELINE_RADIUS: Float = 6f

    fun timelineEvent(strId: String, values: Array<ImFloat>): Boolean {
        var col = ImGui.getStyle().colors[ImGuiCol.PlotLines]
        val lineColor = ImGui.colorConvertFloat4ToU32(col[0], col[1], col[2], col[3])
        col = ImGui.getStyle().colors[ImGuiCol.Button]
        val inactiveColor = ImGui.colorConvertFloat4ToU32(col[0], col[1], col[2], col[3])
        col = ImGui.getStyle().colors[ImGuiCol.ButtonHovered]
        val activeColor = ImGui.colorConvertFloat4ToU32(col[0], col[1], col[2], col[3])

        var changed = false
        val cursorPos = ImGui.getWindowPos()

        for (i in 0..1) {
            val pos = ImVec2(cursorPos.x, cursorPos.y)
            pos.x += ImGui.getWindowSizeX() * values[i].get() / sMaxTimelineValue.get() + TIMELINE_RADIUS
            pos.y += TIMELINE_RADIUS

            ImGui.setCursorScreenPos(pos.x - TIMELINE_RADIUS, pos.y - TIMELINE_RADIUS)
            ImGui.pushID(i)
            ImGui.invisibleButton(strId, 2 * TIMELINE_RADIUS, 2 * TIMELINE_RADIUS)
            if (ImGui.isItemActive() || ImGui.isItemHovered()) {
                ImGui.setTooltip(values[i].get().toString())
                val a = ImVec2(pos.x, ImGui.getWindowContentRegionMinY() + ImGui.getWindowPosY())
                val b = ImVec2(pos.x, ImGui.getWindowContentRegionMaxY() + ImGui.getWindowPosY())
                ImGui.getWindowDrawList().addLine(a.x, a.y, b.x, b.y, lineColor)
            }
            if (ImGui.isItemActive() && ImGui.isMouseDragging(0)) {
                values[i].set(values[i].get() + ImGui.getIO().mouseDeltaX / ImGui.getWindowSizeX() * sMaxTimelineValue.get())
                changed = true
            }
            ImGui.popID()
            ImGui.getWindowDrawList().addCircleFilled(
                pos.x, pos.y,
                TIMELINE_RADIUS,
                if (ImGui.isItemActive() || ImGui.isItemHovered()) activeColor else inactiveColor
            )
        }

        val start = ImVec2(cursorPos)
        start.x += ImGui.getWindowSizeX() * values[0].get() / sMaxTimelineValue.get() + 2 * TIMELINE_RADIUS
        start.y += TIMELINE_RADIUS / 2
        val end = ImVec2(
            start.x + ImGui.getWindowSizeX() * (values[1].get() - values[0].get()) / sMaxTimelineValue.get() - 2 * TIMELINE_RADIUS,
            start.y + TIMELINE_RADIUS
        )

        ImGui.pushID(-1)
        ImGui.setCursorScreenPos(start.x, start.y)
        ImGui.invisibleButton(strId, end.x - start.x, end.y - start.y)
        if (ImGui.isItemActive() && ImGui.isMouseDragging(0)) {
            values[0].set(values[0].get() + ImGui.getIO().mouseDeltaX / ImGui.getWindowSizeX() * sMaxTimelineValue.get())
            values[1].set(values[1].get() + ImGui.getIO().mouseDeltaX / ImGui.getWindowSizeX() * sMaxTimelineValue.get())
            changed = true
        }
        ImGui.popID()

        ImGui.setCursorScreenPos(cursorPos.x, cursorPos.y + ImGui.getTextLineHeightWithSpacing())
        ImGui.getWindowDrawList().addRectFilled(
            start.x, start.y,
            end.x, end.y,
            if (ImGui.isItemActive() || ImGui.isItemHovered()) activeColor else inactiveColor
        )

        if (values[0].get() > values[1].get()) {
            val tmp = values[0].get()
            values[0].set(values[1].get())
            values[1].set(tmp)
        }
        if (values[1].get() > sMaxTimelineValue.get()) values[1].set(sMaxTimelineValue.get())
        if (values[0].get() < 0) values[0].set(0f)
        return changed
    }

    fun endTimeline() {
        var col = ImGui.getStyle().colors[ImGuiCol.Button]
        val color = ImGui.colorConvertFloat4ToU32(col[0], col[1], col[2], col[3])
        col = ImGui.getStyle().colors[ImGuiCol.Border]
        val lineColor = ImGui.colorConvertFloat4ToU32(col[0], col[1], col[2], col[3])
        col = ImGui.getStyle().colors[ImGuiCol.Text]
        val textColor = ImGui.colorConvertFloat4ToU32(col[0], col[1], col[2], col[3])

        val rounding: Float = ImGui.getStyle().scrollbarRounding

        val min = ImGui.getWindowContentRegionMin()
        val max = ImGui.getWindowContentRegionMax()
        val pos = ImGui.getWindowPos()
        val start = ImVec2(min.x + pos.x, max.y - ImGui.getTextLineHeightWithSpacing() + pos.y)
        val end = ImVec2(max.x + pos.x, max.y + pos.y)

        ImGui.getWindowDrawList().addRectFilled(start.x, start.y, end.x, end.y, color, rounding)

        val LINE_COUNT = 10
        val textOffset = ImVec2(0f, ImGui.getTextLineHeightWithSpacing())
        for (i in 0 until LINE_COUNT) {
            val a = ImVec2(min.x + pos.x + TIMELINE_RADIUS, min.y + pos.y)
            a.x += i * (ImGui.getWindowContentRegionMaxX() - ImGui.getWindowContentRegionMinX()) / LINE_COUNT
            val b = ImVec2(a.x, start.y)
            ImGui.getWindowDrawList().addLine(a.x, a.y, b.x, b.y, lineColor)
            val tmp = String.format("%.2f", i * sMaxTimelineValue.get() / LINE_COUNT)
            ImGui.getWindowDrawList().addText(b.x, b.y, textColor, tmp)
        }

        ImGui.endChild()
    }
}


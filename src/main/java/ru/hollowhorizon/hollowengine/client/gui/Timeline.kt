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

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiMouseCursor
import imgui.type.ImFloat


object Timeline {
    var sMaxTimelineValue: ImFloat = ImFloat()

    fun beginTimeline(strId: String, maxValue: Float): Boolean {
        ImGui.beginChild("$strId::timeline")
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
        val start = ImVec2(min.x + pos.x, min.y + pos.y)
        val end = ImVec2(max.x + pos.x, min.y + pos.y + ImGui.getTextLineHeightWithSpacing())

        ImGui.getWindowDrawList().addRectFilled(start.x, start.y, end.x, end.y, color, 0f)

        val LINE_COUNT = 10
        for (i in 0 until LINE_COUNT) {
            val a = ImVec2(min.x + pos.x + TIMELINE_RADIUS, min.y + pos.y)
            a.x += i * (ImGui.getWindowContentRegionMaxX() - ImGui.getWindowContentRegionMinX()) / LINE_COUNT
            val b = ImVec2(a.x, start.y)
            ImGui.getWindowDrawList().addLine(a.x, a.y, b.x, b.y, lineColor)
            val tmp = String.format("%.2f", i * sMaxTimelineValue.get() / LINE_COUNT)
            ImGui.getWindowDrawList().addText(b.x, b.y, textColor, tmp)
        }
        ImGui.endChild()
        ImGui.setCursorPosY(ImGui.getTextLineHeightWithSpacing() + TIMELINE_RADIUS)

        sMaxTimelineValue.set(maxValue)
        return ImGui.beginChild(strId)
    }

    const val TIMELINE_RADIUS: Float = 16f

    fun timelineEvent(strId: String, values: Array<ImFloat>, r: Float, g: Float, b: Float): Boolean {
        val lineColor = ImGui.colorConvertFloat4ToU32(0.1f, 1f, 0.1f, 0.7f)
        val inactiveColor = ImGui.colorConvertFloat4ToU32(r / 2, g / 2, b / 2, 1f)
        val activeColor = ImGui.colorConvertFloat4ToU32(r, g, b, 1f)

        var changed = false
        val cursorPos = ImGui.getWindowPos() + ImGui.getCursorPos()

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
                ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW)
            }
            if (ImGui.isItemActive() && ImGui.isMouseDragging(0)) {
                values[i].set(values[i].get() + ImGui.getIO().mouseDeltaX / ImGui.getWindowSizeX() * sMaxTimelineValue.get())
                changed = true
            }
            ImGui.popID()
//            ImGui.getWindowDrawList().addCircleFilled(
//                pos.x, pos.y,
//                TIMELINE_RADIUS,
//                if (ImGui.isItemActive() || ImGui.isItemHovered()) activeColor else inactiveColor
//            )
        }

        val start = ImVec2(cursorPos)
        start.x += ImGui.getWindowSizeX() * values[0].get() / sMaxTimelineValue.get() + TIMELINE_RADIUS
        //start.y += TIMELINE_RADIUS / 2
        val end = ImVec2(
            start.x + ImGui.getWindowSizeX() * (values[1].get() - values[0].get()) / sMaxTimelineValue.get(),
            start.y + TIMELINE_RADIUS * 2
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
            start.x + 3f, start.y + 3f,
            end.x + 3f, end.y + 3f,
            ImGui.colorConvertFloat4ToU32(0.5f, 0.5f, 0.5f, 0.3f)
        )
        ImGui.getWindowDrawList().addRectFilled(
            start.x, start.y,
            end.x, end.y,
            activeColor
        )
        ImGui.getWindowDrawList().addRectFilled(
            start.x, end.y - TIMELINE_RADIUS / 2,
            end.x, end.y, inactiveColor
        )

        val color = if (ImGui.isItemActive() || ImGui.isItemHovered()) ImGui.colorConvertFloat4ToU32(
            1f,
            1f,
            1f,
            1f
        ) else ImGui.colorConvertFloat4ToU32(0f, 0f, 0f, 1f)
        ImGui.getWindowDrawList().addRectFilled(
            start.x, start.y,
            end.x, start.y + 2f,
            color
        )
        ImGui.getWindowDrawList().addRectFilled(
            start.x, end.y,
            end.x, end.y - 2f,
            color
        )
        ImGui.getWindowDrawList().addRectFilled(
            start.x, start.y,
            start.x + 2f, end.y,
            color
        )
        ImGui.getWindowDrawList().addRectFilled(
            end.x, start.y,
            end.x - 2f, end.y,
            color
        )

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 3f)
        ImGui.separator()

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
        ImGui.endChild()
    }
}


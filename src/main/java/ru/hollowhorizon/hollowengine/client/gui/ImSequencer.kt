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
import imgui.ImVec4
import imgui.flag.ImDrawFlags
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiMouseCursor
import imgui.type.ImBoolean
import imgui.type.ImInt
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Sequentity {
    private var initialTime = 0
    private var initialEventTime = 0
    private var initialEventLength = 0
    private val indicator_size = ImVec2(9.0f, 9.0f)

    fun draw(registry: Registry<State, Track>) {
        val state = registry.state

        state.pan.x = transition(state.pan.x, state.targetPan.x, CommonTheme.transitionSpeed, 1.0f)
        state.pan.y = transition(state.pan.y, state.targetPan.y, CommonTheme.transitionSpeed, 1.0f)
        state.zoom.x = transition(state.zoom.x, state.targetZoom.x, CommonTheme.transitionSpeed)
        state.zoom.y = transition(state.zoom.y, state.targetZoom.y, CommonTheme.transitionSpeed)

        val painter = ImGui.getWindowDrawList()
        val titlebarHeight = 0f//ImGui.getFontSize() + ImGui.getStyle().framePaddingY * 2 // default: 24
        val windowSize = ImGui.getWindowSize()
        val windowPos = ImGui.getWindowPos() + ImVec2(0.0f, titlebarHeight)
        val padding = ImVec2(7.0f, 2.0f)

        val x = windowPos.clone()
        val y = windowPos.clone() + ImVec2(0.0f, TimelineTheme.height)
        val z = windowPos.clone() + ImVec2(ListerTheme.width, 0.0f)
        val w = windowPos.clone() + ImVec2(ListerTheme.width, TimelineTheme.height)

        val zoom = state.zoom.x / state.stride
        val stride = state.stride * 5  // How many frames to skip drawing
        val minTime = (state.range.x / stride).toInt()
        val maxTime = (state.range.y / stride).toInt()

        val multiplier = zoom / stride

        val timeToPx = { time: Float -> time * multiplier }
        val pxToTime = { px: Float -> px / multiplier }

        val crossBackground = {

            painter.addRectFilled(
                x, x.clone() + ImVec2(ListerTheme.width + 1, TimelineTheme.height),
                ListerTheme.background
            )

            // Border
            painter.addLine(
                x.clone() + ImVec2(ListerTheme.width, 0.0f),
                x.clone() + ImVec2(ListerTheme.width, TimelineTheme.height),
                CommonTheme.dark,
                CommonTheme.borderWidth
            )

            painter.addLine(
                x.clone() + ImVec2(0.0f, TimelineTheme.height),
                x.clone() + ImVec2(ListerTheme.width + 1, TimelineTheme.height),
                CommonTheme.dark,
                CommonTheme.borderWidth
            )
        }

        val listerBackground = {
            if (CommonTheme.bling) {
                // Drop Shadow
                painter.addRectFilled(
                    y,
                    y.clone() + ImVec2(ListerTheme.width + 3.0f, windowSize.y),
                    ImGui.colorConvertFloat4ToU32(0.0f, 0.0f, 0.0f, 0.1f)
                )
                painter.addRectFilled(
                    y,
                    y.clone() + ImVec2(ListerTheme.width + 2.0f, windowSize.y),
                    ImGui.colorConvertFloat4ToU32(0.0f, 0.0f, 0.0f, 0.2f)
                )
            }

            // Fill
            painter.addRectFilled(
                y, y.clone() + ImVec2(ListerTheme.width, windowSize.y),
                ImGui.getStyle().getColor(ImGuiCol.TitleBg)
            )

            // Border
            painter.addLine(
                y.clone() + ImVec2(ListerTheme.width, 0.0f),
                y.clone() + ImVec2(ListerTheme.width, windowSize.y),
                CommonTheme.dark, CommonTheme.borderWidth
            )
        }

        val timelineBackground = {
            if (CommonTheme.bling) {
                // Drop Shadow
                painter.addRectFilled(
                    z.clone(), z.clone() + ImVec2(windowSize.x, TimelineTheme.height + 3.0f),
                    ImVec4(0.0f, 0.0f, 0.0f, 0.1f)
                )
                painter.addRectFilled(
                    z.clone(), z.clone() + ImVec2(windowSize.x, TimelineTheme.height + 2.0f),
                    ImVec4(0.0f, 0.0f, 0.0f, 0.2f)
                )
            }

            // Fill
            painter.addRectFilled(
                z.clone(), z.clone() + ImVec2(windowSize.x, TimelineTheme.height),
                TimelineTheme.background
            )

            // Border
            painter.addLine(
                z.clone() + ImVec2(0.0f, TimelineTheme.height),
                z.clone() + ImVec2(windowSize.x, TimelineTheme.height),
                CommonTheme.dark,
                CommonTheme.borderWidth
            )
        }

        val editorBackground = {
            painter.addRectFilled(
                w,
                w.clone() + windowPos + windowSize,
                EditorTheme.background
            )
        }

        val timeline = {

            for (time in minTime..maxTime) {
                var xMin = time * zoom
                var xMax = 0.0f
                var yMin = 0.0f
                var yMax = TimelineTheme.height - 1

                xMin += z.x + state.pan.x
                xMax += z.x + state.pan.x
                yMin += z.y
                yMax += z.y

                painter.addLine(ImVec2(xMin, yMin), ImVec2(xMin, yMax), TimelineTheme.dark)
                painter.addText(
                    ImGui.getFont(),
                    ImGui.getFontSize() * 0.85f,
                    ImVec2(xMin + 5.0f, yMin),
                    TimelineTheme.text,
                    (time * stride).toString()
                )

                if (time == maxTime) break

                for (j in 0 until 5 - 1) {
                    val innerSpacing = zoom / 5
                    val subline = innerSpacing * (j + 1)
                    painter.addLine(
                        ImVec2(xMin + subline, yMin + (TimelineTheme.height * 0.5f)),
                        ImVec2(xMin + subline, yMax),
                        TimelineTheme.mid
                    )
                }
            }
        }

        val verticalGrid = {
            for (time in minTime..maxTime) {
                var xMin = time * zoom
                var xMax = 0.0f
                var yMin = 0.0f
                var yMax = windowSize.y

                xMin += w.x + state.pan.x
                xMax += w.x + state.pan.x
                yMin += w.y
                yMax += w.y

                painter.addLine(ImVec2(xMin, yMin), ImVec2(xMin, yMax), EditorTheme.dark)

                if (time == maxTime) break

                for (j in 0 until 5 - 1) {
                    val innerSpacing = zoom / 5
                    val subline = innerSpacing * (j + 1)
                    painter.addLine(ImVec2(xMin + subline, yMin), ImVec2(xMin + subline, yMax), EditorTheme.mid)
                }
            }
        }

        var indicatorCount = 0

        val timeIndicator = { time: ImInt, cursorColor: ImVec4, lineColor: ImVec4 ->
            var xMin = time.get() * zoom / stride
            var xMax = 0.0f
            var yMin = TimelineTheme.height
            var yMax = windowSize.y

            xMin += z.x + state.pan.x
            xMax += z.x + state.pan.x
            yMin += z.y
            yMax += z.y

            painter.addLine(ImVec2(xMin, yMin), ImVec2(xMin, yMax), lineColor, 2.0f)

            val size = ImVec2(10.0f, 20.0f)
            val topPos = ImVec2(xMin, yMin)

            ImGui.pushID(indicatorCount)
            val pos = topPos.clone() - ImVec2(size.x, size.y) - ImGui.getWindowPos()
            ImGui.setCursorPos(pos.x, pos.y)
            ImGui.setItemAllowOverlap()
            ImGui.invisibleButton("##indicator", size.x * 2.0f, size.y * 2.0f)
            ImGui.popID()

            if (ImGui.isItemActivated()) initialTime = time.get()

            var color = cursorColor
            if (ImGui.isItemHovered()) {
                ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW)
                color = ImVec4(color.x * 1.2f, color.y * 1.2f, color.z * 1.2f, color.w * 1.2f)
            }

            if (ImGui.isItemActive()) {
                var new = (initialTime + ImGui.getMouseDragDeltaX() / (zoom / stride)).toInt()


                if (TimelineTheme.start_time.imVec4 == color) new = new.coerceAtLeast(0)
                if (TimelineTheme.current_time.imVec4 == color) new = new.coerceIn(0, state.range.y.toInt())

                time.set(new)
            }

            val points = arrayOf(
                topPos.clone(),
                topPos.clone() - ImVec2(-size.x, size.y / 2.0f),
                topPos.clone() - ImVec2(-size.x, size.y),
                topPos.clone() - ImVec2(size.x, size.y),
                topPos.clone() - ImVec2(size.x, size.y / 2.0f)
            )

            val shadow1 = Array(5) {
                points[it].clone() + ImVec2(1.0f, 1.0f)
            }
            val shadow2 = Array(5) {
                points[it].clone() + ImVec2(3.0f, 3.0f)
            }
            painter.addConvexPolyFilled(shadow1, 5, CommonTheme.shadow)
            painter.addConvexPolyFilled(shadow2, 5, CommonTheme.shadow)
            painter.addConvexPolyFilled(points, 5, ImGui.colorConvertFloat4ToU32(color.x, color.y, color.z, color.w))
            painter.addPolyline(points, 5, color * 1.25f, 1.0f)
            painter.addLine(
                topPos.clone() - ImVec2(2.0f, size.y * 0.3f),
                topPos.clone() - ImVec2(2.0f, size.y * 0.8f),
                EditorTheme.accent_dark
            )
            painter.addLine(
                topPos.clone() - ImVec2(-2.0f, size.y * 0.3f),
                topPos.clone() - ImVec2(-2.0f, size.y * 0.8f),
                EditorTheme.accent_dark
            )

            indicatorCount++
        }

        val header = { track: Track, cursor: ImVec2 ->
            val size = ImVec2(windowSize.x, CommonTheme.trackHeight)

            painter.addRectFilled(
                ImVec2(w.x, cursor.y),
                ImVec2(w.x, cursor.y) + size,
                EditorTheme.background
            )

            if (CommonTheme.tintTrack) {
                // Tint empty area with the track color
                painter.addRectFilled(
                    ImVec2(w.x, cursor.y),
                    ImVec2(w.x, cursor.y) + size,
                    track.color.imVec4.apply { this.w = 0.1f }
                )
            }

            painter.addRect(
                ImVec2(w.x, cursor.y),
                ImVec2(w.x, cursor.y) + size,
                EditorTheme.mid
            )

            cursor.y += size.y
        }

        val events = {
            val cursor = ImVec2(w.x + state.pan.x, w.y + state.pan.y)

            registry.view().forEach { track ->
                header(track, cursor)

                // Give each event a unique ImGui ID
                var eventCount = 0

                for ((_, channel) in track.channels) {
                    for (event in channel.events) {
                        val pos = ImVec2(timeToPx(event.time.toFloat()), 0.0f)
                        val size = ImVec2(timeToPx(event.length.toFloat()), state.zoom.y)

                        val headTailSize =
                            ImVec2(
                                min(
                                    EditorTheme.head_tail_handle_width.y,
                                    max(EditorTheme.head_tail_handle_width.x, size.x / 6)
                                ),
                                size.y
                            )
                        val tailPos = ImVec2(pos.x + size.x - headTailSize.x, pos.y)
                        val bodyPos = ImVec2(pos.x + headTailSize.x, pos.y)
                        val bodySize = ImVec2(size.x - 2 * headTailSize.x, size.y)

                        // Transitions
                        var targetHeight = 0.0f



                        ImGui.pushID(track.label)
                        ImGui.pushID(eventCount)

                        /* Event Head Start */
                        val cursorPos = cursor.clone() + pos - ImGui.getWindowPos()
                        ImGui.setCursorPos(cursorPos.x, cursorPos.y)
                        ImGui.setItemAllowOverlap()
                        ImGui.invisibleButton("##event_head", headTailSize.x, headTailSize.y)

                        val headHovered = ImGui.isItemHovered() || ImGui.isItemActive()

                        if (ImGui.isItemActivated()) {
                            initialEventTime = event.time
                            initialEventLength = event.length
                            state.selectedEvent = event
                        }

                        if (!ImGui.getIO().keyAlt && ImGui.isItemActive()) {
                            val delta = ImGui.getMouseDragDelta().x
                            event.time = initialEventTime + pxToTime(delta).toInt()
                            event.length = initialEventLength - pxToTime(delta).toInt()
                            event.removed = (event.time > state.range.y || event.time + event.length < state.range.x)

                            event.enabled = !event.removed
                            targetHeight = EditorTheme.active_clip_raise / 2
                        }
                        /* Event Head End */

                        /* Event Tail Start */
                        cursorPos.set(cursor.clone() + tailPos - ImGui.getWindowPos())
                        ImGui.setCursorPos(cursorPos.x, cursorPos.y)
                        ImGui.setItemAllowOverlap()
                        ImGui.invisibleButton("##event_tail", headTailSize.x, headTailSize.y)

                        val tailHovered = ImGui.isItemHovered() || ImGui.isItemActive()

                        if (ImGui.isItemActivated()) {
                            initialEventTime = event.time
                            initialEventLength = event.length
                            state.selectedEvent = event
                        }

                        if (!ImGui.getIO().keyAlt && ImGui.isItemActive()) {
                            val delta = ImGui.getMouseDragDelta().x
                            event.length = initialEventLength + pxToTime(delta).toInt()
                            event.removed = (event.time > state.range.y || event.time + event.length < state.range.x)

                            event.enabled = !event.removed
                            targetHeight = EditorTheme.active_clip_raise / 2
                        }
                        /* Event Tail End */

                        /* Event Body Start */
                        cursorPos.set(cursor.clone() + bodyPos - ImGui.getWindowPos())
                        ImGui.setCursorPos(cursorPos.x, cursorPos.y)
                        ImGui.setItemAllowOverlap()
                        ImGui.invisibleButton("##event_body", bodySize.x, bodySize.y)

                        ImGui.popID()
                        ImGui.popID()

                        var color = channel.color

                        if (!event.enabled || track.mute.get() || track.notsoloed) {
                            color = hsv(0.0f, 0.0f, 0.5f)
                        } else if (ImGui.isItemHovered() || ImGui.isItemActive()) {
                            ImGui.setMouseCursor(ImGuiMouseCursor.Hand)
                        }

                        // User Input
                        if (ImGui.isItemActivated()) {
                            initialEventTime = event.time
                            initialEventLength = event.length
                            state.selectedEvent = event
                        }

                        if (!ImGui.getIO().keyAlt && ImGui.isItemActive()) {
                            val delta = ImGui.getMouseDragDeltaX()
                            event.time = initialEventTime + pxToTime(delta).toInt()
                            event.removed = (event.time > state.range.y || event.time + event.length < state.range.x)
                            event.enabled = !event.removed
                            targetHeight = EditorTheme.active_clip_raise
                        }
                        /* Event Body End */

                        event.height = transition(event.height, targetHeight, CommonTheme.transitionSpeed)
                        pos.set(pos.x - event.height, pos.y - event.height)
                        tailPos.set(tailPos.x - event.height, tailPos.y - event.height)

                        val shadow = 2
                        val shadowMovement = event.height * (1.0f + EditorTheme.active_clip_raise_shadow_movement)
                        painter.addRectFilled(
                            cursor.clone() + pos + shadow + shadowMovement,
                            cursor.clone() + pos + size + shadow + shadowMovement,
                            hsva(0.0f, 0.0f, 0.0f, 0.3f), EditorTheme.radius
                        )

                        painter.addRectFilled(
                            cursor.clone() + pos,
                            cursor.clone() + pos + size,
                            color
                        )

                        val coli = ImVec4()
                        ImGui.colorConvertU32ToFloat4(color, coli)
                        // Add a dash to the bottom of each event.
                        painter.addRectFilled(
                            cursor.clone() + pos + ImVec2(0.0f, size.y - 5.0f),
                            cursor.clone() + pos + size,
                            coli * 0.8f
                        )

                        if (ImGui.isItemHovered() || ImGui.isItemActive() || headHovered || tailHovered || state.selectedEvent == event) {
                            painter.addRect(
                                cursor.clone() + pos + event.thickness * 0.25f,
                                cursor.clone() + pos + size - event.thickness * 0.25f,
                                EditorTheme.selection,
                                EditorTheme.radius,
                                ImDrawFlags.RoundCornersAll,
                                event.thickness
                            )
                        } else {
                            painter.addRect(
                                cursor.clone() + pos + event.thickness,
                                cursor.clone() + pos + size - event.thickness,
                                EditorTheme.outline, EditorTheme.radius, 0, 0f
                            )
                        }

                        if (headHovered) {
                            painter.addRectFilled(
                                cursor.clone() + pos,
                                cursor.clone() + pos + headTailSize,
                                EditorTheme.head_tail_hover, EditorTheme.radius
                            )
                            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW)
                        }

                        if (tailHovered) {
                            painter.addRectFilled(
                                cursor.clone() + tailPos,
                                cursor.clone() + tailPos + headTailSize,
                                EditorTheme.head_tail_hover, EditorTheme.radius
                            )
                            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW)
                        }


                        if (event.enabled && (ImGui.isItemHovered() || ImGui.isItemActive() || headHovered || tailHovered)) {
                            if (event.length > 5.0f) {
                                painter.addText(
                                    ImGui.getFont(),
                                    ImGui.getFontSize() * 0.85f,
                                    cursor.clone() + pos + ImVec2(3.0f + event.thickness, 0.0f),
                                    EditorTheme.text,
                                    (event.time).toString()
                                )
                            }

                            if (event.length > 30.0f) {
                                painter.addText(
                                    ImGui.getFont(),
                                    ImGui.getFontSize() * 0.85f,
                                    cursor.clone() + pos + ImVec2(size.x - 20.0f, 0.0f),
                                    EditorTheme.text,
                                    (event.length).toString()
                                )
                            }
                        }


                        event.enabled = !(event.time + event.length < 0f || event.time > state.range.y)


                        eventCount++
                    }

                    // Next event type
                    cursor.y += state.zoom.y + EditorTheme.spacing
                }

                // Next track
                cursor.y += padding.y
            }
        }

        val range = {
            val cursor = ImVec2(w.x, w.y)
            val rangeCursorStart = ImVec2(state.range.x * zoom / stride + state.pan.x, TimelineTheme.height)
            val rangeCursorEnd = ImVec2(state.range.y * zoom / stride + state.pan.x, TimelineTheme.height)

            painter.addRectFilled(
                cursor,
                cursor.clone() + ImVec2(rangeCursorStart.x, windowSize.y),
                ImGui.colorConvertFloat4ToU32(0.0f, 0.0f, 0.0f, 0.3f)
            )

            painter.addRectFilled(
                cursor.clone() + ImVec2(rangeCursorEnd.x, 0.0f),
                cursor.clone() + ImVec2(windowSize.x, windowSize.y),
                ImGui.colorConvertFloat4ToU32(0.0f, 0.0f, 0.0f, 0.3f)
            )
        }

        //Size 20f x 20f
        val button: (String, ImBoolean, ImVec2) -> Boolean = { label: String, checked: ImBoolean, size: ImVec2 ->
            if (checked.get()) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImGui.colorConvertFloat4ToU32(0f, 0f, 0f, 0.25f))
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImGui.colorConvertFloat4ToU32(0f, 0f, 0f, 0.15f))
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, ImGui.colorConvertFloat4ToU32(1f, 1f, 1f, 0.1f))
            }

            val pressed = ImGui.button(label, size.x, size.y)

            if (checked.get()) ImGui.popStyleColor(2)
            else ImGui.popStyleColor(1)

            if (pressed) checked.set(checked.get() xor true)

            pressed
        }

        val lister = {
            val cursor = ImVec2(y.x, y.y + state.pan.y)

            registry.view().forEach { track ->
                val textSize = ImGui.calcTextSize(track.label)
                val pos = ImVec2(
                    ListerTheme.width - textSize.x - padding.x - padding.x,
                    CommonTheme.trackHeight / 2.0f - textSize.y / 2.0f
                )

                painter.addRectFilled(
                    cursor.clone() + ImVec2(ListerTheme.width - 5.0f, 0.0f),
                    cursor.clone() + ImVec2(ListerTheme.width, CommonTheme.trackHeight),
                    track.color
                )

                painter.addText(
                    cursor.clone() + pos, ListerTheme.text, track.label
                )

                val cursorPos = cursor.clone() + ImVec2(padding.x, 0.0f) - ImGui.getWindowPos()
                ImGui.setCursorPos(cursorPos.x, cursorPos.y)
                ImGui.pushID(track.label)
                button("m", track.mute, ImVec2(CommonTheme.trackHeight, CommonTheme.trackHeight))
                ImGui.sameLine()

                if (button("s", track.solo, ImVec2(CommonTheme.trackHeight, CommonTheme.trackHeight))) {
                    solo(registry)
                }

                ImGui.popID()

                val trackCorner = cursor.clone()
                cursor.y += CommonTheme.trackHeight

                // Draw channels
                // |
                // |-- channel
                // |-- channel
                // |__ channel
                //
                for ((_, channel) in track.channels) {

                    val indicatorPos = ImVec2(
                        ListerTheme.width - indicator_size.x - padding.x,
                        state.zoom.y * 0.5f - indicator_size.y * 0.5f
                    )

                    painter.addRectFilled(
                        cursor.clone() + indicatorPos,
                        cursor.clone() + indicatorPos + indicator_size,
                        channel.color
                    )

                    val colori = ImVec4()
                    ImGui.colorConvertU32ToFloat4(channel.color, colori)
                    painter.addRect(
                        cursor.clone() + indicatorPos,
                        cursor.clone() + indicatorPos + indicator_size,
                        colori * 1.25f
                    )

                    val labelSize = ImGui.calcTextSize(channel.label) * 0.85f
                    val textPos = ImVec2(
                        ListerTheme.width - labelSize.x - padding.x - indicator_size.x - padding.x,
                        state.zoom.y * 0.5f - labelSize.y * 0.5f
                    )

                    painter.addText(
                        ImGui.getFont(),
                        labelSize.y,
                        cursor.clone() + textPos,
                        ListerTheme.text, channel.label
                    )

                    // Next channel
                    cursor.y += state.zoom.y + EditorTheme.spacing
                }

                // Next track
                cursor.y += padding.y

                // Visualise mute and solo state
                if (track.mute.get() || track.notsoloed) {
                    val faded = ImVec4().apply { ImGui.colorConvertU32ToFloat4(ListerTheme.background, this) }
                    faded.w = 0.8f

                    painter.addRectFilled(
                        trackCorner.clone() + ImVec2(ListerTheme.buttons_width, 0.0f),
                        trackCorner.clone() + ImVec2(ListerTheme.width, cursor.y),
                        faded
                    )
                }
            }
        }

        /**
         * Draw
         *
         */
        editorBackground()
        verticalGrid()
        events()
        timelineBackground()
        timeline()
        range()

        // Can intercept mouse events
        var hoveringBackground = true
        val rangeX = ImInt(state.range.x.toInt())
        val rangeY = ImInt(state.range.y.toInt())
        timeIndicator(rangeX, TimelineTheme.start_time.imVec4, EditorTheme.start_time.imVec4)
        if (ImGui.isItemHovered()) hoveringBackground = false
        timeIndicator(rangeY, TimelineTheme.end_time.imVec4, EditorTheme.end_time.imVec4)
        if (ImGui.isItemHovered()) hoveringBackground = false
        timeIndicator(state.currentTime, TimelineTheme.current_time.imVec4, EditorTheme.current_time.imVec4)
        if (ImGui.isItemHovered()) hoveringBackground = false

        state.range.x = rangeX.get().toFloat()
        state.range.y = rangeY.get().toFloat()

        listerBackground()
        lister()
        crossBackground()

        /**
         * User Input
         *
         */
        if (hoveringBackground) {
            ImGui.setCursorPos(0.0f, titlebarHeight)
            ImGui.invisibleButton("##mpan", ListerTheme.width, TimelineTheme.height)

            if (ImGui.isItemHovered()) ImGui.setMouseCursor(ImGuiMouseCursor.Hand)
            val panM = ImGui.isItemActive() || (ImGui.isWindowFocused() && ImGui.getIO().getMouseDown(1))

            ImGui.setCursorPos(ListerTheme.width, titlebarHeight)
            ImGui.invisibleButton("##pan[0]", windowSize.x, TimelineTheme.height)

            if (ImGui.isItemHovered()) ImGui.setMouseCursor(ImGuiMouseCursor.ResizeEW)
            val panH = ImGui.isItemActive()

            ImGui.setCursorPos(ListerTheme.width - 110.0f, TimelineTheme.height + titlebarHeight)
            ImGui.invisibleButton("##pan[1]", ListerTheme.width, windowSize.y)

            if (ImGui.isItemHovered()) ImGui.setMouseCursor(ImGuiMouseCursor.ResizeNS)
            val panV = ImGui.isItemActive()

            when {
                panM -> {
                    state.targetPan.x += ImGui.getIO().mouseDelta.x
                    state.targetPan.y += ImGui.getIO().mouseDelta.y
                }
                panV -> state.targetPan.y += ImGui.getIO().mouseDelta.y
                panH -> state.targetPan.x += ImGui.getIO().mouseDelta.x
            }

            val wheel = ImGui.getIO().mouseWheel
            if (wheel != 0.0f && ImGui.getIO().keyAlt) {
                if (ImGui.getIO().keyCtrl) {
                    state.targetZoom.y += wheel * 10f
                    state.targetZoom.y = state.targetZoom.y.coerceIn(10f, 250f)
                } else {
                    state.targetZoom.x += wheel * 10f
                    state.targetZoom.x = state.targetZoom.x.coerceIn(30f, 500f)
                }
            }
        }
    }

    private fun solo(registry: Registry<State, Track>) {
        var anySolo = false
        registry.view().forEach { track ->
            if (track.solo.get()) anySolo = true
            track.notsoloed = false
        }

        if (anySolo) registry.view().forEach { track ->
            track.notsoloed = !track.solo.get()
        }
    }
}

private fun transition(current: Float, target: Float, velocity: Float, epsilon: Float = 0.1f): Float {
    var mCurrent = current
    val delta = target - current

    // Prevent transitions between too small values
    // (Especially damaging to fonts)
    if (abs(delta) < epsilon) mCurrent = target

    mCurrent += delta * velocity
    return mCurrent
}

class State {
    val currentTime = ImInt(0)
    val range = ImVec2(0f, 100f)

    var selectedEvent: Event? = null
    var selectedTrack: Track? = null
    var selectedChannel: Channel? = null

    // Visual
    val zoom = ImVec2(250.0f, 20.0f)
    val pan = ImVec2(8.0f, 8.0f)
    val stride = 2

    // Transitions
    val targetZoom = ImVec2(200.0f, 20.0f)
    val targetPan = ImVec2(15.0f, 20.0f)
}

class Track {
    var label = "Untitled track"
    val color = hsv(0.66f, 0.5f, 1.0f)
    val solo = ImBoolean(false)
    val mute = ImBoolean(false)
    val channels = HashMap<EventType, Channel>()
    internal var notsoloed = false
}

class Channel {
    var label = "Untitled channel"
    var color = hsv(0.33f, 0.5f, 1.0f)
    val type = EventType.MOVE
    val events = ArrayList<Event>()
}

class Event {
    var time = 0
    var length = 0
    val color = hsv(0f, 0f, 1f)
    val type = EventType.MOVE
    var enabled = true
    var removed = false
    val scale = 1.0f
    var height = 0.0f
    var thickness = 0.0f
}

enum class EventType {
    MOVE, ROTATE, SCALE
}

object CommonTheme {
    val dark = hsv(0.0f, 0.0f, 0.3f)
    val shadow = hsva(0.0f, 0.0f, 0.0f, 0.1f)

    var bling = true
    var tintTrack = true

    var borderWidth = 1.0f
    var trackHeight = 25.0f
    var transitionSpeed = 0.2f
}

object TimelineTheme {
    val background = hsv(0.0f, 0.0f, 0.250f)
    val text = hsv(0.0f, 0.0f, 0.850f)
    val dark = hsv(0.0f, 0.0f, 0.322f)
    val mid = hsv(0.0f, 0.0f, 0.314f)
    val start_time = hsv(0.33f, 0.0f, 0.50f)
    val current_time = hsv(0.57f, 0.62f, 0.99f)
    val end_time = hsv(0.0f, 0.0f, 0.40f)

    var height = 40.0f
}

object ListerTheme {
    val background = hsv(0.0f, 0.0f, 0.188f)
    val text = hsv(0.0f, 0.0f, 0.850f)
    var width = 220.0f
    var buttons_width = 90.0f
}

object EditorTheme {
    val background = hsv(0.0f, 0.00f, 0.651f)
    val text = hsv(0.0f, 0.00f, 0.950f)
    val mid = hsv(0.0f, 0.00f, 0.600f)
    val dark = hsv(0.0f, 0.00f, 0.498f)
    val accent_dark = hsva(0.0f, 0.0f, 0.0f, 0.1f)
    val selection = hsv(0.0f, 0.0f, 1.0f)
    val outline = hsv(0.0f, 0.0f, 0.1f)
    val head_tail_hover = hsva(0.0f, 0.0f, 1.0f, 0.25f)

    val start_time = hsv(0.33f, 0.0f, 0.25f)
    val current_time = hsv(0.6f, 0.5f, 0.5f)
    val end_time = hsv(0.0f, 0.0f, 0.25f)

    var radius = 0.0f
    var spacing = 1.0f
    val head_tail_handle_width = ImVec2(10.0f, 100.0f)
    var active_clip_raise = 5.0f
    var active_clip_raise_shadow_movement = 0.25f

}

class Registry<K, V>(val state: K) {

    val values = ArrayList<V>()

    fun view(): List<V> {
        return values
    }
}
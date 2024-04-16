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

package ru.hollowhorizon.hollowengine.client.gui.widget

import imgui.ImGui
import imgui.ImVec2
import imgui.callback.ImGuiInputTextCallback
import imgui.flag.*
import imgui.type.ImString

class ComboFilterState {
    var activeIdx = 0 // Index of currently 'active' item by use of up/down keys
    var selectionChanged = false // Flag to help focus the correct item when selecting active item
    var isOpen = false
    var keyboardFocus = false
}

object ImInputCombo {
    fun inputCombo(label: String, text: ImString, state: ComboFilterState, vararg items: String): Boolean {
        ImGui.setItemAllowOverlap()
        if(state.keyboardFocus) {
            ImGui.setKeyboardFocusHere(0)
            state.keyboardFocus = false
        }
        var done =
            ImGui.inputText(label, text, ImGuiInputTextFlags.AutoSelectAll)

        val activated = ImGui.isItemActivated()
        state.isOpen = state.isOpen or ImGui.isItemHovered() || ImGui.isItemActive()

        val focused = ImGui.isItemHovered() || ImGui.isItemActive()

        if (state.isOpen) {
            items.sortByDescending { score(text.get(), it) }
            val new_idx = search(text.get(), *items)
            val idx = if (new_idx >= 0) new_idx else state.activeIdx
            state.selectionChanged = state.activeIdx != idx;
            //state.activeIdx = idx

            if(ImGui.isKeyPressed(ImGui.getIO().getKeyMap(ImGuiKey.DownArrow))) {
                state.activeIdx = (state.activeIdx + 1) % items.size
                state.keyboardFocus = true
            } else if(ImGui.isKeyPressed(ImGui.getIO().getKeyMap(ImGuiKey.UpArrow))) {
                state.activeIdx = (state.activeIdx - 1 + items.size) % items.size
                state.keyboardFocus = true
            } else if(ImGui.isKeyPressed(ImGui.getIO().getKeyMap(ImGuiKey.Enter))) {
                done = true
                val i = state.activeIdx
                text.set(items[i])
                state.keyboardFocus = false
            }

            //ImGui.setNextWindowFocus()
            val popup = drawPopup(state, idx, *items)
            state.isOpen = !popup && !done && focused
        }
        return done;
    }

    private fun drawPopup(state: ComboFilterState, START: Int, vararg items: String): Boolean {
        var clicked = false

        // Grab the position for the popup
        val pos = ImGui.getItemRectMin()
        pos.y += ImGui.getItemRectSize().y
        val size = ImVec2(ImGui.getItemRectSize().x - 60, ImGui.getTextLineHeightWithSpacing() * 4);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);

        val flags = ImGuiWindowFlags.NoTitleBar or
                ImGuiWindowFlags.NoResize or
                ImGuiWindowFlags.NoMove or
                ImGuiWindowFlags.HorizontalScrollbar or
                ImGuiWindowFlags.NoSavedSettings or
                0; //ImGuiWindowFlags_ShowBorders

        //ImGui.setNextWindowFocus()

        ImGui.setNextWindowPos(pos.x, pos.y)
        ImGui.setNextWindowSize(size.x, size.y)
        ImGui.beginTooltip()

        ImGui.pushAllowKeyboardFocus(true)

        for (i in items.indices) {
            val isIndexActive = state.activeIdx == i

            if (isIndexActive) {
                ImGui.pushStyleColor(ImGuiCol.Border, 1f, 1f, 0f, 1f)
            }

            ImGui.pushID(i);
            if (ImGui.selectable(items[i], isIndexActive)) {
                // And item was clicked, notify the input
                // callback so that it can modify the input buffer
                state.activeIdx = i
                clicked = true
            }
            if (ImGui.isItemFocused() && ImGui.isKeyPressed(ImGui.getIO().getKeyMap(ImGuiKey.Enter))) {
                // Allow ENTER key to select current highlighted item (w/ keyboard navigation)
                state.activeIdx = i
                clicked = true
            }
            ImGui.popID()

            if (isIndexActive) {
                if (state.selectionChanged) {
                    // Make sure we bring the currently 'active' item into view.
                    ImGui.setScrollHereX()
                    ImGui.setScrollHereY()
                    state.selectionChanged = false
                }

                ImGui.popStyleColor(1)
            }
        }

        ImGui.popAllowKeyboardFocus()
        ImGui.endTooltip()
        ImGui.popStyleVar(1)

        return clicked
    }

    fun score(str1: String, str2: String): Int {
        var score = 0
        var consecutive = 0
        var maxErrors = 0

        var i1 = 0
        var i2 = 0

        while (i1 < str1.length && i2 < str2.length) {
            val isLeading = (str1[i1].code and 64) != 0 && (i1 == 0 || (str1[i1 - 1].code and 64) == 0)
            if ((str1[i1].toInt() and 0xFFDF) == (str2[i2].toInt() and 0xFFDF)) {
                val hadSeparator = i1 > 0 && str1[i1 - 1] <= ' '
                val x = if (hadSeparator || isLeading) 10 else consecutive * 5
                consecutive = 1
                score += x
                i2++
            } else {
                val x = -1
                val y = if (isLeading) -3 else 0
                consecutive = 0
                score += x
                maxErrors += y
            }
            i1++
        }

        return score + if (maxErrors < -9) -9 else maxErrors
    }

    fun search(str: String, vararg words: String): Int {
        var scoreMax = 0
        var best = -1

        for (i in words.indices) {
            val score = score(words[i], str)
            val record = score >= scoreMax
            val draw = score == scoreMax

            if (record) {
                scoreMax = score
                best = if (!draw) {
                    i
                } else {
                    if (best >= 0 && words[best].length < words[i].length) best else i
                }
            }
        }

        return best
    }
}

fun main() {
    println(ImInputCombo.search("Hollo", "Hello", "Hollow", "Horizon", "F#fav", "fafaf"))
}
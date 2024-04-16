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

package ru.hollowhorizon.hollowengine.common.data

import com.google.gson.JsonObject
import net.minecraft.Util
import net.minecraft.server.packs.FolderPackResources
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager
import java.io.InputStream

object HollowStoryPack : FolderPackResources(DirectoryManager.HOLLOW_ENGINE) {

    private val PACK_META_BYTES = Util.make(JsonObject()) { json ->
        json.add("pack", JsonObject().apply {
            addProperty("description", "HollowEngine Folder Resources")
            addProperty("pack_format", 9)
        })
    }.toString()

    override fun getResource(pResourcePath: String): InputStream {
        return when (pResourcePath) {
            PACK_META -> PACK_META_BYTES.byteInputStream()
            else -> super.getResource(pResourcePath)
        }
    }

    override fun hasResource(pResourcePath: String): Boolean {
        return pResourcePath == PACK_META || super.hasResource(pResourcePath)
    }
}
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

package ru.hollowhorizon.hollowengine.common.scripting.content

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.server.ServerLifecycleHooks
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hollowengine.mixins.RecipeManagerAccessor
import ru.hollowhorizon.kotlinscript.common.scripting.ScriptingCompiler
import ru.hollowhorizon.kotlinscript.common.scripting.errors
import ru.hollowhorizon.kotlinscript.common.scripting.kotlin.AbstractHollowScriptConfiguration
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.loadDependencies
import kotlin.script.experimental.jvm.util.isError

@KotlinScript(
    displayName = "Content Script",
    fileExtension = "content.kts",
    compilationConfiguration = ContentScriptConfiguration::class
)
abstract class ContentScript(
    recipes: MutableMap<RecipeType<*>, MutableMap<ResourceLocation, Recipe<*>>>,
    byName: MutableMap<ResourceLocation, Recipe<*>>
) : ContentScriptBase(recipes, byName)

fun runContentScript(recipeManager: RecipeManagerAccessor, script: File) {
    HollowCore.LOGGER.info("[ContentScriptCompiler]: loading script \"${script.name}\"")

    val result = ScriptingCompiler.compileFile<ContentScript>(script)

    val creativePlayers = ServerLifecycleHooks.getCurrentServer()?.playerList?.players?.filter { it.abilities.instabuild }

    result.errors?.let { errors ->
        errors.forEach { error ->
            creativePlayers?.forEach {
                it.sendSystemMessage("§c[ERROR]§r $error".mcText)
            } ?: HollowCore.LOGGER.error("[ContentScriptCompiler]: $error")
        }
        return
    }

    HollowCore.LOGGER.info("[RecipeScriptCompiler]: Script compiled: \"${result}\"")

    val recipes = recipeManager.`hollowcore$getRecipes`().toMutableMap()
    val byName = recipeManager.`hollowcore$getByName`().toMutableMap()

    recipes.keys.forEach {
        recipes[it] = recipes[it]?.toMutableMap() ?: hashMapOf()
    }

    val res = result.execute {
        jvm {
            constructorArgs(recipes, byName)
            loadDependencies(false)
        }
    }

    if(res.isError()) {
        (res as ResultWithDiagnostics.Failure).errors().let { errors ->
            errors.forEach { error ->
                creativePlayers?.forEach {
                    it.sendSystemMessage("§c[ERROR]§r $error".mcText)
                } ?: HollowCore.LOGGER.error("[ModScriptCompiler]: $error")
            }
            return
        }
    }

    recipeManager.`hollowcore$setRecipes`(recipes)
    recipeManager.`hollowcore$setByName`(byName)

    HollowCore.LOGGER.info("[RecipeScriptCompiler]: Script evaluated: \"${res}\"")
}

class ContentScriptConfiguration : AbstractHollowScriptConfiguration({
    defaultImports(
        "ru.hollowhorizon.hollowengine.common.recipes.*",
        "ru.hollowhorizon.hollowengine.common.scripting.*",
        "ru.hollowhorizon.hc.client.utils.*"
    )

    baseClass(ContentScriptBase::class)
})

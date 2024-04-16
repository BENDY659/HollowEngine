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

package ru.hollowhorizon.hollowengine.common.recipes

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CampfireCookingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeType

object Campfire {
    fun addRecipe(
        output: ItemStack,
        input: Ingredient,
        group: String = "",
        experience: Float = 0f,
        cookingTime: Int = 200
    ) {
        val recipe =
            CampfireCookingRecipe(RecipeHelper.createRecipeId(), group, input, output, experience, cookingTime)
        RecipeHelper.currentScript?.addRecipe(recipe)
    }

    fun replaceRecipe(
        output: ItemStack,
        input: Ingredient,
        group: String = "",
        experience: Float = 0f,
        cookingTime: Int = 200
    ) {
        removeRecipe(output)
        addRecipe(output, input, group, experience, cookingTime)
    }

    fun addRecipe(
        output: ItemStack,
        input: ItemStack,
        group: String = "",
        experience: Float = 0f,
        cookingTime: Int = 200
    ) = addRecipe(output, Ingredient.of(input), group, experience, cookingTime)

    fun addRecipe(
        output: ItemStack,
        input: TagKey<Item>,
        group: String = "",
        experience: Float = 0f,
        cookingTime: Int = 200
    ) = addRecipe(output, Ingredient.of(input), group, experience, cookingTime)

    fun replaceRecipe(
        output: ItemStack,
        input: ItemStack,
        group: String = "",
        experience: Float = 0f,
        cookingTime: Int = 200
    ) = replaceRecipe(output, Ingredient.of(input), group, experience, cookingTime)

    fun replaceRecipe(
        output: ItemStack,
        input: TagKey<Item>,
        group: String = "",
        experience: Float = 0f,
        cookingTime: Int = 200
    ) = replaceRecipe(output, Ingredient.of(input), group, experience, cookingTime)

    fun removeRecipe(output: ItemStack) {
        RecipeHelper.currentScript?.removeByOutput(output, RecipeType.CAMPFIRE_COOKING)
    }
}
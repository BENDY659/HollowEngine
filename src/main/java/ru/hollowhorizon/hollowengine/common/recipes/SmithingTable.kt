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
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.UpgradeRecipe

object SmithingTable {
    fun addRecipe(result: ItemStack, input: Ingredient, addition: Ingredient) {
        RecipeHelper.currentScript?.addRecipe(
            UpgradeRecipe(RecipeHelper.createRecipeId(), input, addition, result)
        )
    }

    fun addRecipe(result: ItemStack, input: ItemStack, addition: ItemStack) =
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))

    fun addRecipe(result: ItemStack, input: TagKey<Item>, addition: TagKey<Item>) =
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))

    fun addRecipe(result: ItemStack, input: TagKey<Item>, addition: ItemStack) =
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))

    fun addRecipe(result: ItemStack, input: ItemStack, addition: TagKey<Item>) =
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))

    fun replaceRecipe(result: ItemStack, input: ItemStack, addition: ItemStack) {
        removeRecipe(result)
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))
    }

    fun replaceRecipe(result: ItemStack, input: TagKey<Item>, addition: TagKey<Item>) {
        removeRecipe(result)
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))
    }

    fun replaceRecipe(result: ItemStack, input: TagKey<Item>, addition: ItemStack) {
        removeRecipe(result)
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))
    }

    fun replaceRecipe(result: ItemStack, input: ItemStack, addition: TagKey<Item>) {
        removeRecipe(result)
        addRecipe(result, Ingredient.of(input), Ingredient.of(addition))
    }

    fun removeRecipe(result: ItemStack, checkTag: Boolean = false) {
        RecipeHelper.currentScript?.removeByOutput(result, RecipeType.SMITHING, checkTag)
    }
}
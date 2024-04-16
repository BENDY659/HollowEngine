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

import net.minecraft.core.NonNullList
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.item.crafting.ShapelessRecipe

class CraftingTable(val isShapeless: Boolean = false) {
    val grid = Grid()
    val context = ItemContext(this)

    companion object {
        fun shaped(item: ItemStack, group: String = "", craft: CraftingTable.() -> Unit) {
            val craft = CraftingTable().apply(craft)
            val grid = craft.grid.grid
            val width = grid.maxBy { it.length }.length
            val height = grid.size

            val recipe =
                ShapedRecipe(RecipeHelper.createRecipeId(), group, width, height, craft.context.assembly(), item)
            RecipeHelper.currentScript?.addRecipe(recipe)
        }

        fun replaceShaped(item: ItemStack, group: String = "", craft: CraftingTable.() -> Unit) {
            RecipeHelper.currentScript?.removeByOutput(item, RecipeType.CRAFTING)
            shaped(item, group, craft)
        }

        fun shapeless(item: ItemStack, group: String = "", craft: CraftingTable.() -> Unit) {
            val craft = CraftingTable(true).apply(craft)
            val recipe = ShapelessRecipe(RecipeHelper.createRecipeId(), group, item, craft.context.assembly())
            RecipeHelper.currentScript?.addRecipe(recipe)
        }

        fun replaceShapeless(item: ItemStack, group: String = "", craft: CraftingTable.() -> Unit) {
            RecipeHelper.currentScript?.removeByOutput(item, RecipeType.CRAFTING)
            shapeless(item, group, craft)
        }

        fun removeByOutput(output: ItemStack, checkTag: Boolean = false) {
            RecipeHelper.currentScript?.removeByOutput(output, RecipeType.CRAFTING, checkTag)
        }
    }

    val extra = hashMapOf<String, Any>()

    fun where(context: ItemContext.() -> Unit) {
        this.context.apply(context)
    }

    fun items(vararg items: Ingredient) {
        this.context.ingredients.addAll(items)
    }

    fun items(vararg items: ItemStack) {
        this.context.ingredients.addAll(items.map { Ingredient.of(it) })
    }

    fun items(vararg tags: TagKey<Item>) {
        this.context.ingredients.addAll(tags.map { Ingredient.of(it) })
    }

    fun items(vararg items: Any) {
        this.context.ingredients.addAll(items.map {
            when (it) {
                is TagKey<*> -> Ingredient.of(it as TagKey<Item>)
                is ItemStack -> Ingredient.of(it)
                else -> throw IllegalStateException("Not item reference in CraftingTable.items() method!")
            }
        })
    }

    inner class Grid {
        val grid = ArrayList<String>()

        operator fun invoke(vararg rows: String) {
            var last = -1
            if (!rows.all {
                    if (last == -1) last = it.length
                    it.length == last && it.length < 4
                } || rows.size > 3) throw IllegalStateException("All grid rows must have the same length and be at most 3!")


            grid += rows
        }
    }

    inner class ItemContext(val table: CraftingTable) {
        val items = hashMapOf<Char, Ingredient>(
            ' ' to Ingredient.EMPTY
        )
        val ingredients = NonNullList.create<Ingredient>()

        operator fun Char.minus(item: ItemStack) {
            this - Ingredient.of(item)
        }

        operator fun Char.minus(item: Ingredient) {
            items[this] = item
        }

        fun assembly(): NonNullList<Ingredient> {
            val list = NonNullList.create<Ingredient>()

            if (!isShapeless) {
                grid.grid.forEach {
                    it.forEach { char ->
                        list.add(this.items[char] ?: throw IllegalStateException("Item '$char' not found!"))
                    }
                }
            } else list.addAll(items.values + ingredients)
            return list
        }
    }
}

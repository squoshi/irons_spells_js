package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRecipeRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

@SuppressWarnings("unused")
public class AlchemistCauldronKubeJSRecipes {
    private static AlchemistCauldronRecipe addAlchemistCauldronRecipe(ItemStack input, ItemStack ingredient, ItemStack result) {
        AlchemistCauldronRecipe recipe = new AlchemistCauldronRecipe(input, ingredient, result);
        AlchemistCauldronRecipeRegistry.addRecipe(recipe);
        return recipe;
    }

    private static AlchemistCauldronRecipe addAlchemistCauldronRecipe(Potion input, ItemStack ingredient, ItemStack result) {
        AlchemistCauldronRecipe recipe = new AlchemistCauldronRecipe(input, ingredient.getItem(), result.getItem());
        AlchemistCauldronRecipeRegistry.addRecipe(recipe);
        return recipe;
    }

    @Info(value = """
        Creates a new Alchemist Cauldron recipe. Used in StartupEvents.postInit
    """)
    public static class AlchemistCauldronRecipeBuilder {
        private ItemStack input;
        private ItemStack ingredient;
        private ItemStack result;
        private Potion potionInput;
        private int baseRequirement = 1;
        private int resultLimit = 4;

        public static AlchemistCauldronRecipeBuilder create() {
            return new AlchemistCauldronRecipeBuilder();
        }

        public AlchemistCauldronRecipeBuilder setInput(ItemStack input) {
            this.input = input;
            return this;
        }

        public AlchemistCauldronRecipeBuilder setIngredient(ItemStack ingredient) {
            this.ingredient = ingredient;
            return this;
        }

        public AlchemistCauldronRecipeBuilder setResult(ItemStack result) {
            this.result = result;
            return this;
        }

        public AlchemistCauldronRecipeBuilder setPotionInput(Potion potionInput) {
            this.potionInput = potionInput;
            return this;
        }

        public AlchemistCauldronRecipeBuilder setBaseRequirement(int i) {
            this.baseRequirement = i;
            return this;
        }

        public AlchemistCauldronRecipeBuilder setResultLimit(int i) {
            this.resultLimit = i;
            return this;
        }

        public AlchemistCauldronRecipe register() {
            if (input != null && ingredient != null && result != null) {
                return AlchemistCauldronKubeJSRecipes.addAlchemistCauldronRecipe(input, ingredient, result).setBaseRequirement(baseRequirement).setResultLimit(resultLimit);
            } else if (potionInput != null && ingredient != null && result != null) {
                return AlchemistCauldronKubeJSRecipes.addAlchemistCauldronRecipe(potionInput, ingredient, result).setBaseRequirement(baseRequirement).setResultLimit(resultLimit);
            } else {
                throw new IllegalArgumentException("Invalid recipe parameters");
            }
        }
    }
}
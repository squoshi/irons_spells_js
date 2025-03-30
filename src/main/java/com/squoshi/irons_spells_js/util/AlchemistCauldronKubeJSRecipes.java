package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRecipeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

@SuppressWarnings("unused")
public class AlchemistCauldronKubeJSRecipes {
    private static AlchemistCauldronRecipe addAlchemistCauldronRecipe(ResourceLocation id, ItemStack input, ItemStack ingredient, ItemStack result) {
        AlchemistCauldronRecipe recipe = new AlchemistCauldronRecipe(input, ingredient, result);
        AlchemistCauldronRecipeRegistry.registerRecipe(id, recipe);
        return recipe;
    }

    private static AlchemistCauldronRecipe addAlchemistCauldronRecipe(ResourceLocation id, Potion input, ItemStack ingredient, ItemStack result) {
        AlchemistCauldronRecipe recipe = new AlchemistCauldronRecipe(input, ingredient.getItem(), result.getItem());
        AlchemistCauldronRecipeRegistry.registerRecipe(id, recipe);
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
        private ResourceLocation id;

        private AlchemistCauldronRecipeBuilder(ResourceLocation id) {
            this.id = id;
        }

        public static AlchemistCauldronRecipeBuilder create(ResourceLocation id) {
            return new AlchemistCauldronRecipeBuilder(id);
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
            if (input != null && ingredient != null && result != null && id != null) {
                return AlchemistCauldronKubeJSRecipes.addAlchemistCauldronRecipe(id, input, ingredient, result).setBaseRequirement(baseRequirement).setResultLimit(resultLimit);
            } else if (potionInput != null && ingredient != null && result != null && id != null) {
                return AlchemistCauldronKubeJSRecipes.addAlchemistCauldronRecipe(id, potionInput, ingredient, result).setBaseRequirement(baseRequirement).setResultLimit(resultLimit);
            } else {
                throw new IllegalArgumentException("Invalid recipe parameters");
            }
        }
    }
}
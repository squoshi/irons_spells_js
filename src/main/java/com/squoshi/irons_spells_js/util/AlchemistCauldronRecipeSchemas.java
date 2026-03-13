package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * KubeJS recipe schemas for the Iron's Spellbooks alchemist cauldron recipe types.
 * <p>
 * These allow users to create alchemist cauldron recipes in {@code ServerEvents.recipes}:
 * <pre>{@code
 * ServerEvents.recipes(event => {
 *     // Brew recipe
 *     let brew = event.recipes.irons_spellbooks.alchemist_cauldron_brew
 *     brew(
 *         [Fluid.of("milk", 500)],            // result fluids
 *         "minecraft:white_terracotta",        // reagent input
 *         Fluid.of("water", 1000)              // base fluid
 *     )
 *
 *     // Empty recipe (fluid from cauldron -> item)
 *     let empty = event.recipes.irons_spellbooks.alchemist_cauldron_empty
 *     empty(
 *         "minecraft:white_concrete",          // result item
 *         "minecraft:dirt",                     // input item
 *         Fluid.of("milk", 250)                // fluid consumed from cauldron
 *     )
 *
 *     // Fill recipe (item -> fluid into cauldron)
 *     let fill = event.recipes.irons_spellbooks.alchemist_cauldron_fill
 *     fill(
 *         Fluid.of("milk", 1000),              // fluid added to cauldron
 *         "minecraft:milk_bucket",             // input item
 *         "minecraft:bucket"                    // result item
 *     )
 * })
 * }</pre>
 */
public interface AlchemistCauldronRecipeSchemas {

    // ── Brew ────────────────────────────────────────────────────────────
    RecipeKey<InputFluid> BREW_BASE_FLUID = FluidComponents.INPUT.key("base_fluid");
    RecipeKey<InputItem> BREW_INPUT = ItemComponents.INPUT.key("input");
    RecipeKey<OutputFluid[]> BREW_RESULTS = FluidComponents.OUTPUT_ARRAY.key("results");
    RecipeKey<OutputItem> BREW_BYPRODUCT = ItemComponents.OUTPUT.key("byproduct").defaultOptional().allowEmpty();

    RecipeSchema BREW = new RecipeSchema(
            ForgeFluidRecipeJS.class, ForgeFluidRecipeJS::new,
            BREW_RESULTS, BREW_INPUT, BREW_BASE_FLUID, BREW_BYPRODUCT
    );

    // ── Fill (item → fluid into cauldron) ───────────────────────────────
    RecipeKey<InputItem> FILL_INPUT = ItemComponents.INPUT.key("input");
    RecipeKey<OutputItem> FILL_RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<OutputFluid> FILL_FLUID = FluidComponents.OUTPUT.key("fluid");
    RecipeKey<Boolean> FILL_MUST_FIT_ALL = BooleanComponent.BOOLEAN.key("mustFitAll").optional(true);
    RecipeKey<String> FILL_SOUND = StringComponent.ANY.key("sound").defaultOptional();

    RecipeSchema FILL = new RecipeSchema(
            ForgeFluidRecipeJS.class, ForgeFluidRecipeJS::new,
            FILL_FLUID, FILL_INPUT, FILL_RESULT, FILL_MUST_FIT_ALL, FILL_SOUND
    );

    // ── Empty (fluid from cauldron → item) ──────────────────────────────
    RecipeKey<InputItem> EMPTY_INPUT = ItemComponents.INPUT.key("input");
    RecipeKey<OutputItem> EMPTY_RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<InputFluid> EMPTY_FLUID = FluidComponents.INPUT.key("fluid");
    RecipeKey<String> EMPTY_SOUND = StringComponent.ANY.key("sound").defaultOptional();

    RecipeSchema EMPTY = new RecipeSchema(
            ForgeFluidRecipeJS.class, ForgeFluidRecipeJS::new,
            EMPTY_RESULT, EMPTY_INPUT, EMPTY_FLUID, EMPTY_SOUND
    );
}
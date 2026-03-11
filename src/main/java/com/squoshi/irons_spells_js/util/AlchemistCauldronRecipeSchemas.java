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
 * // Brew recipe
 * ServerEvents.recipes(event => {
 *     event.recipes.irons_spellbooks.alchemist_cauldron_brew(
 *         Fluid.of('irons_spellbooks:rare_ink', 1000),  // base fluid
 *         'forge:ingots/gold',                            // reagent input
 *         [Fluid.of('irons_spellbooks:epic_ink', 250)]    // results
 *     )
 *
 *     // Fill recipe (item -> fluid into cauldron)
 *     event.recipes.irons_spellbooks.alchemist_cauldron_fill(
 *         'irons_spellbooks:blood_vial',                  // input item
 *         'minecraft:glass_bottle',                        // returned item
 *         Fluid.of('irons_spellbooks:blood', 250)          // fluid added to cauldron
 *     )
 *
 *     // Empty recipe (fluid from cauldron -> item)
 *     event.recipes.irons_spellbooks.alchemist_cauldron_empty(
 *         'minecraft:glass_bottle',                        // input item
 *         'irons_spellbooks:blood_vial',                   // result item
 *         Fluid.of('irons_spellbooks:blood', 250)          // fluid consumed from cauldron
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
            BREW_BASE_FLUID, BREW_INPUT, BREW_RESULTS, BREW_BYPRODUCT
    );

    // ── Fill (item → fluid into cauldron) ───────────────────────────────
    RecipeKey<InputItem> FILL_INPUT = ItemComponents.INPUT.key("input");
    RecipeKey<OutputItem> FILL_RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<OutputFluid> FILL_FLUID = FluidComponents.OUTPUT.key("fluid");
    RecipeKey<Boolean> FILL_MUST_FIT_ALL = BooleanComponent.BOOLEAN.key("mustFitAll").optional(true);
    RecipeKey<String> FILL_SOUND = StringComponent.ANY.key("sound").defaultOptional();

    RecipeSchema FILL = new RecipeSchema(
            ForgeFluidRecipeJS.class, ForgeFluidRecipeJS::new,
            FILL_INPUT, FILL_RESULT, FILL_FLUID, FILL_MUST_FIT_ALL, FILL_SOUND
    );

    // ── Empty (fluid from cauldron → item) ──────────────────────────────
    RecipeKey<InputItem> EMPTY_INPUT = ItemComponents.INPUT.key("input");
    RecipeKey<OutputItem> EMPTY_RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<InputFluid> EMPTY_FLUID = FluidComponents.INPUT.key("fluid");
    RecipeKey<String> EMPTY_SOUND = StringComponent.ANY.key("sound").defaultOptional();

    RecipeSchema EMPTY = new RecipeSchema(
            ForgeFluidRecipeJS.class, ForgeFluidRecipeJS::new,
            EMPTY_INPUT, EMPTY_RESULT, EMPTY_FLUID, EMPTY_SOUND
    );
}
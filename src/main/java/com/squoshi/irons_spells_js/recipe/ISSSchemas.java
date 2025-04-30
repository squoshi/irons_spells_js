package com.squoshi.irons_spells_js.recipe;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.RegistryComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.kubejs.util.Cast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class ISSSchemas {

	public static final RecipeSchema BREW;
	public static final RecipeSchema EMPTY;
	public static final RecipeSchema FILL;

	static {
		RecipeKey<FluidStack> BASE_FLUID = FluidStackComponent.FLUID_STACK.inputKey("base_fluid");
		RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("input");
		RecipeKey<List<FluidStack>> RESULTS = FluidStackComponent.FLUID_STACK.asList().outputKey("results").allowEmpty();
		RecipeKey<ItemStack> BYPRODUCT = ItemStackComponent.ITEM_STACK.outputKey("byproduct").defaultOptional();
		BREW = new RecipeSchema(RESULTS, INPUT, BASE_FLUID, BYPRODUCT);

		RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
		RecipeKey<FluidStack> FLUID = FluidStackComponent.FLUID_STACK.otherKey("fluid");
		RecipeKey<SoundEvent> SOUND = new RegistryComponent<>(BuiltInRegistries.SOUND_EVENT, Cast.to(RegistryType.ofKey(BuiltInRegistries.SOUND_EVENT.key())), BuiltInRegistries.SOUND_EVENT.byNameCodec()).otherKey("sound").defaultOptional();
		EMPTY = new RecipeSchema(RESULT, INPUT, FLUID, SOUND);

		RecipeKey<Boolean> MUST_FIT_ALL = BooleanComponent.BOOLEAN.otherKey("mustFitAll").optional(true);
		FILL = new RecipeSchema(FLUID, INPUT, RESULT, MUST_FIT_ALL, SOUND);
	}
}

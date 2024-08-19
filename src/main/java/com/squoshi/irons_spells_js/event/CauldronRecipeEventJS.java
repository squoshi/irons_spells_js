package com.squoshi.irons_spells_js.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronRecipeRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class CauldronRecipeEventJS implements KubeEvent {
	private final List<CauldronRecipe> recipes = new ArrayList<>();

	public CauldronRecipe add(ItemStack output, ItemStack input, ItemStack ingredient) {
		var recipe = new CauldronRecipe(input, ingredient, output);
		recipes.add(recipe);
		return recipe;
	}

	@HideFromJS
	public void registerAll() {
		recipes.forEach(CauldronRecipe::register);
	}

	public record CauldronRecipe(ItemStack input, ItemStack ingredient, ItemStack output, int inputCount, int outputCount) {
		public CauldronRecipe(ItemStack input, ItemStack ingredient, ItemStack output) {
			this(input, ingredient, output, 1, 4);
		}

		public CauldronRecipe {
			if (input.getCount() > 4) {
				ConsoleJS.STARTUP.warn("Cauldron input " + input + " with size over 4, setting max to 4.");
				input.setCount(4);
			}
			if (ingredient.getCount() != 1) {
				ConsoleJS.STARTUP.warn("Cauldron ingredient " + ingredient + " with size not equal to 1, setting to 1.");
				ingredient.setCount(1);
			}
			if (output.getCount() > 4) {
				ConsoleJS.STARTUP.warn("Cauldron output " + output + " with size over 4, setting max to 4.");
				output.setCount(1);
			}
			inputCount = input.getCount();
			outputCount = output.getCount();
			input.setCount(1);
			output.setCount(1);
		}

		public void register() {
			AlchemistCauldronRecipeRegistry.registerRecipe(output.kjs$getIdLocation(), new AlchemistCauldronRecipe(input, ingredient, output).setBaseRequirement(inputCount).setResultLimit(outputCount));
		}
	}
}

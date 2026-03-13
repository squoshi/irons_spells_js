package com.squoshi.irons_spells_js.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.recipe.RecipeJS;

/**
 * Custom RecipeJS subclass that converts between KubeJS fluid format
 * ({@code {fluid, amount, nbt}}) and Forge FluidStack.CODEC format
 * ({@code {FluidName, Amount, Tag}}).
 * <p>
 * This is needed because Iron's Spellbooks serializes fluids using
 * {@code FluidStack.CODEC} (Forge format), while KubeJS uses its own format.
 */
public class ForgeFluidRecipeJS extends RecipeJS {

    private static JsonElement toForgeFluidJson(FluidStackJS fluid) {
        JsonObject obj = new JsonObject();
        obj.addProperty("FluidName", fluid.getId());
        obj.addProperty("Amount", (int) fluid.kjs$getAmount());
        if (fluid.getNbt() != null) {
            obj.add("Tag", JsonParser.parseString(fluid.getNbt().toString()));
        }
        return obj;
    }

    private static FluidStackJS fromForgeFluidJson(Object from) {
        if (from instanceof JsonObject obj && obj.has("FluidName")) {
            JsonObject kjsFormat = new JsonObject();
            kjsFormat.addProperty("fluid", obj.get("FluidName").getAsString());
            if (obj.has("Amount")) {
                kjsFormat.addProperty("amount", obj.get("Amount").getAsLong());
            }
            if (obj.has("Tag")) {
                kjsFormat.add("nbt", obj.get("Tag"));
            }
            return FluidStackJS.fromJson(kjsFormat);
        }
        return FluidStackJS.of(from);
    }

    @Override
    public InputFluid readInputFluid(Object from) {
        return fromForgeFluidJson(from);
    }

    @Override
    public JsonElement writeInputFluid(InputFluid value) {
        return toForgeFluidJson((FluidStackJS) value);
    }

    @Override
    public OutputFluid readOutputFluid(Object from) {
        return (OutputFluid) fromForgeFluidJson(from);
    }

    @Override
    public JsonElement writeOutputFluid(OutputFluid value) {
        return toForgeFluidJson((FluidStackJS) value);
    }
}
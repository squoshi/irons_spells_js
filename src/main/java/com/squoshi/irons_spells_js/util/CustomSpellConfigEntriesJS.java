package com.squoshi.irons_spells_js.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class CustomSpellConfigEntriesJS {
    public static void addSpellEntries(Map<ResourceLocation, JsonElement> entries) {
        IronsSpellsJSPlugin.SPELL_REGISTRY.objects.values().forEach(builder -> {
            AbstractSpell spell = (AbstractSpell) builder.get();
            ResourceLocation id = spell.getSpellResource();
            entries.putIfAbsent(id, createConfigs(spell.getDefaultConfig()));
        });
    }

    private static JsonObject createConfigs(DefaultConfig config) {
        JsonObject json = new JsonObject();
        json.addProperty(SpellConfigParameter.ENABLED.key().toString(), config.enabled);
        json.addProperty(SpellConfigParameter.SCHOOL.key().toString(), config.schoolResource.toString());
        json.addProperty(SpellConfigParameter.MIN_RARITY.key().toString(), config.minRarity.getSerializedName());
        json.addProperty(SpellConfigParameter.MAX_LEVEL.key().toString(), config.maxLevel);
        json.addProperty(SpellConfigParameter.COOLDOWN_IN_SECONDS.key().toString(), config.cooldownInSeconds);
        json.addProperty(SpellConfigParameter.ALLOW_CRAFTING.key().toString(), config.allowCrafting);
        json.addProperty(SpellConfigParameter.MANA_MULTIPLIER.key().toString(), 1.0);
        json.addProperty(SpellConfigParameter.POWER_MULTIPLIER.key().toString(), 1.0);
        return json;
    }
}

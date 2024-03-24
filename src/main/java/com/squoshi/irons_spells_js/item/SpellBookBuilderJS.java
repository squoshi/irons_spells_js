package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.resources.ResourceLocation;

public class SpellBookBuilderJS extends BuilderBase<SpellBook> {
    public transient SpellRarity rarity = SpellRarity.COMMON;
    public transient int maxSpellSlots = 1;

    public SpellBookBuilderJS(ResourceLocation i) {
        super(i);
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ITEM;
    }

    @SuppressWarnings("unused")
    public SpellBookBuilderJS setRarity(SpellRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    @SuppressWarnings("unused")
    public SpellBookBuilderJS setMaxSpellSlots(int maxSpellSlots) {
        this.maxSpellSlots = maxSpellSlots;
        return this;
    }

    @Override
    public SpellBook createObject() {
        return new SpellBook(this.maxSpellSlots, this.rarity);
    }
}

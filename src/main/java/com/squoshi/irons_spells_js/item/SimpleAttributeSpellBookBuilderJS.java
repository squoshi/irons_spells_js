package com.squoshi.irons_spells_js.item;

import com.google.common.collect.Multimap;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.spell_books.SimpleAttributeSpellBook;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class SimpleAttributeSpellBookBuilderJS extends BuilderBase<SimpleAttributeSpellBook> {
    public transient int maxSpellSlots = 0;
    public transient SpellRarity rarity = SpellRarity.COMMON;
    public transient Multimap<Attribute, AttributeModifier> defaultModifiers;

    public SimpleAttributeSpellBookBuilderJS(ResourceLocation i) {
        super(i);
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ITEM;
    }

    @SuppressWarnings("unused")
    public SimpleAttributeSpellBookBuilderJS addDefaultAttribute(ISSKJSUtils.AttributeHolder attribute, AttributeModifier modifier) {
        this.defaultModifiers.put(Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(attribute.getLocation())), modifier);
        return this;
    }

    @SuppressWarnings("unused")
    public SimpleAttributeSpellBookBuilderJS setMaxSpellSlots(int maxSpellSlots) {
        this.maxSpellSlots = maxSpellSlots;
        return this;
    }

    @SuppressWarnings("unused")
    public SimpleAttributeSpellBookBuilderJS setRarity(SpellRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    @Override
    public SimpleAttributeSpellBook createObject() {
        return new SimpleAttributeSpellBook(this.maxSpellSlots, this.rarity, this.defaultModifiers);
    }
}

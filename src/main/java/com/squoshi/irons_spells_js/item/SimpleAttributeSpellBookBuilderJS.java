package com.squoshi.irons_spells_js.item;

import com.google.common.collect.ArrayListMultimap;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SimpleAttributeSpellBookBuilderJS extends BuilderBase<SimpleAttributeSpellBook> {
    public record AttributeHolder(ResourceLocation attribute, AttributeModifier modifier) {}

    public transient int maxSpellSlots = 0;
    public transient SpellRarity rarity = SpellRarity.COMMON;
    public transient List<AttributeHolder> defaultModifiers = new ArrayList<>();

    public SimpleAttributeSpellBookBuilderJS(ResourceLocation i) {
        super(i);
        tag(new ResourceLocation("curios:spellbook"));
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ITEM;
    }

    @SuppressWarnings("unused")
    public SimpleAttributeSpellBookBuilderJS addDefaultAttribute(ISSKJSUtils.AttributeHolder attribute, String modifierName, double modifierAmount, AttributeModifier.Operation modifierOperation) {
        defaultModifiers.add(new AttributeHolder(attribute.getLocation(), new AttributeModifier(modifierName, modifierAmount, modifierOperation)));
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
        final Multimap<Attribute, AttributeModifier> map = ArrayListMultimap.create();
        for (AttributeHolder holder : defaultModifiers) {
            final Attribute attribute = Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(holder.attribute()));
            map.put(attribute, holder.modifier());
        }
        return new SimpleAttributeSpellBook(maxSpellSlots, rarity, map);
    }
}

package com.squoshi.irons_spells_js.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UniqueSpellBookBuilderJS extends BuilderBase<UniqueSpellBook> {
    public record AttributeHolder(ResourceLocation attribute, AttributeModifier modifier) {}

    public record SpellHolder(ResourceLocation spell, int spellLevel) {}

    public transient SpellRarity rarity = SpellRarity.COMMON;
    public transient SpellDataRegistryHolder[] spellDataRegistryHolder = SpellDataRegistryHolder.of();
    public transient int additionalSlots = 0;
    public transient List<AttributeHolder> defaultModifiers = new ArrayList<>();

    public transient List<SpellHolder> spellHolders = new ArrayList<>();

    public UniqueSpellBookBuilderJS(ResourceLocation i) {
        super(i);
        tag(new ResourceLocation("curios:spellbook"));
    }

    @SuppressWarnings("unused")
    public UniqueSpellBookBuilderJS addDefaultAttribute(ISSKJSUtils.AttributeHolder attribute, String modifierName, double modifierAmount, AttributeModifier.Operation modifierOperation) {
        defaultModifiers.add(new AttributeHolder(attribute.getLocation(), new AttributeModifier(modifierName, modifierAmount, modifierOperation)));
        return this;
    }

    @SuppressWarnings("unused")
    public UniqueSpellBookBuilderJS setAdditionalSpellSlots(int additionalSlots) {
        this.additionalSlots = additionalSlots;
        return this;
    }

    @SuppressWarnings("unused")
    public UniqueSpellBookBuilderJS setRarity(SpellRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    @SuppressWarnings("unused")
    public UniqueSpellBookBuilderJS addDefaultSpell(ISSKJSUtils.SpellHolder spell, int spellLevel) {
        this.spellHolders.add(new SpellHolder(spell.getLocation(), spellLevel));
        return this;
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ITEM;
    }

    @Override
    public UniqueSpellBook createObject() {
        final Multimap<Attribute, AttributeModifier> map = ArrayListMultimap.create();
        for (AttributeHolder holder : defaultModifiers) {
            final Attribute attribute = Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(holder.attribute()));
            map.put(attribute, holder.modifier());
        }
        SpellDataRegistryHolder[] spellDataHolders = new SpellDataRegistryHolder[this.spellHolders.size()];
        var iterator = spellHolders.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            var spells = iterator.next();
            spellDataHolders[i] = new SpellDataRegistryHolder(RegistryObject.create(spells.spell, SpellRegistry.REGISTRY.get()), spells.spellLevel);
        }
        return new UniqueSpellBook(rarity, spellDataHolders, additionalSlots, () -> map);
    }
}

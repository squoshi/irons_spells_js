package com.squoshi.irons_spells_js.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.item.spell_books.SimpleAttributeSpellBook;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class SpellBookBuilderJS extends BuilderBase<SpellBook> {
    public transient SpellDataRegistryHolder[] spellDataRegistryHolder = SpellDataRegistryHolder.of();
    public transient List<SpellHolder> spellHolders = new ArrayList<>();
    public transient int maxSpellSlots = 1;
    public transient List<AttributeHolder> defaultModifiers = new ArrayList<>();

    public SpellBookBuilderJS(ResourceLocation i) {
        super(i);
        tag(new ResourceLocation("curios:spellbook"));
    }

    @Override
    public RegistryInfo<Item> getRegistryType() {
        return RegistryInfo.ITEM;
    }

    public SpellBookBuilderJS addDefaultAttribute(ISSKJSUtils.AttributeHolder attribute, String modifierName, double modifierAmount, AttributeModifier.Operation modifierOperation) {
        defaultModifiers.add(new AttributeHolder(attribute.getLocation(), new AttributeModifier(modifierName, modifierAmount, modifierOperation)));
        return this;
    }

    public SpellBookBuilderJS setMaxSpellSlots(int maxSpellSlots) {
        this.maxSpellSlots = maxSpellSlots;
        return this;
    }

    public SpellBookBuilderJS addDefaultSpell(ISSKJSUtils.SpellHolder spell, int spellLevel) {
        this.spellHolders.add(new SpellHolder(spell.getLocation(), spellLevel));
        return this;
    }

    @Override
    public SpellBook createObject() {
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
        if (spellDataHolders.length > 0) {
            return new UniqueSpellBook(SpellRarity.LEGENDARY, spellDataHolders, Math.max(0, maxSpellSlots - spellDataHolders.length), () -> map);
        }
        if (!map.isEmpty()) {
            return new SimpleAttributeSpellBook(maxSpellSlots, SpellRarity.LEGENDARY, map);
        }
        return new SpellBook(maxSpellSlots, SpellRarity.LEGENDARY);
    }

    public record AttributeHolder(ResourceLocation attribute, AttributeModifier modifier) {
    }

    public record SpellHolder(ResourceLocation spell, int spellLevel) {
    }
}

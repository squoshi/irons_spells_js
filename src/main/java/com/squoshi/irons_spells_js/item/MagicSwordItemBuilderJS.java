package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.item.custom.HandheldItemBuilder;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class MagicSwordItemBuilderJS extends HandheldItemBuilder {
    public record AttributeHolder(ResourceLocation attribute, AttributeModifier modifier) {}

    public transient List<AttributeHolder> additionalAttributes = new ArrayList<>();
    public transient SpellDataRegistryHolder[] spellDataRegistryHolders = SpellDataRegistryHolder.of();

    public MagicSwordItemBuilderJS(ResourceLocation i) {
        super(i, 3f, -2.4f);
    }

    @SuppressWarnings("unused")
    public MagicSwordItemBuilderJS setDefaultSpells(SpellDataRegistryHolder... spellDataRegistryHolder) {
        this.spellDataRegistryHolders = spellDataRegistryHolder;
        return this;
    }

    @SuppressWarnings("unused")
    public MagicSwordItemBuilderJS addAdditionalAttribute(ResourceLocation attribute, String modifierName, double modifierAmount, AttributeModifier.Operation modifierOperation) {
        additionalAttributes.add(new AttributeHolder(attribute, new AttributeModifier(modifierName, modifierAmount, modifierOperation)));
        return this;
    }

    @Override
    public MagicSwordItem createObject() {
        Map<Attribute, AttributeModifier> map = new HashMap<>(Map.of());
        for (AttributeHolder holder : additionalAttributes) {
            final Attribute attribute = Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(holder.attribute()));
            map.put(attribute, holder.modifier());
        }
        return new MagicSwordItem(this.toolTier, this.attackDamageBaseline, this.speedBaseline, this.spellDataRegistryHolders, map, this.createItemProperties());
    }
}

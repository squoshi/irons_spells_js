package com.squoshi.irons_spells_js.item;

import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.item.custom.HandheldItemBuilder;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

public class MagicSwordItemBuilderJS extends HandheldItemBuilder {
    public record AttributeHolder(ResourceLocation attribute, AttributeModifier modifier) {}
    public record SpellHolder(ResourceLocation spell, int spellLevel) {}

    public transient List<AttributeHolder> additionalAttributes = new ArrayList<>();
    public transient List<SpellHolder> spellHolders = new ArrayList<>();

    public MagicSwordItemBuilderJS(ResourceLocation i) {
        super(i, 3f, -2.4f);
    }

    @SuppressWarnings("unused")
    public MagicSwordItemBuilderJS addDefaultSpell(ISSKJSUtils.SpellHolder spell, int spellLevel) {
        this.spellHolders.add(new SpellHolder(spell.getLocation(), spellLevel));
        return this;
    }

    @SuppressWarnings("unused")
    public MagicSwordItemBuilderJS addAdditionalAttribute(ISSKJSUtils.AttributeHolder attribute, String modifierName, double modifierAmount, AttributeModifier.Operation modifierOperation) {
        additionalAttributes.add(new AttributeHolder(attribute.getLocation(), new AttributeModifier(modifierName, modifierAmount, modifierOperation)));
        return this;
    }

    @Override
    public MagicSwordItem createObject() {
        Map<Attribute, AttributeModifier> map = new HashMap<>(Map.of());
        for (AttributeHolder holder : additionalAttributes) {
            final Attribute attribute = Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(holder.attribute()));
            map.put(attribute, holder.modifier());
        }
        SpellDataRegistryHolder[] spellDataHolders = new SpellDataRegistryHolder[this.spellHolders.size()];
        var iterator = spellHolders.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            var spells = iterator.next();
            spellDataHolders[i] = new SpellDataRegistryHolder(RegistryObject.create(spells.spell, SpellRegistry.REGISTRY.get()), spells.spellLevel);
        }
        return new MagicSwordItem(this.toolTier, this.attackDamageBaseline, this.speedBaseline, spellDataHolders, map, this.createItemProperties());
    }
}

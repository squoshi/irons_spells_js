package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.item.custom.HandheldItemBuilder;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class StaffItemBuilderJS extends HandheldItemBuilder {
    public record AttributeHolder(ResourceLocation attribute, AttributeModifier modifier) {}

    public transient List<AttributeHolder> additionalAttributes = new ArrayList<>();

    public StaffItemBuilderJS(ResourceLocation i) {
        super(i, 3f, -2.4f);
    }

    @SuppressWarnings("unused")
    public StaffItemBuilderJS addAdditionalAttribute(ResourceLocation attribute, String modifierName, double modifierAmount, AttributeModifier.Operation modifierOperation) {
        additionalAttributes.add(new AttributeHolder(attribute, new AttributeModifier(modifierName, modifierAmount, modifierOperation)));
        return this;
    }

    @Override
    public StaffItem createObject() {
        Map<Attribute, AttributeModifier> map = new HashMap<>(Map.of());
        for (AttributeHolder holder : additionalAttributes) {
            final Attribute attribute = Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(holder.attribute()));
            map.put(attribute, holder.modifier());
        }
        return new StaffItem(this.createItemProperties(), this.attackDamageBaseline, this.speedBaseline, map);
    }
}

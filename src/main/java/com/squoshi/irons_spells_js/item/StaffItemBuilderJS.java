package com.squoshi.irons_spells_js.item;

import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.item.custom.HandheldItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
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

    @Info("""
            Adds an additional attribute to the item. It takes an attribute ID (or just an attribute object), the modifier name, the modifier amount, and the modifier operation.
            The modifier operation can be either `ADDITION`, `MULTIPLY_TOTAL` or `MULTIPLY_BASE`.
    """)
    @SuppressWarnings("unused")
    public StaffItemBuilderJS addAdditionalAttribute(ISSKJSUtils.AttributeHolder attribute, String modifierName, double modifierAmount, AttributeModifier.Operation modifierOperation) {
        additionalAttributes.add(new AttributeHolder(attribute.getLocation(), new AttributeModifier(modifierName, modifierAmount, modifierOperation)));
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

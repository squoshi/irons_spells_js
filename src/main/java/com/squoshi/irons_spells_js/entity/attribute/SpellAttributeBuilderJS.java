package com.squoshi.irons_spells_js.entity.attribute;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class SpellAttributeBuilderJS extends BuilderBase<Attribute> {
    public transient String descriptionId;
    public transient double defaultValue;
    public transient double minimumValue;
    public transient double maximumValue;

    public SpellAttributeBuilderJS(ResourceLocation i) {
        super(i);
        this.descriptionId = "attribute." + i.getNamespace() + "." + i.getPath();
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ATTRIBUTE;
    }

    @SuppressWarnings("unused")
    public SpellAttributeBuilderJS setDefaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @SuppressWarnings("unused")
    public SpellAttributeBuilderJS setMinimumValue(double minimumValue) {
        this.minimumValue = minimumValue;
        return this;
    }

    @SuppressWarnings("unused")
    public SpellAttributeBuilderJS setMaximumValue(double maximumValue) {
        this.maximumValue = maximumValue;
        return this;
    }

    @Override
    public Attribute createObject() {
        return new MagicRangedAttribute(this.descriptionId, this.defaultValue, this.minimumValue, this.maximumValue).setSyncable(true);
    }
}

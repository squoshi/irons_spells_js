package com.squoshi.irons_spells_js.spell.school;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;

public class SchoolTypeJSBuilder extends BuilderBase<SchoolType> {
    public transient ResourceLocation schoolResource;
    public transient TagKey<Item> focus;
    public transient Component name;
    public transient LazyOptional<Attribute> powerAttribute;
    public transient LazyOptional<Attribute> resistanceAttribute;
    public transient LazyOptional<SoundEvent> defaultCastSound;
    public transient ResourceKey<DamageType> damageType;

    public SchoolTypeJSBuilder(ResourceLocation i) {
        super(i);
        this.schoolResource = i;
    }

    public SchoolTypeJSBuilder setFocus(ResourceLocation focus) {
        this.focus = TagKey.create(Registries.ITEM, focus);
        return this;
    }

    public SchoolTypeJSBuilder setName(Component name) {
        this.name = name;
        return this;
    }

    public SchoolTypeJSBuilder setPowerAttribute(Attribute powerAttribute) {
        this.powerAttribute = LazyOptional.of(() -> powerAttribute);
        return this;
    }

    public SchoolTypeJSBuilder setResistanceAttribute(Attribute resistanceAttribute) {
        this.resistanceAttribute = LazyOptional.of(() -> resistanceAttribute);
        return this;
    }

    public SchoolTypeJSBuilder setDefaultCastSound(SoundEvent defaultCastSound) {
        this.defaultCastSound = LazyOptional.of(() -> defaultCastSound);
        return this;
    }

    public SchoolTypeJSBuilder setDamageType(ResourceKey<DamageType> damageType) {
        this.damageType = damageType;
        return this;
    }

    @Override
    public RegistryInfo getRegistryType() {
        return IronsSpellsJSPlugin.SCHOOL_REGISTRY;
    }

    @Override
    public SchoolType createObject() {
        return new SchoolType(
                this.schoolResource,
                this.focus,
                this.name,
                this.powerAttribute,
                this.resistanceAttribute,
                this.defaultCastSound,
                this.damageType
        );
    }
}

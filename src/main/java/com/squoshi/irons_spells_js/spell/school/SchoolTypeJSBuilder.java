package com.squoshi.irons_spells_js.spell.school;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
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

    public SchoolTypeJSBuilder(ResourceLocation i) {
        super(i);
        this.schoolResource = i;
    }

    @SuppressWarnings("unused")
    public SchoolTypeJSBuilder setFocus(ResourceLocation focus) {
        this.focus = TagKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft:item")), focus);
        return this;
    }

    @SuppressWarnings("unused")
    public SchoolTypeJSBuilder setName(Component name) {
        this.name = name;
        return this;
    }

    @SuppressWarnings("unused")
    public SchoolTypeJSBuilder setPowerAttribute(Attribute powerAttribute) {
        this.powerAttribute = LazyOptional.of(() -> powerAttribute);
        return this;
    }

    @SuppressWarnings("unused")
    public SchoolTypeJSBuilder setResistanceAttribute(Attribute resistanceAttribute) {
        this.resistanceAttribute = LazyOptional.of(() -> resistanceAttribute);
        return this;
    }

    @SuppressWarnings("unused")
    public SchoolTypeJSBuilder setDefaultCastSound(SoundEvent defaultCastSound) {
        this.defaultCastSound = LazyOptional.of(() -> defaultCastSound);
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
                this.defaultCastSound
        );
    }
}
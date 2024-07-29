package com.squoshi.irons_spells_js.spell.school;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@SuppressWarnings("unused")
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

    @Info("""
            Sets the ID of the item tag used for the focus item.
            Focus items need the `"irons_spellbooks:school_focus"` tag, as well as the tag specified here.
    """)
    public SchoolTypeJSBuilder setFocus(ResourceLocation focus) {
        this.focus = ItemTags.create(focus);
        return this;
    }

    @Info("""
            Sets the name of the school. It requires a `Component`, which allows custom colors and formatting. You can also use `Text`.
    """)
    public SchoolTypeJSBuilder setName(Component name) {
        this.name = name;
        return this;
    }

    @Info("""
            Sets the power attribute of the school. It takes either a String, ResourceLocation, or just an Attribute.
    """)
    public SchoolTypeJSBuilder setPowerAttribute(ISSKJSUtils.AttributeHolder powerAttribute) {
        this.powerAttribute = LazyOptional.of(() -> Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(powerAttribute.getLocation())));
        return this;
    }

    @Info("""
            Sets the resistance attribute of the school. It takes either a String, ResourceLocation, or just an Attribute.
    """)
    public SchoolTypeJSBuilder setResistanceAttribute(ISSKJSUtils.AttributeHolder resistanceAttribute) {
        this.resistanceAttribute = LazyOptional.of(() -> Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(resistanceAttribute.getLocation())));
        return this;
    }

    @Info("""
            Sets the default cast sound of the school. It takes either a String, ResourceLocation, or just a SoundEvent.
    """)
    public SchoolTypeJSBuilder setDefaultCastSound(ISSKJSUtils.SoundEventHolder defaultCastSound) {
        this.defaultCastSound = LazyOptional.of(() -> Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(defaultCastSound.getLocation())));
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

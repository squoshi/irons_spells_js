package com.squoshi.irons_spells_js.spell.school;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class SchoolTypeJSBuilder extends BuilderBase<SchoolType> {
    public transient ResourceLocation schoolResource;
    public transient TagKey<Item> focus;
    public transient Component name;
    public transient LazyOptional<Attribute> powerAttribute;
    public transient LazyOptional<Attribute> resistanceAttribute;
    public transient LazyOptional<SoundEvent> defaultCastSound;
    public transient List<Item> focusItems = new ArrayList<>();

    public SchoolTypeJSBuilder(ResourceLocation i) {
        super(i);
        this.schoolResource = i;
        this.focus = ItemTags.create(new ResourceLocation(this.schoolResource.getNamespace(), this.schoolResource.getPath() + "_focus"));
    }

    public SchoolTypeJSBuilder setFocus(Item ...focusItems) {
        for (var item : focusItems){
            if (Items.AIR == item){
                ConsoleJS.STARTUP.error("Tried to add an invalid item to IronSpells School Focus");
            } else this.focusItems.add(item);
        }
        return this;
    }

    public SchoolTypeJSBuilder setName(Component name) {
        this.name = name;
        return this;
    }

    public SchoolTypeJSBuilder setPowerAttribute(ISSKJSUtils.AttributeHolder powerAttribute) {
        this.powerAttribute = LazyOptional.of(() -> Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(powerAttribute.getLocation())));
        return this;
    }

    public SchoolTypeJSBuilder setResistanceAttribute(ISSKJSUtils.AttributeHolder resistanceAttribute) {
        this.resistanceAttribute = LazyOptional.of(() -> Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(resistanceAttribute.getLocation())));
        return this;
    }

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

    @Override
    public void generateDataJsons(DataJsonGenerator generator) {
        var focusJson = new JsonObject();
        focusJson.addProperty("replace", false);
        var focusArray = new JsonArray();
        focusArray.add("#" + this.focus.location());
        focusJson.add("values", focusArray);
        var target = new ResourceLocation("irons_spellbooks:tags/items/school_focus");
        generator.json(target, focusJson);

        if (!this.focusItems.isEmpty()){
            var itemsJson = new JsonObject();
            itemsJson.addProperty("replace", false);
            var itemsArray = new JsonArray();
            for (var item : this.focusItems) {
                itemsArray.add(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString());
            }
            itemsJson.add("values", itemsArray);
            var itemsTarget = new ResourceLocation(this.focus.location().getNamespace() + ":tags/items/"+ this.focus.location().getPath());
            generator.json(itemsTarget, itemsJson);
        }
    }
}
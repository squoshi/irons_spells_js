package com.squoshi.irons_spells_js.spell.school;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SchoolTypeJSBuilder extends BuilderBase<SchoolType> {
    public transient ResourceLocation schoolResource;
    public transient TagKey<Item> schoolFocus;
    public transient List<Item> focusItems = new ArrayList<>();
    public transient List<TagKey<Item>> focusTags = new ArrayList<>();
    public transient Component name;
    public transient Lazy<Attribute> powerAttribute;
    public transient Lazy<Attribute> resistanceAttribute;
    public transient Lazy<SoundEvent> defaultCastSound;
    public transient ResourceKey<DamageType> damageType;
    public transient boolean requiresLearning = false;
    public transient boolean allowLooting = true;

    public SchoolTypeJSBuilder(ResourceLocation i) {
        super(i);
        this.schoolResource = i;
    }

    @Info("""
        Sets the focus item tags for this school.
    """)
    public final SchoolTypeJSBuilder addFocusItemTags(String... focusTags) {
        for (var tag : focusTags) {
            var id = tag.contains(":") ? ResourceLocation.parse(tag) :  ResourceLocation.fromNamespaceAndPath("minecraft", tag);
            var tagKey = ItemTags.create(id);
            this.focusTags.add(tagKey);
        }
        return this;
    }

    @Info("""
            Sets the ID of the item tag used for the focus item.
            Focus items need the `"irons_spellbooks:school_focus"` tag, as well as the tag specified here.
            Deprecated: Use `setDefaultFocusTag` instead.
    """)
    @Deprecated(forRemoval = true)
    public SchoolTypeJSBuilder setFocus(ResourceLocation focus) {
        return setDefaultFocusTag(focus.toString());
    }

    @Info("""
        Sets the ID of the item tag used for the focus item.
        Default tag is `${mod_id}:${school_name}_focus`
    """)
    public SchoolTypeJSBuilder setDefaultFocusTag(String tag) {
        var id = tag.contains(":") ? ResourceLocation.parse(tag) :  ResourceLocation.fromNamespaceAndPath("minecraft", tag);
        this.schoolFocus = ItemTags.create(id);
        return this;
    }
    @Info("""
            Adds specific items to the focus item list for this school.
    """)
    public SchoolTypeJSBuilder addFocusItems(Item... items) {
        for (var item : items) {
            if (Items.AIR == item) {
                ConsoleJS.STARTUP.error("Tried to add an invalid item to IronSpells School Focus");
            } else {
                this.focusItems.add(item);
            }
        }
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
    public SchoolTypeJSBuilder setPowerAttribute(String powerAttribute) {
        var rl = ResourceLocation.parse(powerAttribute);
        this.powerAttribute = Lazy.of(() -> BuiltInRegistries.ATTRIBUTE.get(rl));
        return this;
    }

    @Info("""
            Sets the resistance attribute of the school. It takes either a String, ResourceLocation, or just an Attribute.
    """)
    public SchoolTypeJSBuilder setResistanceAttribute(String resistanceAttribute) {
        var rl = ResourceLocation.parse(resistanceAttribute);
        this.resistanceAttribute = Lazy.of(() -> BuiltInRegistries.ATTRIBUTE.get(rl));
        return this;
    }

    @Info("""
            Sets the default cast sound of the school. It takes either a String, ResourceLocation, or just a SoundEvent.
    """)
    public SchoolTypeJSBuilder setDefaultCastSound(String defaultCastSound) {
        var rl = ResourceLocation.parse(defaultCastSound);
        this.defaultCastSound = Lazy.of(() -> BuiltInRegistries.SOUND_EVENT.get(rl));
        return this;
    }

    @Info("""
            Sets the damage type of the school. It takes either a String, ResourceLocation, or just a DamageType.
            Damage types can be created using datapacks or server scripts, or you can use an existing damage type.
    """)
    public SchoolTypeJSBuilder setDamageType(ResourceLocation damageType) {
        this.damageType = ResourceKey.create(RegistryInfo.DAMAGE_TYPE.key, damageType);
        return this;
    }

    @Info("""
            Sets require learning to true.
    """)
    public SchoolTypeJSBuilder requiresLearning() {
        this.requiresLearning = true;
        return this;
    }

    @Info("""
            Disables looting.
    """)
    public SchoolTypeJSBuilder disableLooting() {
        this.allowLooting = false;
        return this;
    }

    @Override
    public RegistryInfo<SchoolType> getRegistryType() {
        return IronsSpellsJSPlugin.SCHOOL_REGISTRY;
    }

    @Override
    public SchoolType createObject() {
        return new SchoolType(
                this.schoolResource,
                this.schoolFocus,
                this.name,
                this.powerAttribute,
                this.resistanceAttribute,
                this.defaultCastSound,
                this.damageType,
                this.requiresLearning,
                this.allowLooting
        );
    }
}
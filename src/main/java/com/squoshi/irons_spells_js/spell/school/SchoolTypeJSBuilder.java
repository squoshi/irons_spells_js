package com.squoshi.irons_spells_js.spell.school;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class SchoolTypeJSBuilder extends BuilderBase<SchoolType> {
	public transient ResourceLocation schoolResource;
	public transient TagKey<Item> schoolFocus;
	public transient List<Item> focusItems = new ArrayList<>();
	public transient List<TagKey<Item>> focusTags = new ArrayList<>();
	public transient Component name;
	public transient Holder<Attribute> powerAttribute;
	public transient Holder<Attribute> resistanceAttribute;
	public transient Holder<SoundEvent> defaultCastSound;
	public transient ResourceKey<DamageType> damageType;
	public transient boolean requiresLearning = false;
	public transient boolean allowLooting = true;

	public SchoolTypeJSBuilder(ResourceLocation i) {
		super(i);
		this.schoolResource = i;
		schoolFocus = ItemTags.create(ResourceLocation.fromNamespaceAndPath(this.schoolResource.getNamespace(), this.schoolResource.getPath() + "_focus"));
	}

	@Info("""
		        Sets the ID of the item tag used for the focus item.
		        ⚠️ Default tag is `${mod_id}:${school_name}_focus` ⚠️️
		""")
	public SchoolTypeJSBuilder setDefaultFocusTag(TagKey<Item> focus) {
		this.schoolFocus = focus;
		return this;
	}

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

	@SafeVarargs
	public final SchoolTypeJSBuilder addFocusItemTags(TagKey<Item>... focusTags) {
		this.focusTags.addAll(Arrays.asList(focusTags));
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
	public SchoolTypeJSBuilder setPowerAttribute(Holder<Attribute> powerAttribute) {
		this.powerAttribute = powerAttribute;
		return this;
	}

	@Info("""
		        Sets the resistance attribute of the school. It takes either a String, ResourceLocation, or just an Attribute.
		""")
	public SchoolTypeJSBuilder setResistanceAttribute(Holder<Attribute> resistanceAttribute) {
		this.resistanceAttribute = resistanceAttribute;
		return this;
	}

	@Info("""
		        Sets the default cast sound of the school. It takes either a String, ResourceLocation, or just a SoundEvent.
		""")
	public SchoolTypeJSBuilder setDefaultCastSound(Holder<SoundEvent> defaultCastSound) {
		this.defaultCastSound = defaultCastSound;
		return this;
	}

	@Info("""
		        Sets the damage type of the school. It takes either a String, ResourceLocation, or just a DamageType.
		        Damage types can be created using datapacks or server scripts, or you can use an existing damage type.
		""")
	public SchoolTypeJSBuilder setDamageType(ResourceKey<DamageType> damageType) {
		this.damageType = damageType;
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

	@Override
	public void generateData(KubeDataGenerator generator) {
		var focusJson = new JsonObject();
		focusJson.addProperty("replace", false);
		var focusArray = new JsonArray();
		var tagObj = new JsonObject();
		tagObj.addProperty("id", "#" + this.schoolFocus.location());
		tagObj.addProperty("required", false);
		focusArray.add(tagObj);
		var target = ResourceLocation.parse("irons_spellbooks:tags/item/school_focus");
		focusJson.add("values", focusArray);
		generator.json(target, focusJson);

		focusJson = new JsonObject();
		focusJson.addProperty("replace", false);
		focusArray = new JsonArray();

		if (!this.focusTags.isEmpty()) {
			for (var tag : this.focusTags) {
				focusArray.add("#" + tag.location());
			}
		}

		if (!this.focusItems.isEmpty()) {
			for (var item : this.focusItems) {
				focusArray.add(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).toString());
			}
		}

		focusJson.add("values", focusArray);
		target = ResourceLocation.parse(schoolFocus.location().getNamespace() + ":tags/item/" + schoolFocus.location().getPath());
		generator.json(target, focusJson);
	}
}
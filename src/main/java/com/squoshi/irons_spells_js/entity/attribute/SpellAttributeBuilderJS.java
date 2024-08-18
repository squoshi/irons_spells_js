package com.squoshi.irons_spells_js.entity.attribute;

import dev.latvian.mods.kubejs.entity.AttributeBuilder;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

@ReturnsSelf
public class SpellAttributeBuilderJS extends AttributeBuilder {

	public SpellAttributeBuilderJS(ResourceLocation i) {
		super(i);
	}

	@HideFromJS
	@Override
	public AttributeBuilder bool(boolean defaultValue) {
		ConsoleJS.STARTUP.error("Boolean not supported for Magic Attributes!");
		return this;
	}

	@Override
	public Attribute createObject() {
		if (defaultValue == null || defaultValue.right().isPresent()) {
			throw new IllegalArgumentException("You need to set a range, use range() method.");
		}
		var range = defaultValue.left().orElseThrow();
		syncable = true;
		return new MagicRangedAttribute(this.getBuilderTranslationKey(), range.defaultValue(), range.min(), range.max());
	}
}
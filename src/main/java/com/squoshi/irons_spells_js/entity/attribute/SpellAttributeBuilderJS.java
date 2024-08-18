package com.squoshi.irons_spells_js.entity.attribute;

import com.google.common.base.Predicates;
import dev.latvian.mods.kubejs.entity.AttributeBuilder;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

@ReturnsSelf
public class SpellAttributeBuilderJS extends AttributeBuilder {

	private Range range = null;
	public SpellAttributeBuilderJS(ResourceLocation i) {
		super(i);
	}

	@Override
	public AttributeBuilder range(double defaultValue, double min, double max) {
		this.range = new Range(defaultValue, min, max);
		return this;
	}

	@HideFromJS
	@Override
	public AttributeBuilder bool(boolean defaultValue) {
		ConsoleJS.STARTUP.warn("Boolean not supported for Magic Attributes!");
		return this;
	}

	@Override
	public Attribute createObject() {
		if (range == null) {
			throw new IllegalArgumentException("You need to set a range, use range() method.");
		}

		// Temp fix, wait for KubeJS to merge Attribute fix
		if (this.getPredicateList().isEmpty()) {
			getPredicateList().add(Predicates.alwaysTrue());
		}
		return new MagicRangedAttribute(this.getBuilderTranslationKey(), this.range.defaultValue(), this.range.min(), this.range.max()).setSyncable(true);
	}
}
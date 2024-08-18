package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class CustomStaff extends StaffItem {
	private final Builder builder;

	private CustomStaff(Builder builder) {
		super(builder.createItemProperties().attributes(ExtendedSwordItem.createAttributes(builder.tierBuilder.build())));
		this.builder = builder;
	}

	@Override
	public int getEnchantmentValue() {
		return builder.enchantmentValue;
	}

	@ReturnsSelf
	public static class Builder extends ItemBuilder {
		private final StaffTierBuilder tierBuilder = new StaffTierBuilder();
		private int enchantmentValue = 20;

		public Builder(ResourceLocation i) {
			super(i);
		}

		public Builder setEnchantmentValue(int enchantmentValue) {
			this.enchantmentValue = enchantmentValue;
			return this;
		}

		@Info("""
			**NOT SUPPORTED**
			""")
		@Override
		public ItemBuilder use(UseCallback use) {
			ConsoleJS.STARTUP.warn("Use callback is not supported for this Staff.");
			return this;
		}

		public Builder setTier(Consumer<StaffTierBuilder> tier) {
			tier.accept(tierBuilder);
			return this;
		}

		@Override
		public StaffItem createObject() {
			return new CustomStaff(this);
		}

		public enum StaffTierEnum {
			GRAYBEARD(StaffTier.GRAYBEARD),
			ARTIFICER(StaffTier.ARTIFICER),
			ICE_STAFF(StaffTier.ICE_STAFF),
			LIGHTNING_ROD(StaffTier.LIGHTNING_ROD),
			BLOOD_STAFF(StaffTier.BLOOD_STAFF);

			private final StaffTier tier;

			StaffTierEnum(StaffTier tier) {
				this.tier = tier;
			}

			public StaffTier getTier() {
				return tier;
			}
		}

		public static class StaffTierBuilder {
			private final List<AttributeContainer> attributes = new ArrayList<>();
			private StaffTierEnum existingTier = null;
			private boolean mergeAttributes = false;
			private Float damage;
			private Float speed;

			public StaffTierBuilder setDamage(float damage) {
				this.damage = damage;
				return this;
			}

			public StaffTierBuilder setSpeed(float speed) {
				this.speed = speed;
				return this;
			}

			public StaffTierBuilder addAttribute(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
				this.attributes.add(new AttributeContainer(attribute, value, operation));
				return this;
			}

			public StaffTierBuilder useBaseTier(StaffTierEnum tier, boolean mergeAttributes) {
				this.existingTier = tier;
				this.mergeAttributes = mergeAttributes;
				return this;
			}

			@HideFromJS
			public StaffTier build() {
				if (existingTier != null) {
					if (damage == null) {
						damage = existingTier.getTier().getAttackDamageBonus();
					}
					if (speed == null) {
						speed = existingTier.getTier().getSpeed();
					}
					if (mergeAttributes) {
						attributes.addAll(List.of(existingTier.getTier().getAdditionalAttributes()));
					}
				}
				if (damage == null) {
					damage = 2F;
				}
				if (speed == null) {
					speed = -3F;
				}
				return new StaffTier(damage, speed, attributes.toArray(new AttributeContainer[0]));
			}
		}
	}
}



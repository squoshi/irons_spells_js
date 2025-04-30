package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;
import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.ExtendedWeaponTier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CustomMagicSwordItem extends MagicSwordItem {
	public CustomMagicSwordItem(Builder b) {
		super(b.tier.build(), b.createItemProperties().attributes(ExtendedSwordItem.createAttributes(b.tier.build())), b.spellDataRegistryHolders.toArray(new SpellDataRegistryHolder[]{}));
	}

	public static class Builder extends ItemBuilder {
		private final TierBuilder tier = new TierBuilder();
		private final Properties properties = new Properties();
		private final List<SpellDataRegistryHolder> spellDataRegistryHolders = new ArrayList<>();

		public Builder(ResourceLocation id) {
			super(id);
		}

		public Builder setTier(Consumer<TierBuilder> callback) {
			callback.accept(tier);
			return this;
		}

		@SuppressWarnings("unchecked")
		public Builder addSpell(Holder<AbstractSpell> spell, int level) {
			spellDataRegistryHolders.add(new SpellDataRegistryHolder((Supplier<AbstractSpell>) spell, level));
			return this;
		}

		@Override
		public CustomMagicSwordItem createObject() {
			return new CustomMagicSwordItem(this);
		}
	}

	public static class TierBuilder {
		private final List<AttributeContainer> attributes = new ArrayList<>();
		private ExtendedWeaponTierEnum existingTier = null;
		private boolean mergeAttributes = false;
		private Integer uses;
		private Float damage;
		private Float speed;
		private Integer enchantmentValue;
		private TagKey<Block> incorrectBlocksForDrops;
		private Supplier<Ingredient> repairIngredient;
		private ExtendedWeaponTier tier;

		private static <A, B> A weirdFunction(@Nullable A receivingVar, @Nullable B optional, Function<B, A> getIfTrue, A defaultValue) {
			return receivingVar == null ? optional != null ? getIfTrue.apply(optional) : defaultValue : receivingVar;
		}

		public TierBuilder useBaseTier(ExtendedWeaponTierEnum tier, boolean mergeAttributes) {
			this.existingTier = tier;
			this.mergeAttributes = mergeAttributes;
			return this;
		}

		public TierBuilder setUses(int uses) {
			this.uses = uses;
			return this;
		}

		public TierBuilder setDamage(float damage) {
			this.damage = damage;
			return this;
		}

		public TierBuilder setSpeed(float speed) {
			this.speed = speed;
			return this;
		}

		public TierBuilder setEnchantmentValue(int enchantmentValue) {
			this.enchantmentValue = enchantmentValue;
			return this;
		}

		public TierBuilder setIncorrectBlocksForDrops(TagKey<Block> incorrectBlocksForDrops) {
			this.incorrectBlocksForDrops = incorrectBlocksForDrops;
			return this;
		}

		public TierBuilder setRepairIngredient(Supplier<Ingredient> repairIngredient) {
			this.repairIngredient = repairIngredient;
			return this;
		}

		public TierBuilder addAttribute(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
			this.attributes.add(new AttributeContainer(attribute, value, operation));
			return this;
		}

		@HideFromJS
		private ExtendedWeaponTier build() {
			if (tier == null) {
				if (existingTier != null && mergeAttributes) {
					this.attributes.addAll(List.of(existingTier.tier.getAdditionalAttributes()));
				}
				uses = weirdFunction(uses, existingTier, (t) -> t.tier.getUses(), 1000);
				damage = (Float) weirdFunction(damage, existingTier, (t) -> t.tier.getAttackDamageBonus(), 6);
				speed = (Float) weirdFunction(speed, existingTier, (t) -> t.tier.getSpeed(), -3);
				enchantmentValue = weirdFunction(enchantmentValue, existingTier, (t) -> t.tier.getEnchantmentValue(), 4);
				incorrectBlocksForDrops = weirdFunction(incorrectBlocksForDrops, existingTier, (t) -> t.tier.getIncorrectBlocksForDrops(), BlockTags.INCORRECT_FOR_IRON_TOOL);
				repairIngredient = weirdFunction(repairIngredient, existingTier, (t) -> t.tier::getRepairIngredient, () -> Ingredient.of(Items.IRON_INGOT));

				tier = new ExtendedWeaponTier(uses, damage, speed, enchantmentValue, incorrectBlocksForDrops, repairIngredient, attributes.toArray(new AttributeContainer[0]));
			}
			return tier;
		}

		public enum ExtendedWeaponTierEnum {
			HELLRAZOR(ExtendedWeaponTier.HELLRAZOR),
			LEGIONNAIRE_FLAMBERGE(ExtendedWeaponTier.LEGIONNAIRE_FLAMBERGE),
			DECREPIT_FLAMBERGE(ExtendedWeaponTier.DECREPIT_FLAMBERGE),
			DECREPIT_SCYTHE(ExtendedWeaponTier.DECREPIT_SCYTHE),
			DREADSWORD(ExtendedWeaponTier.DREADSWORD),
			MISERY(ExtendedWeaponTier.MISERY),
			METAL_MAGEHUNTER(ExtendedWeaponTier.METAL_MAGEHUNTER),
			CRYSTAL_MAGEHUNTER(ExtendedWeaponTier.CRYSTAL_MAGEHUNTER),
			SPELLBREAKER(ExtendedWeaponTier.SPELLBREAKER),
			TRUTHSEEKER(ExtendedWeaponTier.TRUTHSEEKER),
			CLAYMORE(ExtendedWeaponTier.CLAYMORE),
			AMETHYST_RAPIER(ExtendedWeaponTier.AMETHYST_RAPIER);

			private final ExtendedWeaponTier tier;

			ExtendedWeaponTierEnum(ExtendedWeaponTier tier) {
				this.tier = tier;
			}
		}
	}
}
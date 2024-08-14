package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.squoshi.irons_spells_js.IronsSpellsJSMod.MODDED_REGISTRIES;

@SuppressWarnings("unused")
public class CustomMagicSwordItem extends MagicSwordItem {
    public CustomMagicSwordItem(Builder b) {
        super(b.tier.build(), b.properties, b.spellDataRegistryHolders.toArray(new SpellDataRegistryHolder[]{}));
    }

    public static class Builder extends BuilderBase<CustomMagicSwordItem> {
        private final TierBuilder tier = new TierBuilder();
        private final Properties properties = new Properties();
        private final List<SpellDataRegistryHolder> spellDataRegistryHolders = new ArrayList<>();

        public Builder(ResourceLocation id) {
            super(id);
        }

        public Builder modifyTier(Consumer<TierBuilder> callback) {
            callback.accept(tier);
            return this;
        }

        public Builder modifyProperties(Consumer<Properties> callback) {
            callback.accept(properties);
            return this;
        }

        public Builder addDefaultSpell(Holder<AbstractSpell> spell, int level) {
            spellDataRegistryHolders.add(new SpellDataRegistryHolder((Supplier<AbstractSpell>) spell, level));
            return this;
        }

        @Override
        public CustomMagicSwordItem createObject() {
            this.properties.attributes(ExtendedSwordItem.createAttributes(this.tier.build()));
            return new CustomMagicSwordItem(this);
        }
    }

    public static class TierBuilder {
        private final List<AttributeContainer> attributes = new ArrayList<>();
        private int uses = 1000;
        private float damage = 6;
        private float speed = -3;
        private int enchantmentValue = 4;
        private TagKey<Block> incorrectBlocksForDrops = BlockTags.INCORRECT_FOR_IRON_TOOL;
        private Supplier<Ingredient> repairIngredient = () -> Ingredient.of(Items.IRON_INGOT);
        private ExtendedWeaponTier tier;

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

        public TierBuilder addAdditionalAttribute(AttributeContainer attribute) {
            this.attributes.add(attribute);
            return this;
        }

        @HideFromJS
        private ExtendedWeaponTier build() {
            if (tier == null) {
                tier = new ExtendedWeaponTier(uses, damage, speed, enchantmentValue, incorrectBlocksForDrops, repairIngredient, attributes.toArray(attributes.toArray(new AttributeContainer[0])));
            }
            return tier;
        }
    }
}
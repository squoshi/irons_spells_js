package com.squoshi.irons_spells_js.item;

import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.ExtendedWeaponTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CustomMagicSwordItem extends MagicSwordItem {
    private SpellHolder[] spellHolders;
    private List<SpellData> customSpells = null;

    public CustomMagicSwordItem(Builder b) {
        super(b.tier.build(), b.createItemProperties(), new SpellDataRegistryHolder[0]);
        this.spellHolders = b.pendingSpells.toArray(new SpellHolder[0]);
    }

    @Override
    public List<SpellData> getSpells() {
        if (spellHolders != null) {
            customSpells = Arrays.stream(spellHolders)
                    .map(h -> new SpellData(SpellRegistry.getSpell(h.spell()), h.level()))
                    .toList();
            spellHolders = null;
        }
        return customSpells != null ? customSpells : List.of();
    }

    public record SpellHolder(ResourceLocation spell, int level) {}

    public static class Builder extends ItemBuilder {
        private final TierBuilder tier = new TierBuilder();
        private final List<SpellHolder> pendingSpells = new ArrayList<>();

        public Builder(ResourceLocation id) {
            super(id);
        }

        public Builder setTier(Consumer<TierBuilder> callback) {
            callback.accept(tier);
            return this;
        }

        public Builder addSpell(ISSKJSUtils.SpellHolder spell, int level) {
            pendingSpells.add(new SpellHolder(spell.getLocation(), level));
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

        public TierBuilder setRepairIngredient(Supplier<Ingredient> repairIngredient) {
            this.repairIngredient = repairIngredient;
            return this;
        }

        public TierBuilder addAttribute(String attribute, double value, AttributeModifier.Operation operation) {
            var rl = ResourceLocation.parse(attribute);
            this.attributes.add(new AttributeContainer(() -> BuiltInRegistries.ATTRIBUTE.get(rl), value, operation));
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
                repairIngredient = weirdFunction(repairIngredient, existingTier, (t) -> t.tier::getRepairIngredient, () -> Ingredient.of(Items.IRON_INGOT));

                tier = new ExtendedWeaponTier(uses, damage, speed, enchantmentValue, repairIngredient, attributes.toArray(new AttributeContainer[0]));
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

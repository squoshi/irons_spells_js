package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class CustomStaff extends StaffItem {
    private final Builder builder;

    private CustomStaff(Builder builder) {
        super(builder.createItemProperties(), builder.tierBuilder.build());
        this.builder = builder;
    }

    @Override
    public int getEnchantmentValue() {
        return builder.enchantmentValue;
    }

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
            private StaffTier tier = null;
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

            public StaffTierBuilder addAttribute(String attribute, double value, AttributeModifier.Operation operation) {
                var rl = ResourceLocation.parse(attribute);
                this.attributes.add(new AttributeContainer(() -> BuiltInRegistries.ATTRIBUTE.get(rl), value, operation));
                return this;
            }

            public StaffTierBuilder useBaseTier(StaffTierEnum tier, boolean mergeAttributes) {
                this.existingTier = tier;
                this.mergeAttributes = mergeAttributes;
                return this;
            }

            @HideFromJS
            private StaffTier build() {
                if (tier == null) {
                    if (existingTier != null && mergeAttributes) {
                        attributes.addAll(List.of(existingTier.getTier().getAdditionalAttributes()));
                    }
                    damage = weirdFunction(damage, existingTier, (t) -> t.getTier().getAttackDamageBonus(), 2F);
                    speed = weirdFunction(speed, existingTier, (t) -> t.getTier().getSpeed(), -3F);

                    tier = new StaffTier(damage, speed, attributes.toArray(new AttributeContainer[0]));
                }
                return tier;
            }
        }
    }
    public static <A, B> A weirdFunction(@Nullable A receivingVar, @Nullable B optional, Function<B, A> getIfTrue, A defaultValue) {
        return receivingVar == null ? optional != null ? getIfTrue.apply(optional) : defaultValue : receivingVar;
    }
}



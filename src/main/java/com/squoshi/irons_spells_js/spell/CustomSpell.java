package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CustomSpell extends AbstractSpell {
    @FunctionalInterface
    public interface CastCallback {
        void apply(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData);
    }
    @FunctionalInterface
    public interface CastClientCallback {
        void apply(Level world, int spellLevel, LivingEntity entity, ICastData castData);
    }

    private final ResourceLocation spellResource;
    private final DefaultConfig defaultConfig;
    private final CastType castType;
    private final SoundEvent startSound, finishSound;
    private final CastCallback onCast;
    private final CastClientCallback onClientCast;

    public CustomSpell(Builder b) {
        this.spellResource = b.spellResource;
        this.defaultConfig = new DefaultConfig()
                .setMinRarity(b.minRarity)
                .setSchoolResource(b.school)
                .setMaxLevel(b.maxLevel)
                .setCooldownSeconds(b.cooldownSeconds)
                .build();
        this.castType = b.castType;
        this.startSound = b.startSound;
        this.finishSound = b.finishSound;
        this.onCast = b.onCast;
        this.onClientCast = b.onClientCast;
        this.manaCostPerLevel = b.manaCostPerLevel;
        this.baseSpellPower = b.baseSpellPower;
        this.spellPowerPerLevel = b.spellPowerPerLevel;
        this.castTime = b.castTime;
        this.baseManaCost = b.baseManaCost;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellResource;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return castType;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return startSound != null ? Optional.of(startSound) : super.getCastStartSound();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return finishSound != null ? Optional.of(finishSound) : super.getCastFinishSound();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (onCast != null) {
            try {
                onCast.apply(level, spellLevel, entity, castSource, playerMagicData);
            } catch (Exception e){
                ConsoleJS.STARTUP.error(e);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        if (onClientCast != null) {
            try {
                onClientCast.apply(level, spellLevel, entity, castData);
            } catch (Exception e){
                ConsoleJS.STARTUP.error(e);
            }
        }
        super.onClientCast(level, spellLevel, entity, castData);
    }

    public static class Builder extends BuilderBase<CustomSpell> {
        private SpellRarity minRarity = SpellRarity.COMMON;
        private ResourceLocation school = SchoolRegistry.BLOOD_RESOURCE;
        private int maxLevel = 10;
        private int cooldownSeconds = 20;
        private CastType castType = CastType.INSTANT;
        private SoundEvent startSound = null;
        private SoundEvent finishSound = null;
        private final ResourceLocation spellResource;
        private CastCallback onCast = null;
        private CastClientCallback onClientCast = null;
        private int manaCostPerLevel = 20;
        private int baseSpellPower = 0;
        private int spellPowerPerLevel = 1;
        private int castTime = 0;
        private int baseManaCost = 40;

        public Builder(ResourceLocation i) {
            super(i);
            this.spellResource = i;
        }

        @SuppressWarnings("unused")
        public Builder setCastType(CastType type) {
            this.castType = type;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setStartSound(SoundEvent soundEvent) {
            this.startSound = soundEvent;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setFinishSound(SoundEvent soundEvent) {
            this.finishSound = soundEvent;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setMinRarity(SpellRarity rarity) {
            this.minRarity = rarity;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setSchool(ResourceLocation schoolResource) {
            this.school = schoolResource;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setMaxLevel(int level) {
            this.maxLevel = level;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCooldownSeconds(int seconds) {
            this.cooldownSeconds = seconds;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setManaCostPerLevel(int cost) {
            this.manaCostPerLevel = cost;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setBaseSpellPower(int power) {
            this.baseSpellPower = power;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setSpellPowerPerLevel(int power) {
            this.spellPowerPerLevel = power;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCastTime(int time) {
            this.castTime = time;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setBaseManaCost(int cost) {
            this.baseManaCost = cost;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder onCast(CastCallback consumer) {
            this.onCast = consumer;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder onClientCast(CastClientCallback consumer) {
            this.onClientCast = consumer;
            return this;
        }

        @Override
        public RegistryInfo getRegistryType() {
            return IronsSpellsJSPlugin.SPELL_REGISTRY;
        }

        @Override
        public CustomSpell createObject() {
            return new CustomSpell(this);
        }
    }
}

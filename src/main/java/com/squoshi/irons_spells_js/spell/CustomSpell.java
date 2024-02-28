package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
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

    private CustomSpell(ResourceLocation spellResource, DefaultConfig defaultConfig, CastType castType,SoundEvent startSound, SoundEvent finishSound, CastCallback onCast, CastClientCallback onClientCast) {
        this.spellResource = spellResource;
        this.defaultConfig = defaultConfig;
        this.castType = castType;
        this.startSound = startSound;
        this.finishSound = finishSound;
        this.onCast = onCast;
        this.onClientCast = onClientCast;
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
            onCast.apply(level, spellLevel, entity, castSource, playerMagicData);
        } else {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
        }
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        if (onClientCast != null) {
            onClientCast.apply(level, spellLevel, entity, castData);
        } else {
            super.onClientCast(level, spellLevel, entity, castData);
        }
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

        public Builder setMinRarity(SpellRarity rarity) {
            this.minRarity = rarity;
            return this;
        }

        public Builder setSchool(ResourceLocation schoolResource) {
            this.school = schoolResource;
            return this;
        }

        public Builder setMaxLevel(int level) {
            this.maxLevel = level;
            return this;
        }

        public Builder setCooldownSeconds(int seconds) {
            this.cooldownSeconds = seconds;
            return this;
        }

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
            final DefaultConfig config = new DefaultConfig().setMinRarity(minRarity).setSchoolResource(school).setMaxLevel(maxLevel).setCooldownSeconds(cooldownSeconds).build();
            return new CustomSpell(spellResource, config, castType, startSound, finishSound, onCast, onClientCast);
        }
    }
}

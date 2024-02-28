package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import com.squoshi.irons_spells_js.mixin.ServerConfigsAccessor;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Optional;
import java.util.function.Consumer;

public class CustomSpell extends AbstractSpell {
    record CastContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, CastSource getCastSource, MagicData getPlayerMagicData){}
    record CastClientContext(Level getLevel, int getSpellLevel, LivingEntity getEntity, ICastData getCastData){}

    private final ResourceLocation spellResource;
    private final DefaultConfig defaultConfig;
    private final CastType castType;
    private final SoundEvent startSound, finishSound;
    private final Consumer<CastContext> onCast;
    private final Consumer<CastClientContext> onClientCast;

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
            var context = new CastContext(level, spellLevel, entity, castSource, playerMagicData);
            safeCallback(onCast, context,"Error while calling onCast");
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        if (onClientCast != null) {
            var context = new CastClientContext(level, spellLevel, entity, castData);
            safeCallback(onClientCast, context, "Error while calling onClientCast");
        }
        super.onClientCast(level, spellLevel, entity, castData);
    }

    private <T> boolean safeCallback(Consumer<T> consumer, T value, String errorMessage) {
        try {
            consumer.accept(value);
        } catch (Throwable e) {
            ConsoleJS.STARTUP.error(errorMessage, e);
            return false;
        }
        return true;
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
        private Consumer<CastContext> onCast = null;
        private Consumer<CastClientContext> onClientCast = null;
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
        public Builder onCast(Consumer<CastContext> consumer) {
            this.onCast = consumer;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder onClientCast(Consumer<CastClientContext> consumer) {
            this.onClientCast = consumer;
            return this;
        }

        @Override
        public RegistryInfo getRegistryType() {
            return IronsSpellsJSPlugin.SPELL_REGISTRY;
        }

        @Override
        public CustomSpell createObject() {
            var spell = new CustomSpell(this);
            ServerConfigsAccessor.getBuilder().push("Spells");
            ServerConfigsAccessor.invoke$createSpellConfig(spell);
            ServerConfigsAccessor.getBuilder().pop();
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigsAccessor.getBuilder().build(), String.format("%s-server.toml", IronsSpellbooks.MODID));
            return spell;
        }
    }
}

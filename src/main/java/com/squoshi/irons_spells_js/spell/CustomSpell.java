package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.IronsSpellsJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
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

        @Info(value = """
            Sets the cast type. Can be `CONTINUOUS`, `INSTANT`, `LONG`, or `NONE`.
        """)
        @SuppressWarnings("unused")
        public Builder setCastType(CastType type) {
            this.castType = type;
            return this;
        }

        @Info(value = """
            Sets the sound that the spell will play when it starts casting.
        """)
        @SuppressWarnings("unused")
        public Builder setStartSound(SoundEvent soundEvent) {
            this.startSound = soundEvent;
            return this;
        }

        @Info(value = """
            Sets the sound that the spell will play after it is done casting.
        """)
        @SuppressWarnings("unused")
        public Builder setFinishSound(SoundEvent soundEvent) {
            this.finishSound = soundEvent;
            return this;
        }

        @Info(value = """
            Sets the rarity of the spell. Can be `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, or `LEGENDARY`.
        """)
        @SuppressWarnings("unused")
        public Builder setMinRarity(SpellRarity rarity) {
            this.minRarity = rarity;
            return this;
        }

        @Info(value = """
            Sets the school of the spell. The different schools each are a resource location.
            
            Example: `.setSchool(SchoolRegistry.BLOOD_RESOURCE`
            Another example: `setSchool('irons_spellbooks:blood')`
        """)
        @SuppressWarnings("unused")
        public Builder setSchool(ResourceLocation schoolResource) {
            this.school = schoolResource;
            return this;
        }

        @Info(value = """
            Sets the max level of the spell. Goes up to `10` from `1`.
        """)
        @SuppressWarnings("unused")
        public Builder setMaxLevel(int level) {
            this.maxLevel = level;
            return this;
        }

        @Info(value = """
            Sets the cooldown of the spell in seconds. Cannot be a decimal value for some reason.
        """)
        @SuppressWarnings("unused")
        public Builder setCooldownSeconds(int seconds) {
            this.cooldownSeconds = seconds;
            return this;
        }

        @Info(value = """
            Sets the mana cost per the spell's level. For example, you could input `10` into this method, and each level of the spell will multiply that value by the level.
        """)
        @SuppressWarnings("unused")
        public Builder setManaCostPerLevel(int cost) {
            this.manaCostPerLevel = cost;
            return this;
        }

        @Info(value = """
            Sets the base spell power. Can be from `1` to `10`. The spell power per level adds on to this.
        """)
        @SuppressWarnings("unused")
        public Builder setBaseSpellPower(int power) {
            this.baseSpellPower = power;
            return this;
        }

        @Info(value = """
            Sets the spell power per level.
        """)
        @SuppressWarnings("unused")
        public Builder setSpellPowerPerLevel(int power) {
            this.spellPowerPerLevel = power;
            return this;
        }

        @Info(value = """
            Sets the cast time. This is used for `LONG` or `CONTINUOUS` spell types.
        """)
        @SuppressWarnings("unused")
        public Builder setCastTime(int time) {
            this.castTime = time;
            return this;
        }

        @Info(value = """
            Sets the base mana cost. The mana cost per level adds on to this.
        """)
        @SuppressWarnings("unused")
        public Builder setBaseManaCost(int cost) {
            this.baseManaCost = cost;
            return this;
        }

        @Info(value = """
            Sets the callback for when the spell is cast. This is what the spell does when it is casted.
        """)
        @SuppressWarnings("unused")
        public Builder onCast(Consumer<CastContext> consumer) {
            this.onCast = consumer;
            return this;
        }

        @Info(value = """
            Sets the callback for when the spell is cast on the client side. This is what the spell does when it is casted.
        """)
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
            return new CustomSpell(this);
        }
    }
}

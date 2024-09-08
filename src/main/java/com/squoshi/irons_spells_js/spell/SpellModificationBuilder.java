package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.event.EventJS;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class SpellModificationBuilder extends EventJS {
    public record ModifiedClientCastCallback(Level level, int spellLevel, LivingEntity entity, ICastData castData) {}
    public record ModifiedServerCastCallback(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {}
    public record ModifiedPreCastConditionsCallback(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {}
    public record ModifiedServerCastCompleteCallback(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {}
    public record ModifiedClientPreCastCallback(Level level, int spellLevel, LivingEntity entity, InteractionHand hand, MagicData playerMagicData) {}
    public record ModifiedServerPreCastCallback(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {}

    private final ResourceLocation spellResource;
    public transient Function<Integer, Integer> castTimeCallback;
    public transient CastType castType;
    public transient Optional<SoundEvent> startSound, finishSound;
    public transient AnimationHolder castStartAnimation, castFinishAnimation;
    public transient Optional<Integer> recastCount = Optional.empty();
    public transient Consumer<ModifiedClientCastCallback> setClientCastCallback;
    public transient boolean cancelClientCast = false;
    public transient Consumer<ModifiedServerCastCallback> setServerCastCallback;
    public transient boolean cancelServerCast = false;
    public transient Predicate<ModifiedPreCastConditionsCallback> setPreCastConditionsCallback;
    public transient Consumer<ModifiedServerCastCompleteCallback> setServerCastCompleteCallback;
    public transient boolean cancelServerCastComplete = false;
    public transient Consumer<ModifiedClientPreCastCallback> setClientPreCastCallback;
    public transient boolean cancelClientPreCast = false;
    public transient Consumer<ModifiedServerPreCastCallback> setServerPreCastCallback;
    public transient boolean cancelServerPreCast = false;
    public transient Predicate<Player> isLearnedCallback;
    public transient BiFunction<Integer, LivingEntity, List<MutableComponent>> customUniqueInfo;

    public SpellModificationBuilder(ResourceLocation spellResource) {
        this.spellResource = spellResource;
    }

    public SpellModificationBuilder setCastTimeCallback(Function<Integer, Integer> callback) {
        this.castTimeCallback = callback;
        return this;
    }

    public SpellModificationBuilder setStartSound(ISSKJSUtils.SoundEventHolder startSound) {
        this.startSound = startSound != null ? Optional.ofNullable(ForgeRegistries.SOUND_EVENTS.getValue(startSound.getLocation())) : Optional.empty();
        return this;
    }

    public SpellModificationBuilder setFinishSound(ISSKJSUtils.SoundEventHolder finishSound) {
        this.finishSound = finishSound != null ? Optional.ofNullable(ForgeRegistries.SOUND_EVENTS.getValue(finishSound.getLocation())) : Optional.empty();
        return this;
    }

    public SpellModificationBuilder setCastStartAnimation(AnimationHolder animationHolder) {
        this.castStartAnimation = animationHolder;
        return this;
    }

    public SpellModificationBuilder setCastFinishAnimation(AnimationHolder animationHolder) {
        this.castFinishAnimation = animationHolder;
        return this;
    }

    public SpellModificationBuilder setRecastCount(int recastCount) {
        this.recastCount = Optional.of(recastCount);
        return this;
    }

    public SpellModificationBuilder setClientCastCallback(boolean cancelOriginal, Consumer<ModifiedClientCastCallback> setClientCastCallback) {
        this.cancelClientCast = cancelOriginal;
        this.setClientCastCallback = setClientCastCallback;
        return this;
    }

    public SpellModificationBuilder setServerCastCallback(boolean cancelOriginal, Consumer<ModifiedServerCastCallback> setServerCastCallback) {
        this.cancelServerCast = cancelOriginal;
        this.setServerCastCallback = setServerCastCallback;
        return this;
    }

    public SpellModificationBuilder setPreCastConditionsCallback(Predicate<ModifiedPreCastConditionsCallback> setPreCastConditionsCallback) {
        this.setPreCastConditionsCallback = setPreCastConditionsCallback;
        return this;
    }

    public SpellModificationBuilder setServerCastCompleteCallback(boolean cancelOriginal, Consumer<ModifiedServerCastCompleteCallback> setServerCastCompleteCallback) {
        this.cancelServerCastComplete = cancelOriginal;
        this.setServerCastCompleteCallback = setServerCastCompleteCallback;
        return this;
    }

    public SpellModificationBuilder setClientPreCastCallback(boolean cancelOriginal, Consumer<ModifiedClientPreCastCallback> setClientPreCastCallback) {
        this.cancelClientPreCast = cancelOriginal;
        this.setClientPreCastCallback = setClientPreCastCallback;
        return this;
    }

    public SpellModificationBuilder setServerPreCastCallback(boolean cancelOriginal, Consumer<ModifiedServerPreCastCallback> setServerPreCastCallback) {
        this.cancelServerPreCast = cancelOriginal;
        this.setServerPreCastCallback = setServerPreCastCallback;
        return this;
    }

    public SpellModificationBuilder setIsLearnedCallback(Predicate<Player> player) {
        this.isLearnedCallback = player;
        return this;
    }

    public SpellModificationBuilder setUniqueInfoCallback(BiFunction<Integer, LivingEntity, List<MutableComponent>> uniqueInfoCallback) {
        this.customUniqueInfo = uniqueInfoCallback;
        return this;
    }

    public AbstractSpell getSpell() {
        return SpellRegistry.getSpell(spellResource);
    }
}

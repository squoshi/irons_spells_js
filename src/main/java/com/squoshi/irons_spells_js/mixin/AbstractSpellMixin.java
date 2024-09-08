package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.events.EntitySpellCastEventJS;
import com.squoshi.irons_spells_js.events.EntitySpellPreCastEventJS;
import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.spell.SpellModificationBuilder;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import com.squoshi.irons_spells_js.util.ISpellModify;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

import static com.squoshi.irons_spells_js.events.SpellModificationEventJS.getOrCreate;


@Mixin(AbstractSpell.class)
public abstract class AbstractSpellMixin implements ISpellModify {
    @Shadow public abstract String getSpellName();

//    @Unique
//    private SpellModificationBuilder irons_spells_js$builder;
//
//    public void setBuilder(ResourceLocation resourceLocation){
//        irons_spells_js$builder = getOrCreate(resourceLocation).getBuilder();
//    }
    // ^ do we really need this?

    public SpellModificationBuilder getBuilder(){
       return getOrCreate(irons_spells_js$getSpell().getSpellResource()).getBuilder();
    }

    @Unique
    private AbstractSpell irons_spells_js$getSpell() {
        return (AbstractSpell)(Object) this;
    }

    @Inject(method = "getCastTime", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastTime(int spellLevel, CallbackInfoReturnable<Integer> cir) {
        if (getBuilder() != null && getBuilder().castTimeCallback != null) {
            cir.setReturnValue(getBuilder().castTimeCallback.apply(spellLevel));
        }
    }

    @Inject(method = "getCastStartSound", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastStartSound(CallbackInfoReturnable<Optional<SoundEvent>> cir) {
        if (getBuilder() != null && getBuilder().startSound.isPresent()) {
            cir.setReturnValue(getBuilder().startSound);
        }
    }

    @Inject(method = "getCastFinishSound", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastFinishSound(CallbackInfoReturnable<Optional<SoundEvent>> cir) {
        if (getBuilder() != null && getBuilder().finishSound.isPresent()) {
            cir.setReturnValue(getBuilder().finishSound);
        }
    }

    @Inject(method = "getCastStartAnimation", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastStartAnimation(CallbackInfoReturnable<AnimationHolder> cir) {
        if (getBuilder() != null && getBuilder().castStartAnimation != null) {
            cir.setReturnValue(getBuilder().castStartAnimation);
        }
    }

    @Inject(method = "getCastFinishAnimation", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastFinishAnimation(CallbackInfoReturnable<AnimationHolder> cir) {
        if (getBuilder() != null && getBuilder().castFinishAnimation != null) {
            cir.setReturnValue(getBuilder().castFinishAnimation);
        }
    }

    @Inject(method = "getRecastCount", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getRecastCount(CallbackInfoReturnable<Integer> cir) {
        if (getBuilder() != null && getBuilder().recastCount.isPresent()) {
            cir.setReturnValue(getBuilder().recastCount.get());
        }
    }

    @Inject(method = "onClientCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData, CallbackInfo ci) {
        if (getBuilder() != null && getBuilder().setClientCastCallback != null) {
            ISSKJSUtils.safeCallback(getBuilder().setClientCastCallback, new SpellModificationBuilder.ModifiedClientCastCallback(level, spellLevel, entity, castData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setClientCastCallback.");
        }
        if (getBuilder().cancelClientCast) {
            ci.cancel();
        }
    }

    @Inject(method = "onCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, CallbackInfo ci) {
        if (getBuilder() != null && getBuilder().setServerCastCallback != null) {
            ISSKJSUtils.safeCallback(getBuilder().setServerCastCallback, new SpellModificationBuilder.ModifiedServerCastCallback(level, spellLevel, entity, castSource, playerMagicData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setServerCastCallback.");
        }
        if (getBuilder().cancelServerCast) {
            ci.cancel();
        }
    }

    @Inject(method = "checkPreCastConditions", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, CallbackInfoReturnable<Boolean> cir) {
        boolean pass = true;
        if (getBuilder() != null && getBuilder().setPreCastConditionsCallback != null) {
            pass = ISSKJSUtils.safePredicate(getBuilder().setPreCastConditionsCallback, new SpellModificationBuilder.ModifiedPreCastConditionsCallback(level, spellLevel, entity, playerMagicData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setPreCastConditionsCallback.");
        }
        EntitySpellPreCastEventJS event = new EntitySpellPreCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (IronsSpellsJSEvents.entitySpellPreCast.hasListeners()) {
            if (!IronsSpellsJSEvents.entitySpellPreCast.post(event).pass()) {
                pass = false;
            }
        }
        cir.setReturnValue(pass);
    }

    @Inject(method = "onServerCastComplete", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled, CallbackInfo ci) {
        if (getBuilder() != null && getBuilder().setServerCastCompleteCallback != null) {
            ISSKJSUtils.safeCallback(getBuilder().setServerCastCompleteCallback, new SpellModificationBuilder.ModifiedServerCastCompleteCallback(level, spellLevel, entity, playerMagicData, cancelled), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setServerCastCompleteCallback.");
        }
        EntitySpellCastEventJS event = new EntitySpellCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (IronsSpellsJSEvents.entitySpellCast.hasListeners()) {
            IronsSpellsJSEvents.entitySpellCast.post(event);
        }
        if (getBuilder().cancelServerCastComplete) {
            ci.cancel();
        }
    }

    @Inject(method = "onClientPreCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onClientPreCast(Level level, int spellLevel, LivingEntity entity, InteractionHand hand, MagicData playerMagicData, CallbackInfo ci) {
        if (getBuilder() != null && getBuilder().setClientPreCastCallback != null) {
            ISSKJSUtils.safeCallback(getBuilder().setClientPreCastCallback, new SpellModificationBuilder.ModifiedClientPreCastCallback(level, spellLevel, entity, hand, playerMagicData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setClientPreCastCallback.");
        }
        if (getBuilder().cancelClientPreCast) {
            ci.cancel();
        }
    }

    @Inject(method = "onServerPreCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, CallbackInfo ci) {
        if (getBuilder() != null && getBuilder().setServerPreCastCallback != null) {
            ISSKJSUtils.safeCallback(getBuilder().setServerPreCastCallback, new SpellModificationBuilder.ModifiedServerPreCastCallback(level, spellLevel, entity, playerMagicData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setPreSpellCastCallback.");
        }
        if (getBuilder().cancelServerPreCast) {
            ci.cancel();
        }
    }

    @Inject(method = "isLearned", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$isLearned(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (getBuilder() != null && getBuilder().isLearnedCallback != null) {
            cir.setReturnValue(ISSKJSUtils.safePredicate(getBuilder().isLearnedCallback, player, "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: isLearnedCallback."));
        }
    }

    @Inject(method = "getUniqueInfo", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getUniqueInfo(int spellLevel, LivingEntity entity, CallbackInfoReturnable<List<MutableComponent>> cir) {
        if (getBuilder() != null && getBuilder().customUniqueInfo != null) {
            cir.setReturnValue(getBuilder().customUniqueInfo.apply(spellLevel, entity));
        }
    }
}
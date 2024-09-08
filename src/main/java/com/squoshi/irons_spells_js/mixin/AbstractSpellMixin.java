package com.squoshi.irons_spells_js.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static com.squoshi.irons_spells_js.events.SpellModificationEventJS.getOrCreate;


@Mixin(AbstractSpell.class)
public abstract class AbstractSpellMixin implements ISpellModify {
    @Shadow public abstract String getSpellName();

    @Unique
    private SpellModificationBuilder irons_spells_js$builder;

    public void irons_spells_js$setBuilder(ResourceLocation resourceLocation){
        irons_spells_js$builder = getOrCreate(resourceLocation).getBuilder();
    }

    public SpellModificationBuilder irons_spells_js$getBuilder(){
       return getOrCreate(irons_spells_js$getSpell().getSpellResource()).getBuilder();
    }

    @Unique
    private AbstractSpell irons_spells_js$getSpell() {
        return (AbstractSpell)(Object) this;
    }

    @Inject(method = "getCastTime", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastTime(int spellLevel, CallbackInfoReturnable<Integer> cir) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().castTimeCallback != null) {
            cir.setReturnValue(irons_spells_js$convertToInteger(irons_spells_js$getBuilder().castTimeCallback.apply(spellLevel)));
        }
    }

    @Inject(method = "getCastStartSound", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastStartSound(CallbackInfoReturnable<Optional<SoundEvent>> cir) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().startSound.isPresent()) {
            cir.setReturnValue(irons_spells_js$getBuilder().startSound);
        }
    }

    @Inject(method = "getCastFinishSound", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastFinishSound(CallbackInfoReturnable<Optional<SoundEvent>> cir) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().finishSound.isPresent()) {
            cir.setReturnValue(irons_spells_js$getBuilder().finishSound);
        }
    }

    @Inject(method = "getCastStartAnimation", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastStartAnimation(CallbackInfoReturnable<AnimationHolder> cir) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().castStartAnimation != null) {
            cir.setReturnValue(irons_spells_js$getBuilder().castStartAnimation);
        }
    }

    @Inject(method = "getCastFinishAnimation", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getCastFinishAnimation(CallbackInfoReturnable<AnimationHolder> cir) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().castFinishAnimation != null) {
            cir.setReturnValue(irons_spells_js$getBuilder().castFinishAnimation);
        }
    }

    @Inject(method = "getRecastCount", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$getRecastCount(CallbackInfoReturnable<Integer> cir) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().recastCount.isPresent()) {
            cir.setReturnValue(irons_spells_js$getBuilder().recastCount.get());
        }
    }

    @Inject(method = "onClientCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData, CallbackInfo ci) {
        if (irons_spells_js$getBuilder() == null || irons_spells_js$getBuilder().setClientCastCallback == null) return;
        if (irons_spells_js$getBuilder().cancelClientCast) {
            ISSKJSUtils.safeCallback(irons_spells_js$getBuilder().setClientCastCallback, new SpellModificationBuilder.ModifiedClientCastCallback(level, spellLevel, entity, castData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setClientCastCallback.");
            ci.cancel();
        } else {
            ISSKJSUtils.safeCallback(irons_spells_js$getBuilder().setClientCastCallback, new SpellModificationBuilder.ModifiedClientCastCallback(level, spellLevel, entity, castData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setClientCastCallback.");
        }
    }

    @WrapOperation(
            method = "castSpell",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"
            ),
            remap = false
    )
    private void irons_spells_js$queryCancelOnCast(AbstractSpell instance, Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData, Operation<Void> original) {
        if (irons_spells_js$getBuilder() == null || irons_spells_js$getBuilder().setServerCastCallback == null) {
            original.call(instance, level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        SpellModificationBuilder.ModifiedServerCastCallback modifiedServerCastCallback = new SpellModificationBuilder.ModifiedServerCastCallback(level, spellLevel, entity, castSource, playerMagicData);
        if (irons_spells_js$getBuilder().cancelServerCast) {
            ISSKJSUtils.safeCallback(irons_spells_js$getBuilder().setServerCastCallback, modifiedServerCastCallback, "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setServerCastCallback.");
        } else {
            ISSKJSUtils.safeCallback(irons_spells_js$getBuilder().setServerCastCallback, modifiedServerCastCallback, "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setServerCastCallback.");
            original.call(instance, level, spellLevel, entity, castSource, playerMagicData);
        }
    }

    @Inject(method = "checkPreCastConditions", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, CallbackInfoReturnable<Boolean> cir) {
        boolean pass = true;
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().setPreCastConditionsCallback != null) {
            pass = ISSKJSUtils.safePredicate(irons_spells_js$getBuilder().setPreCastConditionsCallback, new SpellModificationBuilder.ModifiedPreCastConditionsCallback(level, spellLevel, entity, playerMagicData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setPreCastConditionsCallback.");
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
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().setServerCastCompleteCallback != null) {
            ISSKJSUtils.safeCallback(irons_spells_js$getBuilder().setServerCastCompleteCallback, new SpellModificationBuilder.ModifiedServerCastCompleteCallback(level, spellLevel, entity, playerMagicData, cancelled), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setServerCastCompleteCallback.");
        }
        EntitySpellCastEventJS event = new EntitySpellCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (IronsSpellsJSEvents.entitySpellCast.hasListeners()) {
            IronsSpellsJSEvents.entitySpellCast.post(event);
        }
        if (irons_spells_js$getBuilder().cancelServerCastComplete) {
            ci.cancel();
        }
    }

    @Inject(method = "onClientPreCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onClientPreCast(Level level, int spellLevel, LivingEntity entity, InteractionHand hand, MagicData playerMagicData, CallbackInfo ci) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().setClientPreCastCallback != null) {
            ISSKJSUtils.safeCallback(irons_spells_js$getBuilder().setClientPreCastCallback, new SpellModificationBuilder.ModifiedClientPreCastCallback(level, spellLevel, entity, hand, playerMagicData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setClientPreCastCallback.");
        }
        if (irons_spells_js$getBuilder().cancelClientPreCast) {
            ci.cancel();
        }
    }

    @Inject(method = "onServerPreCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, CallbackInfo ci) {
        if (irons_spells_js$getBuilder() != null && irons_spells_js$getBuilder().setServerPreCastCallback != null) {
            ISSKJSUtils.safeCallback(irons_spells_js$getBuilder().setServerPreCastCallback, new SpellModificationBuilder.ModifiedServerPreCastCallback(level, spellLevel, entity, playerMagicData), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setPreSpellCastCallback.");
        }
        if (irons_spells_js$getBuilder().cancelServerPreCast) {
            ci.cancel();
        }
    }

    // util:
    @Unique
    private static Integer irons_spells_js$convertToInteger(Object input) {
        if (input instanceof Integer) {
            return (Integer) input;
        } else if (input instanceof Double || input instanceof Float) {
            return ((Number) input).intValue();
        } else {
            return null;
        }
    }
}
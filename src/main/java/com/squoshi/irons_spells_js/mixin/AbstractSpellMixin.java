package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.events.EntitySpellCastEventJS;
import com.squoshi.irons_spells_js.events.EntitySpellPreCastEventJS;
import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.spell.SpellModificationBuilder;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import com.squoshi.irons_spells_js.util.ISpellModify;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.squoshi.irons_spells_js.events.SpellModificationEventJS.getOrCreate;


@Mixin(AbstractSpell.class)
public abstract class AbstractSpellMixin implements ISpellModify {
    @Shadow public abstract String getSpellName();

    @Unique
    private SpellModificationBuilder irons_spells_js$builder;

    public void setBuilder(ResourceLocation resourceLocation){
        irons_spells_js$builder = getOrCreate(resourceLocation).getBuilder();
    }

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

    // private void irons_spells_js$getCastType() {}

    @Inject(method = "onServerPreCast", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, CallbackInfo ci) {
        if (getBuilder() != null && getBuilder().setPreSpellCastCallback != null) {
            ISSKJSUtils.safeCallback(getBuilder().setPreSpellCastCallback, irons_spells_js$getSpell(), "[KubeJS Irons Spells]: Error in " + getSpellName() + "builder for field: setPreSpellCastCallback.");
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

    @Inject(method = "onServerCastComplete", at = @At("HEAD"), remap = false)
    private void irons_spells_js$onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled, CallbackInfo ci) {
        EntitySpellCastEventJS event = new EntitySpellCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (IronsSpellsJSEvents.entitySpellCast.hasListeners()) {
            IronsSpellsJSEvents.entitySpellCast.post(event);
        }
    }
}
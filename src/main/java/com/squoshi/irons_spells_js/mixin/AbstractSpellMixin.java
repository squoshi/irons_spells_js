package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.events.EntitySpellCastEventJS;
import com.squoshi.irons_spells_js.events.EntitySpellPreCastEventJS;
import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSpell.class)
public class AbstractSpellMixin {
    @Inject(method = "checkPreCastConditions", at = @At("HEAD"), remap = false, cancellable = true)
    private void irons_spells_js$postEntitySpellPreCastEvent(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, CallbackInfoReturnable<Boolean> cir) {
        EntitySpellPreCastEventJS event = new EntitySpellPreCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (IronsSpellsJSEvents.entitySpellPreCast.hasListeners()) {
            if (!IronsSpellsJSEvents.entitySpellPreCast.post(event).pass()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "onServerCastComplete", at = @At("HEAD"), remap = false)
    private void irons_spells_js$postEntitySpellCastEvent(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled, CallbackInfo ci) {
        EntitySpellCastEventJS event = new EntitySpellCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (IronsSpellsJSEvents.entitySpellCast.hasListeners()) {
            IronsSpellsJSEvents.entitySpellCast.post(event);
        }
    }
}
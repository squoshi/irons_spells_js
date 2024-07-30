package com.squoshi.irons_spells_js.mixin;

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

@Mixin(AbstractSpell.class)
public class AbstractSpellMixin {
    @Inject(method = "onServerPreCast", at = @At("HEAD"), remap = false)
    private void onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, CallbackInfo ci) {
        EntitySpellPreCastEventJS event = new EntitySpellPreCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (IronsSpellsJSEvents.entitySpellPreCast.hasListeners()) {
            IronsSpellsJSEvents.entitySpellPreCast.post(event);
        }
    }

    @Inject(method = "onServerCastComplete", at = @At("HEAD"), remap = false)
    private void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled, CallbackInfo ci) {
        EntitySpellPreCastEventJS event = new EntitySpellPreCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
        if (cancelled) return;
        if (IronsSpellsJSEvents.entitySpellCast.hasListeners()) {
            IronsSpellsJSEvents.entitySpellCast.post(event);
        }
    }
}

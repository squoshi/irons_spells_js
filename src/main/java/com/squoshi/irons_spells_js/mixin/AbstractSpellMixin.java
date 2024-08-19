package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.event.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.event.SpellPostCastEventJS;
import com.squoshi.irons_spells_js.event.SpellPreCastEventJS;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.EntityType;
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
		var entityKey = entity.getType().kjs$getKey();
		if (entityKey == EntityType.PLAYER.kjs$getKey()) {
			return; // we already deal with player at normal event
		}
		if (IronsSpellsJSEvents.spellPreCast.hasListeners(entityKey)) {
			SpellPreCastEventJS event = new SpellPreCastEventJS(entity, ((AbstractSpell) (Object) this).getSpellId(), spellLevel, playerMagicData.getCastingSpell().getSpell().getSchoolType(), playerMagicData.getCastSource());
			if (IronsSpellsJSEvents.spellPreCast.post(entity, entityKey, event).applyCancel(event)) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "onServerCastComplete", at = @At("HEAD"), remap = false)
	private void irons_spells_js$postEntitySpellCastEvent(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled, CallbackInfo ci) {
		var entityKey = entity.getType().kjs$getKey();
		if (IronsSpellsJSEvents.spellPostCast.hasListeners(entityKey)) {
			SpellPostCastEventJS event = new SpellPostCastEventJS(entity, (AbstractSpell) (Object) this, spellLevel, playerMagicData);
			IronsSpellsJSEvents.spellPostCast.post(event);
		}
	}
}
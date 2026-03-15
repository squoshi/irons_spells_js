package com.squoshi.irons_spells_js.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.events.ManaRegenEventJS;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = MagicManager.class)
public class MagicManagerMixin {
    @ModifyArg(
            method = "regenPlayerMana",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;setMana(F)V"
            ),
            remap = false
    )
    private float grabIncrementValue(
            float mana,
            @Local(argsOnly = true) ServerPlayer serverPlayer,
            @Local int playerMaxMana,
            @Local(ordinal = 0) float playerMana,
            @Local(ordinal = 2) float increment
    ) {
        float inc = increment;
        if (playerMana + inc > playerMaxMana) {
            inc = playerMaxMana - playerMana;
        }
        ManaRegenEventJS event = new ManaRegenEventJS(serverPlayer, inc);
        if (IronsSpellsJSEvents.manaRegen.hasListeners()) {
            if (!IronsSpellsJSEvents.manaRegen.post(event).pass()) return 0;
            inc = event.getAmount();
        }
        if (IronsSpellsJSEvents.manaRegenPlayerEvents.hasListeners()) {
            if (!IronsSpellsJSEvents.manaRegenPlayerEvents.post(event).pass()) return 0;
            inc = event.getAmount();
        }
        return playerMana + inc;
    }
}
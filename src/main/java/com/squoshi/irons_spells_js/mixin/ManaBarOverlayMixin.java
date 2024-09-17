package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ManaBarOverlay.class)
public class ManaBarOverlayMixin {
    @Inject(method = "shouldShowManaBar", at = @At("HEAD"), cancellable = true, remap = false)
    private static void irons_spells_js$shouldShowManaBar(Player player, CallbackInfoReturnable<Boolean> cir) {
        for (Item item : IronsSpellsJSMod.MANA_BAR_ITEMS) {
            if (player.isHolding(item)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
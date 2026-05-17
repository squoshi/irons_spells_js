package com.squoshi.irons_spells_js.mixin;

import com.google.gson.JsonElement;
import com.squoshi.irons_spells_js.util.CustomSpellConfigEntriesJS;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = SpellConfigManager.class, remap = false)
public class SpellConfigManagerMixin {
    @Inject(method = "buildConfigManager", at = @At("HEAD"))
    private void irons_spells_js$addCustomSpellConfigs(Map<ResourceLocation, JsonElement> configEntries, CallbackInfoReturnable<Boolean> cir) {
        CustomSpellConfigEntriesJS.addSpellEntries(configEntries);
    }
}

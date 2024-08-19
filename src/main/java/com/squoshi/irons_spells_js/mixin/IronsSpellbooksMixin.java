package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = IronsSpellbooks.class, remap = false)
public class IronsSpellbooksMixin {

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/ModContainer;registerConfig(Lnet/neoforged/fml/config/ModConfig$Type;Lnet/neoforged/fml/config/IConfigSpec;Ljava/lang/String;)V", ordinal = 1))
	private void kjs_irons_spells$cancelConfig(ModContainer instance, ModConfig.Type type, IConfigSpec configSpec, String fileName) {
		IronsSpellsJSMod.LOGGER.debug("Postponing IronSpells server config...");
	}
}
package com.squoshi.irons_spells_js.compat.entityjs;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellCastingMobJSBuilder;
import com.squoshi.irons_spells_js.compat.entityjs.entity.builder.SpellProjectileJSBuilder;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import net.minecraft.core.registries.Registries;

public class EntityJSPlugin implements KubeJSPlugin {
    @Override
	public void registerBuilderTypes(BuilderTypeRegistry registry) {
		registry.of(Registries.ENTITY_TYPE, reg -> {
			reg.add(IronsSpellsJSMod.id("spellcasting"), SpellCastingMobJSBuilder.class, SpellCastingMobJSBuilder::new);
			reg.add(IronsSpellsJSMod.id("spell_projectile"), SpellProjectileJSBuilder.class, SpellProjectileJSBuilder::new);
		});
	}
}
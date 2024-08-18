package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.entity.attribute.SpellAttributeBuilderJS;
import com.squoshi.irons_spells_js.item.CustomMagicSwordItem;
import com.squoshi.irons_spells_js.item.CustomSpellBook;
import com.squoshi.irons_spells_js.item.CustomStaff;
import com.squoshi.irons_spells_js.spell.CustomSpell;
import com.squoshi.irons_spells_js.spell.school.SchoolTypeJSBuilder;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.core.registries.Registries;

public final class IronsSpellsJSPlugin implements KubeJSPlugin {

	@Override
	public void init() {
		IronsSpellsJSMod.LOGGER.info("Initiating IronsSpellsJSPlugin");
	}

	@Override
	public void registerBuilderTypes(BuilderTypeRegistry registry) {
		registry.addDefault(SpellRegistry.SPELL_REGISTRY_KEY, CustomSpell.Builder.class, CustomSpell.Builder::new);
		registry.addDefault(SchoolRegistry.SCHOOL_REGISTRY_KEY, SchoolTypeJSBuilder.class, SchoolTypeJSBuilder::new);
		registry.of(Registries.ATTRIBUTE, reg -> reg.add("spell", SpellAttributeBuilderJS.class, SpellAttributeBuilderJS::new));
		registry.of(Registries.ITEM, reg -> reg.add("magic_sword", CustomMagicSwordItem.Builder.class, CustomMagicSwordItem.Builder::new));
		registry.of(Registries.ITEM, reg -> reg.add("staff", CustomStaff.Builder.class, CustomStaff.Builder::new));
		registry.of(Registries.ITEM, reg -> reg.add("spellbook", CustomSpellBook.Builder.class, CustomSpellBook.Builder::new));
	}
}

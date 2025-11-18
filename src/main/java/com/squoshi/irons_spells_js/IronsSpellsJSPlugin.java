package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.entity.attribute.SpellAttributeBuilderJS;
import com.squoshi.irons_spells_js.event.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.item.CustomMagicSwordItem;
import com.squoshi.irons_spells_js.item.CustomSpellBook;
import com.squoshi.irons_spells_js.item.CustomStaff;
import com.squoshi.irons_spells_js.recipe.ISSSchemas;
import com.squoshi.irons_spells_js.spell.AbstractSpellWrapper;
import com.squoshi.irons_spells_js.spell.CustomSpell;
import com.squoshi.irons_spells_js.spell.school.SchoolTypeJSBuilder;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import io.redspace.ironsspellbooks.registries.PotionRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potions;

public final class IronsSpellsJSPlugin implements KubeJSPlugin {

	@Override
	public void init() {
		IronsSpellsJSMod.LOGGER.debug("Initiating IronsSpellsJSPlugin");
	}

	@Override
	public void registerBuilderTypes(BuilderTypeRegistry registry) {
		registry.addDefault(SpellRegistry.SPELL_REGISTRY_KEY, CustomSpell.Builder.class, CustomSpell.Builder::new);
		registry.addDefault(SchoolRegistry.SCHOOL_REGISTRY_KEY, SchoolTypeJSBuilder.class, SchoolTypeJSBuilder::new);
		registry.of(Registries.ATTRIBUTE, reg -> reg.add(IronsSpellsJSMod.id("spell"), SpellAttributeBuilderJS.class, SpellAttributeBuilderJS::new));
		registry.of(Registries.ITEM, reg -> reg.add(IronsSpellsJSMod.id("magic_sword"), CustomMagicSwordItem.Builder.class, CustomMagicSwordItem.Builder::new));
		registry.of(Registries.ITEM, reg -> reg.add(IronsSpellsJSMod.id("staff"), CustomStaff.Builder.class, CustomStaff.Builder::new));
		registry.of(Registries.ITEM, reg -> reg.add(IronsSpellsJSMod.id("spellbook"), CustomSpellBook.Builder.class, CustomSpellBook.Builder::new));
	}

	@Override
	public void registerBindings(BindingRegistry event) {
		event.add("SpellRarity", SpellRarity.class);
		event.add("SchoolRegistry", SchoolRegistry.class);
		event.add("CastType", CastType.class);
		event.add("IronsSpellsParticleHelper", ParticleHelper.class);
		event.add("SpellRegistry", SpellRegistry.class);
		event.add("ItemTags", ItemTags.class);
		event.add("Player", Player.class);
		event.add("SpellData", SpellData.class);
		event.add("Spell", AbstractSpellWrapper.class);
		event.add("ISSAnimationHolder", AnimationHolder.class);
		event.add("ISSUtils", Utils.class);
		event.add("TargetEntityCastData", TargetEntityCastData.class);
		event.add("Potions", Potions.class);
		event.add("ISSPotionRegistry", PotionRegistry.class);
		event.add("WizardAttackGoal", WizardAttackGoal.class);
		event.add("WarlockAttackGoal", WarlockAttackGoal.class);
		event.add("WizardRecoverGoal", WizardRecoverGoal.class);
		event.add("WizardSupportGoal", WizardSupportGoal.class);
		event.add("SpellBarrageGoal", SpellBarrageGoal.class);
		event.add("GustDefenseGoal", GustDefenseGoal.class);
		event.add("WispAttackGoal", WispAttackGoal.class);
	}

	@Override
	public void registerEvents(EventGroupRegistry registry) {
		registry.register(IronsSpellsJSEvents.GROUP);
	}

	@Override
	public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
		var iss = registry.namespace("irons_spellbooks");
		iss.register("alchemist_cauldron_brew", ISSSchemas.BREW);
		iss.register("alchemist_cauldron_empty", ISSSchemas.EMPTY);
		iss.register("alchemist_cauldron_fill", ISSSchemas.FILL);
	}
}

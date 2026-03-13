package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.entity.attribute.SpellAttributeBuilderJS;
import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.item.CustomMagicSwordItem;
import com.squoshi.irons_spells_js.item.CustomSpellBook;
import com.squoshi.irons_spells_js.item.CustomStaff;
import com.squoshi.irons_spells_js.spell.CustomSpell;
import com.squoshi.irons_spells_js.spell.AbstractSpellWrapper;
import com.squoshi.irons_spells_js.spell.school.SchoolTypeJSBuilder;
import com.squoshi.irons_spells_js.util.AlchemistCauldronRecipeSchemas;
import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import io.redspace.ironsspellbooks.registries.PotionRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potions;

public class IronsSpellsJSPlugin extends KubeJSPlugin {
    public static final RegistryInfo<AbstractSpell> SPELL_REGISTRY = RegistryInfo.of(SpellRegistry.SPELL_REGISTRY_KEY, AbstractSpell.class);
    public static final RegistryInfo<SchoolType> SCHOOL_REGISTRY = RegistryInfo.of(SchoolRegistry.SCHOOL_REGISTRY_KEY, SchoolType.class);

    @Override
    public void init() {
        SPELL_REGISTRY.addType("basic", CustomSpell.Builder.class, CustomSpell.Builder::new);
        SCHOOL_REGISTRY.addType("basic", SchoolTypeJSBuilder.class, SchoolTypeJSBuilder::new);
        RegistryInfo.ATTRIBUTE.addType("spell", SpellAttributeBuilderJS.class, SpellAttributeBuilderJS::new);
        RegistryInfo.ATTRIBUTE.addType("irons_spells_js:spell", SpellAttributeBuilderJS.class, SpellAttributeBuilderJS::new);
        RegistryInfo.ITEM.addType("spellbook", CustomSpellBook.Builder.class, CustomSpellBook.Builder::new);
        RegistryInfo.ITEM.addType("irons_spells_js:spellbook", CustomSpellBook.Builder.class, CustomSpellBook.Builder::new);
        RegistryInfo.ITEM.addType("staff", CustomStaff.Builder.class, CustomStaff.Builder::new);
        RegistryInfo.ITEM.addType("irons_spells_js:staff", CustomStaff.Builder.class, CustomStaff.Builder::new);
        RegistryInfo.ITEM.addType("magic_sword", CustomMagicSwordItem.Builder.class, CustomMagicSwordItem.Builder::new);
        RegistryInfo.ITEM.addType("irons_spells_js:magic_sword", CustomMagicSwordItem.Builder.class, CustomMagicSwordItem.Builder::new);
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "alchemist_cauldron_brew"), AlchemistCauldronRecipeSchemas.BREW);
        event.register(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "alchemist_cauldron_fill"), AlchemistCauldronRecipeSchemas.FILL);
        event.register(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "alchemist_cauldron_empty"), AlchemistCauldronRecipeSchemas.EMPTY);
    }
    @Override
    public void registerBindings(BindingsEvent event) {
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
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.registerSimple(ISSKJSUtils.AttributeHolder.class, ISSKJSUtils.AttributeHolder::of);
        typeWrappers.registerSimple(ISSKJSUtils.SoundEventHolder.class, ISSKJSUtils.SoundEventHolder::of);
        typeWrappers.registerSimple(ISSKJSUtils.SpellHolder.class, ISSKJSUtils.SpellHolder::of);
        typeWrappers.registerSimple(ISSKJSUtils.SchoolHolder.class, ISSKJSUtils.SchoolHolder::of);
        typeWrappers.registerSimple(ISSKJSUtils.DamageTypeHolder.class, ISSKJSUtils.DamageTypeHolder::of);
        typeWrappers.registerSimple(AbstractSpell.class, o -> {
            if (o instanceof AbstractSpell spell) return spell;
            return SpellRegistry.getSpell(ISSKJSUtils.SpellHolder.of(o).getLocation());
        });
        typeWrappers.registerSimple(SchoolType.class, o -> {
            if (o instanceof SchoolType school) return school;
            return SchoolRegistry.getSchool(ISSKJSUtils.SchoolHolder.of(o).getLocation());
        });
    }

    @Override
    public void registerEvents() {
        IronsSpellsJSEvents.GROUP.register();
    }
}
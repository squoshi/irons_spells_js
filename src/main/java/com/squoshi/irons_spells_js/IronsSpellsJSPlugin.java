package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.spell.CustomSpell;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.util.ParticleHelper;

public class IronsSpellsJSPlugin extends KubeJSPlugin {
    public static final RegistryInfo<AbstractSpell> SPELL_REGISTRY = RegistryInfo.of(SpellRegistry.SPELL_REGISTRY_KEY, AbstractSpell.class);

    @Override
    public void init() {
        SPELL_REGISTRY.addType("basic", CustomSpell.Builder.class, CustomSpell.Builder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("SpellRarity", SpellRarity.class);
        event.add("SchoolRegistry", SchoolRegistry.class);
        event.add("CastType", CastType.class);
        event.add("IronsSpellsParticleHelper", ParticleHelper.class);
    }

    @Override
    public void registerEvents() {
        IronsSpellsJSEvents.GROUP.register();
    }
}
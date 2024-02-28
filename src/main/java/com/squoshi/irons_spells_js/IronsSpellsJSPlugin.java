package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.spell.CustomSpell;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;

public class IronsSpellsJSPlugin extends KubeJSPlugin {
    public static final RegistryInfo SPELL_REGISTRY = RegistryInfo.of(SpellRegistry.SPELL_REGISTRY_KEY);

    @Override
    public void init() {
        SPELL_REGISTRY.addType("default", CustomSpell.Builder.class, CustomSpell.Builder::new, true);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("SpellRarity", SpellRarity.class);
        event.add("CastType", CastType.class);
    }
}
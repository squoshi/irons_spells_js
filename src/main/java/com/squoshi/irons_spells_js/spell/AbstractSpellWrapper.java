package com.squoshi.irons_spells_js.spell;

import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;

@SuppressWarnings("unused")
public interface AbstractSpellWrapper {
    @Info(value = """
        Returns a spell registry object by its ResourceLocation.
    """)
    static AbstractSpell of(ISSKJSUtils.SpellHolder spellHolder) {
        return SpellRegistry.getSpell(spellHolder.getLocation());
    }

    @Info(value = """
        Returns whether a spell is registered or not.
    """)
    static boolean exists(ISSKJSUtils.SpellHolder spellHolder) {
        return SpellRegistry.getSpell(spellHolder.getLocation()) != null;
    }

    @Info(value = """
        Returns whether an object is a spell or not.
    """)
    static boolean isSpell(Object o) {
        return o instanceof AbstractSpell;
    }

    @Info(value = """
        Returns either `ENABLED`, `DISABLED`, or `UNREGISTERED`, based on the spell inputted.
    """)
    static SpellStatus checkStatus(ISSKJSUtils.SpellHolder spellHolder) {
        SpellStatus enabled = SpellRegistry.getSpell(spellHolder.getLocation()).isEnabled() ? SpellStatus.ENABLED : SpellStatus.DISABLED;
        return exists(spellHolder) ? enabled : SpellStatus.UNREGISTERED;
    }

    @Info(value = """
        Returns whether a spell is enabled in the config or not.
    """)
    static boolean isEnabled(ISSKJSUtils.SpellHolder spellHolder) {
        return SpellRegistry.getSpell(spellHolder.getLocation()).isEnabled();
    }

    @Info(value = """
        Returns a SpellHolder reference for the given spell. Can be passed back into Spell.of().
        Useful for passing spell references around without resolving the registry immediately.
    """)
    static ISSKJSUtils.SpellHolder ofHolder(Object o) {
        return ISSKJSUtils.SpellHolder.of(o);
    }

    enum SpellStatus {
        REGISTERED,
        UNREGISTERED,
        ENABLED,
        DISABLED
    }
}
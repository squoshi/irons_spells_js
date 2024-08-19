package com.squoshi.irons_spells_js.spell;

import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface AbstractSpellWrapper {
	@Info(value = """
		    Returns a spell registry object.
		""")
	@Nullable
	static AbstractSpell of(ResourceKey<AbstractSpell> spellKey) {
		return SpellRegistry.REGISTRY.get(spellKey);
	}

	static Holder<AbstractSpell> ofHolder(Holder<AbstractSpell> holder) {
		return holder;
	}

	@Info(value = """
		    Returns whether a spell is registered or not.
		""")
	static boolean exists(ResourceKey<AbstractSpell> spellKey) {
		return SpellRegistry.REGISTRY.containsKey(spellKey);
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
	static SpellStatus checkStatus(ResourceKey<AbstractSpell> spellKey) {
		var spell = of(spellKey);
		return spell == null ? SpellStatus.UNREGISTERED : spell.isEnabled() ? SpellStatus.ENABLED : SpellStatus.DISABLED;
	}

	@Info(value = """
		    Returns whether a spell is enabled in the config or not.
		""")
	static boolean isEnabled(ResourceKey<AbstractSpell> spellKey) {
		var spell = of(spellKey);
		return spell != null && spell.isEnabled();
	}

	enum SpellStatus {
		REGISTERED,
		UNREGISTERED,
		ENABLED,
		DISABLED
	}
}

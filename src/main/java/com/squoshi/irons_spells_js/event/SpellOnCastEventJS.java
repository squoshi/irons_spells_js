package com.squoshi.irons_spells_js.event;

import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class SpellOnCastEventJS implements KubePlayerEvent {
	private final SpellOnCastEvent event;

	public SpellOnCastEventJS(SpellOnCastEvent event) {
		this.event = event;
	}

	@Override
	@Info(value = """
		    Returns the player that cast the spell.
		""")
	public Player getEntity() {
		return event.getEntity();
	}

	@Info(value = """
		    Returns the spell ID of the spell that was cast.
		""")
	public String getSpellId() {
		return event.getSpellId();
	}

	@Info(value = """
		    Returns the school type of the spell that was cast.
		""")
	public SchoolType getSchoolType() {
		return event.getSchoolType();
	}

	@Info(value = """
		    Returns the new spell level of the spell that was cast.
		""")
	public int getSpellLevel() {
		return event.getSpellLevel();
	}

	@Info(value = """
		    Sets the new spell level of the spell that was cast.
		""")
	public void setSpellLevel(int spellLevel) {
		event.setSpellLevel(spellLevel);
	}

	@Info(value = """
		    Returns the original spell level of the spell that was cast.
		""")
	public int getOriginalSpellLevel() {
		return event.getOriginalSpellLevel();
	}

	@Info(value = """
		    Returns the cast source.
		""")
	public CastSource getCastSource() {
		return event.getCastSource();
	}

	@Info(value = """
		    Returns the original mana cost.
		""")
	public int getOriginalManaCost() {
		return event.getOriginalManaCost();
	}

	@Info(value = """
		    Returns the new mana cost.
		""")
	public int getManaCost() {
		return event.getManaCost();
	}

	@Info(value = """
		    Sets the new mana cost.
		""")
	public void setManaCost(int manaCost) {
		event.setManaCost(manaCost);
	}
}

package com.squoshi.irons_spells_js.event;

import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class SpellPreCastEventJS implements KubePlayerEvent {
	private final SpellPreCastEvent event;

	public SpellPreCastEventJS(SpellPreCastEvent event) {
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
		    Returns the cast source.
		""")
	public CastSource getCastSource() {
		return event.getCastSource();
	}

}

package com.squoshi.irons_spells_js.event;

import dev.latvian.mods.kubejs.entity.KubeEntityEvent;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

@SuppressWarnings("unused")
public class SpellPreCastEventJS extends Event implements KubeEntityEvent, ICancellableEvent {
	private final LivingEntity entity;
	private final String spellId;
	private final int spellLevel;
	private final SchoolType schoolType;
	private final CastSource castSource;

	public SpellPreCastEventJS(LivingEntity entity, String spellId, int spellLevel, SchoolType schoolType, CastSource castSource) {
		this.entity = entity;
		this.spellId = spellId;
		this.spellLevel = spellLevel;
		this.schoolType = schoolType;
		this.castSource = castSource;
	}

	@Override
	@Info(value = """
		    Returns the player that cast the spell.
		""")
	public LivingEntity getEntity() {
		return this.entity;
	}

	@Info(value = """
		    Returns the spell ID of the spell that was cast.
		""")
	public String getSpellId() {
		return this.spellId;
	}

	@Info(value = """
		    Returns the school type of the spell that was cast.
		""")
	public SchoolType getSchoolType() {
		return this.schoolType;
	}

	@Info(value = """
		    Returns the new spell level of the spell that was cast.
		""")
	public int getSpellLevel() {
		return this.spellLevel;
	}

	@Info(value = """
		    Returns the cast source.
		""")
	public CastSource getCastSource() {
		return this.castSource;
	}

}

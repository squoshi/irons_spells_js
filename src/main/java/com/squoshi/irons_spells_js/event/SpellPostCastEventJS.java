package com.squoshi.irons_spells_js.event;

import dev.latvian.mods.kubejs.entity.KubeEntityEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class SpellPostCastEventJS implements KubeEntityEvent {
	private final LivingEntity entity;
	private final AbstractSpell spell;
	private final int spellLevel;
	private final Level level;
	private final MagicData playerMagicData;

	public SpellPostCastEventJS(LivingEntity entity, AbstractSpell spell, int spellLevel, MagicData playerMagicData) {
		this.entity = entity;
		this.spell = spell;
		this.spellLevel = spellLevel;
		this.level = entity.level();
		this.playerMagicData = playerMagicData;
	}

	@Override
	public Entity getEntity() {
		return entity;
	}


	public AbstractSpell getSpell() {
		return spell;
	}


	public int getSpellLevel() {
		return spellLevel;
	}


	public Level getLevel() {
		return level;
	}


	public MagicData getMagicData() {
		return playerMagicData;
	}
}

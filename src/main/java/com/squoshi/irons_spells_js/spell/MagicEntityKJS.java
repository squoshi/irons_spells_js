package com.squoshi.irons_spells_js.spell;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings("unused")
@RemapPrefixForJS("irons_spells_js$")
public interface MagicEntityKJS {
	@HideFromJS
	LivingEntity irons_spells_js$self();

	default MagicData irons_spells_js$getMagicData() {
		return MagicData.getPlayerMagicData(irons_spells_js$self());
	}
}

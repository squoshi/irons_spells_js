package com.squoshi.irons_spells_js.event;

import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager.SpellSelectionEvent;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public class SpellSelectionEventJS implements KubePlayerEvent {
	private final SpellSelectionEvent event;

	public SpellSelectionEventJS(SpellSelectionEvent event) {
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
		    Adds spell option to the end of a player's spell bar.
		""")
	public void addSelectionOption(SpellData spellData, String slotId, int localSlotIndex, int globalIndex) {
		event.addSelectionOption(spellData, slotId, localSlotIndex, globalIndex);
	}

	@Info(value = """
		    Adds spell option to the end of a player's spell bar.
		""")
	public void addSelectionOption(SpellData spellData, String slotId, int localSlotIndex) {
		event.addSelectionOption(spellData, slotId, localSlotIndex);
	}

	public SpellSelectionManager getManager() {
		return event.getManager();
	}
}

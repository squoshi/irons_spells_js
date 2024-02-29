package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.player.Player;

public class ChangeManaEventJS extends PlayerEventJS {
    private final ChangeManaEvent event;

    public ChangeManaEventJS(ChangeManaEvent event) {
        this.event = event;
    }

    @Override
    public Player getEntity() {
        return event.getEntity();
    }

    @Info(value = """
        Returns the float mana value that the value was before it was changed.
    """)
    @SuppressWarnings("unused")
    public float getOldMana() {
        return event.getOldMana();
    }

    @Info(value = """
        Returns the float mana value that the value changed to after it was changed.
    """)
    @SuppressWarnings("unused")
    public float getNewMana() {
        return event.getNewMana();
    }

    @Info(value = """
        Changes the value that the mana will change to during the event.
    """)
    @SuppressWarnings("unused")
    public void setNewMana(float newMana) {
        event.setNewMana(newMana);
    }

    @Info(value = """
        Returns the player's current MagicData.
    """)
    @SuppressWarnings("unused")
    public MagicData getMagicData() {
        return event.getMagicData();
    }
}

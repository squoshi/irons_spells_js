package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
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

    @SuppressWarnings("unused")
    public float getOldMana() {
        return event.getOldMana();
    }

    @SuppressWarnings("unused")
    public float getNewMana() {
        return event.getNewMana();
    }

    @SuppressWarnings("unused")
    public void setNewMana(float newMana) {
        event.setNewMana(newMana);
    }

    @SuppressWarnings("unused")
    public MagicData getMagicData() {
        return event.getMagicData();
    }
}

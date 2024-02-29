package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
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
}

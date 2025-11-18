package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import net.minecraft.world.entity.player.Player;

public class ManaRegenEventJS extends PlayerEventJS {
    private final Player player;
    private float mana;

    public ManaRegenEventJS(Player player, float mana) {
        this.player = player;
        this.mana = mana;
    }

    @SuppressWarnings("unused")
    public float getAmount() {
        return mana;
    }

    @SuppressWarnings("unused")
    public void setAmount(float mana) {
        this.mana = mana;
    }

    @Override
    public Player getEntity() {
        return player;
    }
}
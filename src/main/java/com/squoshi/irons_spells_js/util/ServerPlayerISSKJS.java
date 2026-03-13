package com.squoshi.irons_spells_js.util;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public interface ServerPlayerISSKJS {

    @HideFromJS
    ServerPlayer irons_spells_js$getServerPlayer();

    @Info("""
        Returns the player's magic data. Useful for changing the player's mana, or checking if the player is casting a spell.
        Changing the player's mana directly will not sync with the client. Use `syncMana()` on the player to sync the mana with the client.
    """)
    @RemapForJS("getMagicData")
    default MagicData irons_spells_js$getMagicData() {
        return MagicData.getPlayerMagicData(irons_spells_js$getServerPlayer());
    }

    @Info("""
        Synchronizes the player's mana with the client. Call this whenever you change a player's mana in a non-traditional way.
    """)
    @RemapForJS("syncMana")
    default void irons_spells_js$syncMana() {
        PacketDistributor.sendToPlayer(irons_spells_js$getServerPlayer(), new SyncManaPacket(irons_spells_js$getMagicData()));
    }
}
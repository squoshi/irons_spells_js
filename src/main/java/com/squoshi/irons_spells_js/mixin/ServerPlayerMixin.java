package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.util.ServerPlayerISSKJS;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerISSKJS {
    @Override
    public ServerPlayer irons_spells_js$getServerPlayer() {
        return (ServerPlayer) (Object) this;
    }
}
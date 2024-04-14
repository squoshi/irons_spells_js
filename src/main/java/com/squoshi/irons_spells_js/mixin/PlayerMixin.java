package com.squoshi.irons_spells_js.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class PlayerMixin {
    public MagicData kjs$getMagicData() {
        return MagicData.getPlayerMagicData((Player) (Object) this);
    }
}
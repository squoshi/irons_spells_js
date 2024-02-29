package com.squoshi.irons_spells_js.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class IronsSpellsJSUtils {
    public static class MagicDataJS {
        public static MagicData of(ServerPlayer p) {
            return new MagicData(p);
        }
    }
}

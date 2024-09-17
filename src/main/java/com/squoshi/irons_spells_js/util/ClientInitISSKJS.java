package com.squoshi.irons_spells_js.util;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.minecraft.world.item.Item;

@SuppressWarnings("unused")
public interface ClientInitISSKJS {
    @Info("""
        When added, holding the item will show your mana bar.
    """)
    @RemapForJS("addManaBarShowingItem")
    default void irons_spells_js$addManaBarShowingItem(Item item) {
        IronsSpellsJSMod.MANA_BAR_ITEMS.add(item);
    }
}
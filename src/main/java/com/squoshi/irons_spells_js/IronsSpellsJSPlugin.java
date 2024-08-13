package com.squoshi.irons_spells_js;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;

public final class IronsSpellsJSPlugin implements KubeJSPlugin {
    @Override
    public void init() {
        IronsSpellsJSMod.LOGGER.info("Initiating IronsSpellsJSPlugin");
    }
}

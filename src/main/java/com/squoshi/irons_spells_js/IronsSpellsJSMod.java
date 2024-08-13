package com.squoshi.irons_spells_js;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(IronsSpellsJSMod.MODID)
public final class IronsSpellsJSMod {
    public static final String MODID = "irons_spells_js";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public IronsSpellsJSMod(ModContainer mod, IEventBus bus) {
        LOGGER.info("Initializing IronSpellsJS");
    }
}

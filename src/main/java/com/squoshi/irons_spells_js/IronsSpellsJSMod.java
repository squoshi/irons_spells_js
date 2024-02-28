package com.squoshi.irons_spells_js;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Mod(IronsSpellsJSMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class IronsSpellsJSMod {
    public static final String MODID = "irons_spells_js";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public IronsSpellsJSMod() {}
}
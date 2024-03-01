package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.mixin.ServerConfigsAccessor;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Mod(IronsSpellsJSMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class IronsSpellsJSMod {
    public static final String MODID = "irons_spells_js";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public IronsSpellsJSMod() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::runIronSpellsConfig);

        MinecraftForge.EVENT_BUS.addListener(IronsSpellsJSEvents::changeMana);
        MinecraftForge.EVENT_BUS.addListener(IronsSpellsJSEvents::spellCast);
    }

    private void runIronSpellsConfig(InterModEnqueueEvent event){
        LOGGER.info("Registering spells on Config File...");
        ServerConfigsAccessor.getBuilder().push("Spells");
        IronsSpellsJSPlugin.SPELL_REGISTRY.objects.values().forEach(builder -> {
            ServerConfigsAccessor.invoke$createSpellConfig((AbstractSpell) builder.get());
        });
        ServerConfigsAccessor.getBuilder().pop();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigsAccessor.getBuilder().build(), String.format("%s-server.toml", IronsSpellbooks.MODID));
    }
}
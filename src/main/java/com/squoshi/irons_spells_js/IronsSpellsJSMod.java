package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.mixin.ServerConfigsAccessor;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Mod(IronsSpellsJSMod.MODID)
public final class IronsSpellsJSMod {
    public static final String MODID = "irons_spells_js";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final List<DeferredRegister<?>> MODDED_REGISTRIES = new ArrayList<>();

    public IronsSpellsJSMod(ModContainer mod, IEventBus bus) {
        LOGGER.info("Initializing IronSpellsJS");
        bus.addListener(this::runIronSpellsConfig);
    }

    private void runIronSpellsConfig(InterModEnqueueEvent event) {
        LOGGER.info("Registering spells on Config File...");
        ServerConfigsAccessor.getBuilder().push("Spells");
        RegistryObjectStorage.of(SpellRegistry.SPELL_REGISTRY_KEY).objects.values().forEach(builder -> {
            ServerConfigsAccessor.invoke$createSpellConfig(builder.get());
        });
        ServerConfigsAccessor.getBuilder().pop();
        ModList.get().getModContainerById("irons_spellbooks").get().registerConfig(ModConfig.Type.SERVER, ServerConfigsAccessor.getBuilder().build(), String.format("%s-server.toml", IronsSpellbooks.MODID));
    }
}

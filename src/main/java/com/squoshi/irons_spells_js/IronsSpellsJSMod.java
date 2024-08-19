package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.mixin.ServerConfigsAccessor;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronBuildInteractionsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(IronsSpellsJSMod.MODID)
public final class IronsSpellsJSMod {
	public static final String MODID = "irons_spells_js";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public IronsSpellsJSMod(ModContainer mod, IEventBus modBus) {
		LOGGER.info("Initializing IronSpellsJS");
		modBus.addListener(this::runIronSpellsConfig);
		var gameBus = NeoForge.EVENT_BUS;
		gameBus.addListener(this::caldronInteraction);
	}

	private void runIronSpellsConfig(InterModEnqueueEvent event) {
		LOGGER.info("Registering spells on Config File...");
		ServerConfigsAccessor.getBuilder().push("Spells");
		RegistryObjectStorage.of(SpellRegistry.SPELL_REGISTRY_KEY).objects.values()
			.forEach(builder -> ServerConfigsAccessor.invoke$createSpellConfig(builder.get()));
		ServerConfigsAccessor.getBuilder().pop();
		ModList.get().getModContainerById("irons_spellbooks").get().registerConfig(ModConfig.Type.SERVER, ServerConfigsAccessor.getBuilder().build(), String.format("%s-server.toml", IronsSpellbooks.MODID));
	}



	private void caldronInteraction(AlchemistCauldronBuildInteractionsEvent event) {
		// Does NOT work, fires too soon (EVENT_BUS was not started yet), might report to Devs
	}
}

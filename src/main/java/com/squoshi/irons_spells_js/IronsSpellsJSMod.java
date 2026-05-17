package com.squoshi.irons_spells_js;

import com.squoshi.irons_spells_js.events.IronsSpellsJSEvents;
import com.squoshi.irons_spells_js.mixin.ServerConfigsAccessor;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.util.ArrayList;

@Mod(IronsSpellsJSMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class IronsSpellsJSMod {
    public static final String MODID = "irons_spells_js";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    @HideFromJS
    public static ArrayList<Item> MANA_BAR_ITEMS = new ArrayList<>();

    public IronsSpellsJSMod() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::runIronSpellsConfig);

        MinecraftForge.EVENT_BUS.addListener(IronsSpellsJSEvents::changeMana);
        MinecraftForge.EVENT_BUS.addListener(IronsSpellsJSEvents::spellCast);
        MinecraftForge.EVENT_BUS.addListener(IronsSpellsJSEvents::spellPreCast);
        MinecraftForge.EVENT_BUS.addListener(IronsSpellsJSEvents::spellSelectionManager);
    }

    private void runIronSpellsConfig(InterModEnqueueEvent event){
        LOGGER.info("Registering custom spells with Iron's Spellbooks config cache...");
        IronsSpellsJSPlugin.SPELL_REGISTRY.objects.values().forEach(builder -> {
            ServerConfigsAccessor.invoke$createSpellConfig((AbstractSpell) builder.get());
        });
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event){
        RegistryInfo.ITEM.objects.forEach((id, builderBase) -> {
            if (builderBase.get() instanceof SpellBook) {
                CuriosRendererRegistry.register(builderBase.get(), SpellBookCurioRenderer::new);
            }
        });
    }
}
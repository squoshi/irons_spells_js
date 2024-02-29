package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class IronsSpellsJSEvents {
    public static final EventGroup GROUP = EventGroup.of("IronsSpellsEvents");

    public static final EventHandler changeMana = GROUP.server("changeMana", () -> ChangeManaEventJS.class);

    public static void init() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(IronsSpellsJSEvents::changeMana);
    }

    public static void changeMana(ChangeManaEvent event) {
        if (changeMana.hasListeners()) {
            changeMana.post(new ChangeManaEventJS(event));
        }
    }
}

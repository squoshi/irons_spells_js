package com.squoshi.irons_spells_js.events;

import com.squoshi.irons_spells_js.util.ISpellModify;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.squoshi.irons_spells_js.IronsSpellsJSMod.MODID;
import static com.squoshi.irons_spells_js.events.SpellModificationEventJS.getOrCreate;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    @SubscribeEvent
    public static void onSpellRegistry(ServerStartedEvent event) {
        SpellRegistry.getEnabledSpells().forEach(spell -> {
            if (spell instanceof ISpellModify spellModify) {
                if (IronsSpellsJSEvents.modifySpell.hasListeners()) {
                    var eventJS = getOrCreate(spell.getSpellResource());
                    spellModify.irons_spells_js$setBuilder(spell.getSpellResource());
                    IronsSpellsJSEvents.modifySpell.post(eventJS);
                    ConsoleJS.STARTUP.info("Adding spell builder for " + spell.getSpellId());
                    ConsoleJS.STARTUP.info(spell.getSpellResource());
                }
            }
        });
    }
}

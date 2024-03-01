package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.bindings.event.PlayerEvents;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellCastEvent;

public class IronsSpellsJSEvents {
    public static final EventGroup GROUP = EventGroup.of("IronsSpellsEvents");

    public static final EventHandler changeMana = PlayerEvents.GROUP.server("changeMana", () -> ChangeManaEventJS.class);
    public static final EventHandler spellCast = PlayerEvents.GROUP.server("spellCast", () -> SpellCastEventJS.class);

    public static void changeMana(ChangeManaEvent event) {
        if (changeMana.hasListeners()) {
            changeMana.post(new ChangeManaEventJS(event));
        }
    }

    public static void spellCast(SpellCastEvent event) {
        if (spellCast.hasListeners()) {
            spellCast.post(new SpellCastEventJS(event));
        }
    }
}

package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.bindings.event.EntityEvents;
import dev.latvian.mods.kubejs.bindings.event.PlayerEvents;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;

public class IronsSpellsJSEvents {
    public static final EventGroup GROUP = EventGroup.of("ISSEvents");

    // ISSEvents group
    public static final EventHandler changeMana = GROUP.server("changeMana", () -> ChangeManaEventJS.class);
    public static final EventHandler manaRegen = GROUP.server("manaRegen", () -> ManaRegenEventJS.class).hasResult();
    public static final EventHandler spellCast = GROUP.server("spellOnCast", () -> SpellOnCastEventJS.class);
    public static final EventHandler spellPreCast = GROUP.server("spellPreCast", () -> SpellPreCastEventJS.class).hasResult();
    public static final EventHandler spellSelectionManager = GROUP.startup("spellSelection", () -> SpellSelectionEventJS.class);
    public static final EventHandler entitySpellPreCast = GROUP.server("entitySpellPreCast", () -> EntitySpellPreCastEventJS.class).hasResult();
    public static final EventHandler entitySpellCast = GROUP.server("entitySpellOnCast", () -> EntitySpellCastEventJS.class);
    public static final EventHandler modifySpell = GROUP.startup("modifySpell", () -> SpellModificationEventJS.class);

    // PlayerEvents group (backwards compat aliases)
    public static final EventHandler changeManaPlayerEvents = PlayerEvents.GROUP.server("changeMana", () -> ChangeManaEventJS.class);
    public static final EventHandler manaRegenPlayerEvents = PlayerEvents.GROUP.server("manaRegen", () -> ManaRegenEventJS.class).hasResult();
    public static final EventHandler spellCastPlayerEvents = PlayerEvents.GROUP.server("spellOnCast", () -> SpellOnCastEventJS.class);
    public static final EventHandler spellPreCastPlayerEvents = PlayerEvents.GROUP.server("spellPreCast", () -> SpellPreCastEventJS.class).hasResult();
    public static final EventHandler spellSelectionManagerPlayerEvents = PlayerEvents.GROUP.startup("spellSelection", () -> SpellSelectionEventJS.class);

    // EntityEvents group (backwards compat aliases)
    public static final EventHandler entitySpellPreCastEntityEvents = EntityEvents.GROUP.server("spellPreCast", () -> EntitySpellPreCastEventJS.class).hasResult();
    public static final EventHandler entitySpellCastEntityEvents = EntityEvents.GROUP.server("spellOnCast", () -> EntitySpellCastEventJS.class);

    public static void changeMana(ChangeManaEvent event) {
        ChangeManaEventJS eventJS = new ChangeManaEventJS(event);
        if (changeMana.hasListeners()) {
            if (changeMana.post(eventJS).interruptFalse()) {
                event.setCanceled(true);
            }
        }
        if (changeManaPlayerEvents.hasListeners()) {
            if (changeManaPlayerEvents.post(eventJS).interruptFalse()) {
                event.setCanceled(true);
            }
        }
    }

    public static void spellCast(SpellOnCastEvent event) {
        SpellOnCastEventJS eventJS = new SpellOnCastEventJS(event);
        if (spellCast.hasListeners()) {
            spellCast.post(eventJS);
        }
        if (spellCastPlayerEvents.hasListeners()) {
            spellCastPlayerEvents.post(eventJS);
        }
    }

    public static void spellPreCast(SpellPreCastEvent event) {
        SpellPreCastEventJS eventJS = new SpellPreCastEventJS(event);
        if (spellPreCast.hasListeners()) {
            if (spellPreCast.post(eventJS).interruptFalse()) {
                event.setCanceled(true);
            }
        }
        if (spellPreCastPlayerEvents.hasListeners()) {
            if (spellPreCastPlayerEvents.post(eventJS).interruptFalse()) {
                event.setCanceled(true);
            }
        }
    }

    public static void spellSelectionManager(SpellSelectionManager.SpellSelectionEvent event) {
        SpellSelectionEventJS eventJS = new SpellSelectionEventJS(event);
        if (spellSelectionManager.hasListeners()) {
            spellSelectionManager.post(eventJS);
        }
        if (spellSelectionManagerPlayerEvents.hasListeners()) {
            spellSelectionManagerPlayerEvents.post(eventJS);
        }
    }
}

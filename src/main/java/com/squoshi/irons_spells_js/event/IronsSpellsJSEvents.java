package com.squoshi.irons_spells_js.event;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import dev.latvian.mods.kubejs.bindings.event.EntityEvents;
import dev.latvian.mods.kubejs.bindings.event.PlayerEvents;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = IronsSpellsJSMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class IronsSpellsJSEvents {
	public static final EventGroup GROUP = EventGroup.of("ISSEvents");

	public static final EventHandler changeMana = PlayerEvents.GROUP.server("changeMana", () -> ChangeManaEventJS.class).hasResult();
	public static final EventHandler spellCast = PlayerEvents.GROUP.server("spellOnCast", () -> SpellOnCastEventJS.class);
	public static final EventHandler spellPreCast = PlayerEvents.GROUP.server("spellPreCast", () -> SpellPreCastEventJS.class).hasResult();
	public static final EventHandler spellSelectionManager = PlayerEvents.GROUP.startup("spellSelection", () -> SpellSelectionEventJS.class);

	public static final EventHandler entitySpellPreCast = EntityEvents.GROUP.server("spellPreCast", () -> EntitySpellPreCastEventJS.class).hasResult();
	public static final EventHandler entitySpellCast = EntityEvents.GROUP.server("spellOnCast", () -> EntitySpellCastEventJS.class);

	@SubscribeEvent
	public static void changeMana(ChangeManaEvent event) {
		if (changeMana.hasListeners()) {
			changeMana.post(new ChangeManaEventJS(event)).applyCancel(event);
		}
	}

	@SubscribeEvent
	public static void spellCast(SpellOnCastEvent event) {
		if (spellCast.hasListeners()) {
			spellCast.post(new SpellOnCastEventJS(event));
		}
	}

	@SubscribeEvent
	public static void spellPreCast(SpellPreCastEvent event) {
		if (spellPreCast.hasListeners()) {
			spellPreCast.post(new SpellPreCastEventJS(event)).applyCancel(event);
		}
	}

	@SubscribeEvent
	public static void spellSelectionManager(SpellSelectionManager.SpellSelectionEvent event) {
		if (spellSelectionManager.hasListeners()) {
			spellSelectionManager.post(new SpellSelectionEventJS(event));
		}
	}
}

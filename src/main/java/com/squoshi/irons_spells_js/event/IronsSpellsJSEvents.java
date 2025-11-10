package com.squoshi.irons_spells_js.event;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.plugin.builtin.event.EntityEvents;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = IronsSpellsJSMod.MODID)
public class IronsSpellsJSEvents {
	public static final EventGroup GROUP = EventGroup.of("ISSEvents");

	public static final EventHandler changeMana = GROUP.server("changeMana", () -> ChangeManaEventJS.class).hasResult();
	public static final TargetedEventHandler<ResourceKey<EntityType<?>>> spellPreCast = GROUP.server("spellPreCast", () -> SpellPreCastEventJS.class).supportsTarget(EntityEvents.TARGET).hasResult();
	public static final EventHandler spellCast = GROUP.server("spellOnCast", () -> SpellOnCastEventJS.class);
	public static final TargetedEventHandler<ResourceKey<EntityType<?>>> spellPostCast = GROUP.server("spellPostCast", () -> SpellPostCastEventJS.class).supportsTarget(EntityEvents.TARGET);
	public static final EventHandler spellSelectionManager = GROUP.startup("spellSelection", () -> SpellSelectionEventJS.class);

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
		var playerKey = EntityType.PLAYER.kjs$getKey();
		if (spellPreCast.hasListeners(playerKey)) {
			var kjsEvent = new SpellPreCastEventJS(event.getEntity(), event.getSpellId(), event.getSpellLevel(), event.getSchoolType(), event.getCastSource());
			spellPreCast.post(event.getEntity(), playerKey, kjsEvent).applyCancel(event);
		}
	}

	@SubscribeEvent
	public static void spellSelectionManager(SpellSelectionManager.SpellSelectionEvent event) {
		if (spellSelectionManager.hasListeners()) {
			spellSelectionManager.post(new SpellSelectionEventJS(event));
		}
	}
}

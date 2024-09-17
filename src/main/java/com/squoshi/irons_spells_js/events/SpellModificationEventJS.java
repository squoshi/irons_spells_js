package com.squoshi.irons_spells_js.events;

import com.squoshi.irons_spells_js.IronsSpellsJSMod;
import com.squoshi.irons_spells_js.spell.SpellModificationBuilder;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SpellModificationEventJS extends EventJS {
    public static final Map<ResourceLocation, SpellModificationEventJS> eventMap = new HashMap<>();

    private final SpellModificationBuilder builder;
    private final ResourceLocation spell;

    public SpellModificationEventJS(ResourceLocation spellResource) {
        IronsSpellsJSMod.LOGGER.info(spellResource);
        IronsSpellsJSMod.LOGGER.info(SpellRegistry.getSpell(spellResource));
        this.spell = spellResource;
        this.builder = new SpellModificationBuilder(spellResource);
        if (!eventMap.containsKey(spellResource)) {
            eventMap.put(spellResource, this);
        }
    }

    public static SpellModificationEventJS getOrCreate(ResourceLocation spellResource) {
        if (!eventMap.containsKey(spellResource)) {
            return new SpellModificationEventJS(spellResource);
        }
        return eventMap.get(spellResource);
    }

    @HideFromJS
    public SpellModificationBuilder getBuilder() {
        return builder;
    }

    @SuppressWarnings("unused")
    public void modify(AbstractSpell spell, Consumer<SpellModificationBuilder> modifyBuilder) {
        SpellModificationBuilder builder = getOrCreate(spell.getSpellResource()).getBuilder();
        modifyBuilder.accept(builder);
    }
}

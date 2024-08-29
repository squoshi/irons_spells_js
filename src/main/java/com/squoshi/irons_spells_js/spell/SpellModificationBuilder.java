package com.squoshi.irons_spells_js.spell;

import dev.latvian.mods.kubejs.event.EventJS;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class SpellModificationBuilder extends EventJS {
    private final ResourceLocation spellResource;
    public transient Consumer<AbstractSpell> setPreSpellCastCallback;
    public SpellModificationBuilder(ResourceLocation spellResource){
        this.spellResource = spellResource;
    }

    public SpellModificationBuilder setPreSpellCastCallback(Consumer<AbstractSpell> setPreSpellCastCallback) {
        this.setPreSpellCastCallback = setPreSpellCastCallback;
        return this;
    }

    public AbstractSpell getSpell() {
        return SpellRegistry.getSpell(spellResource);
    }
}

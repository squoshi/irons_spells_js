package com.squoshi.irons_spells_js.util;

import com.squoshi.irons_spells_js.spell.SpellModificationBuilder;
import net.minecraft.resources.ResourceLocation;

public interface ISpellModify {
    SpellModificationBuilder getBuilder();
    void setBuilder(ResourceLocation resourceLocation);
}

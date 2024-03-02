package com.squoshi.irons_spells_js.events;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.events.SpellCastEvent;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.player.Player;

public class SpellCastEventJS extends PlayerEventJS {
    private final SpellCastEvent event;

    public SpellCastEventJS(SpellCastEvent event) {
        this.event = event;
    }

    @Override
    @Info(value = """
        Returns the player that cast the spell.
    """)
    @SuppressWarnings("unused")
    public Player getEntity() {
        return event.getEntity();
    }

    @Info(value = """
        Returns if the event is cancelable.
    """)
    @SuppressWarnings("unused")
    public boolean isCancelable() {
        return event.isCancelable();
    }

    @Info(value = """
        Returns the spell ID of the spell that was cast.
    """)
    @SuppressWarnings("unused")
    public String getSpellId() {
        return event.getSpellId();
    }

    @Info(value = """
        Returns the school type of the spell that was cast.
    """)
    @SuppressWarnings("unused")
    public SchoolType getSchoolType() {
        return event.getSchoolType();
    }

    @Info(value = """
        Returns the spell level of the spell that was cast.
    """)
    @SuppressWarnings("unused")
    public int getSpellLevel() {
        return event.getSpellLevel();
    }

    @Info(value = """
        Returns the cast source.
    """)
    @SuppressWarnings("unused")
    public CastSource getCastSource() {
        return event.getCastSource();
    }
}

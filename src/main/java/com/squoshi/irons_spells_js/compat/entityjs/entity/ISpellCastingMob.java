package com.squoshi.irons_spells_js.compat.entityjs.entity;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.Projectile;

@RemapPrefixForJS("irons_spells_js$")
public interface ISpellCastingMob {
    PathfinderMob irons_spells_js$self();

    MagicData irons_spells_js$getMagicData();
    boolean irons_spells_js$isDrinkingPotion();
    void irons_spells_js$setDrinkingPotion(boolean drinkingPotion);
    void irons_spells_js$startDrinkingPotion();
    void irons_spells_js$finishDrinkingPotion();
    void irons_spells_js$onSyncedDataUpdated(EntityDataAccessor<?> key);
    void irons_spells_js$addAdditionalSaveData(CompoundTag compoundTag);
    void irons_spells_js$readAdditionalSaveData(CompoundTag compoundTag);
    void irons_spells_js$cancelCast();
    void irons_spells_js$castComplete();
    void irons_spells_js$startAutoSpinAttack(int ticks);
    void irons_spells_js$setSyncedSpellData(SyncedSpellData syncedSpellData);
    void irons_spells_js$customServerAiStep();
    void irons_spells_js$initiateCastSpell(AbstractSpell spell, int spellLevel);
    void irons_spells_js$notifyDangerousProjectile(Projectile projectile);
    boolean irons_spells_js$isCasting();
    boolean irons_spells_js$setTeleportLocationBehindTarget(int distance);
    void irons_spells_js$setBurningDashDirectionData();
    void irons_spells_js$forceLookAtTarget(LivingEntity target);
    void irons_spells_js$addClientSideParticles();
}

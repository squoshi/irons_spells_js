package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;

public interface ArmorMaterial {
   int getDurabilityForSlot(EquipmentSlot pSlot);

   int getDefenseForSlot(EquipmentSlot pSlot);

   int getEnchantmentValue();

   SoundEvent getEquipSound();

   Ingredient getRepairIngredient();

   String getName();

   float getToughness();

   /**
    * Gets the percentage of knockback resistance provided by armor of the material.
    */
   float getKnockbackResistance();
}
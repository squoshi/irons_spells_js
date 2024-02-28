package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe extends CustomRecipe {
   private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
   private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
   private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

   public FireworkRocketRecipe(ResourceLocation pId) {
      super(pId);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      boolean flag = false;
      int i = 0;

      for(int j = 0; j < pInv.getContainerSize(); ++j) {
         ItemStack itemstack = pInv.getItem(j);
         if (!itemstack.isEmpty()) {
            if (PAPER_INGREDIENT.test(itemstack)) {
               if (flag) {
                  return false;
               }

               flag = true;
            } else if (GUNPOWDER_INGREDIENT.test(itemstack)) {
               ++i;
               if (i > 3) {
                  return false;
               }
            } else if (!STAR_INGREDIENT.test(itemstack)) {
               return false;
            }
         }
      }

      return flag && i >= 1;
   }

   /**
    * Returns an Item that is the result of this recipe
    */
   public ItemStack assemble(CraftingContainer pInv) {
      ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, 3);
      CompoundTag compoundtag = itemstack.getOrCreateTagElement("Fireworks");
      ListTag listtag = new ListTag();
      int i = 0;

      for(int j = 0; j < pInv.getContainerSize(); ++j) {
         ItemStack itemstack1 = pInv.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (GUNPOWDER_INGREDIENT.test(itemstack1)) {
               ++i;
            } else if (STAR_INGREDIENT.test(itemstack1)) {
               CompoundTag compoundtag1 = itemstack1.getTagElement("Explosion");
               if (compoundtag1 != null) {
                  listtag.add(compoundtag1);
               }
            }
         }
      }

      compoundtag.putByte("Flight", (byte)i);
      if (!listtag.isEmpty()) {
         compoundtag.put("Explosions", listtag);
      }

      return itemstack;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   /**
    * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
    * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
    */
   public ItemStack getResultItem() {
      return new ItemStack(Items.FIREWORK_ROCKET);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.FIREWORK_ROCKET;
   }
}
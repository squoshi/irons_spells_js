package net.minecraft.world.item;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHeadItem extends StandingAndWallBlockItem {
   public static final String TAG_SKULL_OWNER = "SkullOwner";

   public PlayerHeadItem(Block pStandingBlock, Block pWallBlock, Item.Properties pProperties) {
      super(pStandingBlock, pWallBlock, pProperties);
   }

   /**
    * Gets the title name of the book
    */
   public Component getName(ItemStack pStack) {
      if (pStack.is(Items.PLAYER_HEAD) && pStack.hasTag()) {
         String s = null;
         CompoundTag compoundtag = pStack.getTag();
         if (compoundtag.contains("SkullOwner", 8)) {
            s = compoundtag.getString("SkullOwner");
         } else if (compoundtag.contains("SkullOwner", 10)) {
            CompoundTag compoundtag1 = compoundtag.getCompound("SkullOwner");
            if (compoundtag1.contains("Name", 8)) {
               s = compoundtag1.getString("Name");
            }
         }

         if (s != null) {
            return Component.translatable(this.getDescriptionId() + ".named", s);
         }
      }

      return super.getName(pStack);
   }

   public void verifyTagAfterLoad(CompoundTag pCompoundTag) {
      super.verifyTagAfterLoad(pCompoundTag);
      if (pCompoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(pCompoundTag.getString("SkullOwner"))) {
         GameProfile gameprofile = new GameProfile((UUID)null, pCompoundTag.getString("SkullOwner"));
         SkullBlockEntity.updateGameprofile(gameprofile, (p_151177_) -> {
            pCompoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), p_151177_));
         });
      }

   }
}
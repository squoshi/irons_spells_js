package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArmorStandItem extends Item {
   public ArmorStandItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Direction direction = pContext.getClickedFace();
      if (direction == Direction.DOWN) {
         return InteractionResult.FAIL;
      } else {
         Level level = pContext.getLevel();
         BlockPlaceContext blockplacecontext = new BlockPlaceContext(pContext);
         BlockPos blockpos = blockplacecontext.getClickedPos();
         ItemStack itemstack = pContext.getItemInHand();
         Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
         AABB aabb = EntityType.ARMOR_STAND.getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
         if (level.noCollision((Entity)null, aabb) && level.getEntities((Entity)null, aabb).isEmpty()) {
            if (level instanceof ServerLevel) {
               ServerLevel serverlevel = (ServerLevel)level;
               ArmorStand armorstand = EntityType.ARMOR_STAND.create(serverlevel, itemstack.getTag(), (Component)null, pContext.getPlayer(), blockpos, MobSpawnType.SPAWN_EGG, true, true);
               if (armorstand == null) {
                  return InteractionResult.FAIL;
               }

               float f = (float)Mth.floor((Mth.wrapDegrees(pContext.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
               armorstand.moveTo(armorstand.getX(), armorstand.getY(), armorstand.getZ(), f, 0.0F);
               this.randomizePose(armorstand, level.random);
               serverlevel.addFreshEntityWithPassengers(armorstand);
               level.playSound((Player)null, armorstand.getX(), armorstand.getY(), armorstand.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
               armorstand.gameEvent(GameEvent.ENTITY_PLACE, pContext.getPlayer());
            }

            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.FAIL;
         }
      }
   }

   private void randomizePose(ArmorStand pArmorStand, RandomSource pRandom) {
      Rotations rotations = pArmorStand.getHeadPose();
      float f = pRandom.nextFloat() * 5.0F;
      float f1 = pRandom.nextFloat() * 20.0F - 10.0F;
      Rotations rotations1 = new Rotations(rotations.getX() + f, rotations.getY() + f1, rotations.getZ());
      pArmorStand.setHeadPose(rotations1);
      rotations = pArmorStand.getBodyPose();
      f = pRandom.nextFloat() * 10.0F - 5.0F;
      rotations1 = new Rotations(rotations.getX(), rotations.getY() + f, rotations.getZ());
      pArmorStand.setBodyPose(rotations1);
   }
}
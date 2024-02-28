package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int LISTENER_RADIUS = 8;
   private static final int WARNING_SOUND_RADIUS = 10;
   private static final int WARDEN_SPAWN_ATTEMPTS = 20;
   private static final int WARDEN_SPAWN_RANGE_XZ = 5;
   private static final int WARDEN_SPAWN_RANGE_Y = 6;
   private static final int DARKNESS_RADIUS = 40;
   private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), (p_222866_) -> {
      p_222866_.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
      p_222866_.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
      p_222866_.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
      p_222866_.put(4, SoundEvents.WARDEN_LISTENING_ANGRY);
   });
   private static final int SHRIEKING_TICKS = 90;
   private int warningLevel;
   private VibrationListener listener = new VibrationListener(new BlockPositionSource(this.worldPosition), 8, this, (VibrationListener.ReceivingEvent)null, 0.0F, 0);

   public SculkShriekerBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.SCULK_SHRIEKER, pPos, pBlockState);
   }

   public VibrationListener getListener() {
      return this.listener;
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("warning_level", 99)) {
         this.warningLevel = pTag.getInt("warning_level");
      }

      if (pTag.contains("listener", 10)) {
         VibrationListener.codec(this).parse(new Dynamic<>(NbtOps.INSTANCE, pTag.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent((p_222864_) -> {
            this.listener = p_222864_;
         });
      }

   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putInt("warning_level", this.warningLevel);
      VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.listener).resultOrPartial(LOGGER::error).ifPresent((p_222871_) -> {
         pTag.put("listener", p_222871_);
      });
   }

   public TagKey<GameEvent> getListenableEvents() {
      return GameEventTags.SHRIEKER_CAN_LISTEN;
   }

   public boolean shouldListen(ServerLevel pLevel, GameEventListener pListener, BlockPos pPos, GameEvent pGameEvent, GameEvent.Context pContext) {
      return !this.isRemoved() && !this.getBlockState().getValue(SculkShriekerBlock.SHRIEKING) && tryGetPlayer(pContext.sourceEntity()) != null;
   }

   @Nullable
   public static ServerPlayer tryGetPlayer(@Nullable Entity pEntity) {
      if (pEntity instanceof ServerPlayer serverplayer1) {
         return serverplayer1;
      } else {
         if (pEntity != null) {
            Entity $$6 = pEntity.getControllingPassenger();
            if ($$6 instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)$$6;
               return serverplayer;
            }
         }

         if (pEntity instanceof Projectile projectile) {
            Entity entity1 = projectile.getOwner();
            if (entity1 instanceof ServerPlayer serverplayer3) {
               return serverplayer3;
            }
         }

         if (pEntity instanceof ItemEntity itementity) {
            Entity entity2 = itementity.getThrowingEntity();
            if (entity2 instanceof ServerPlayer serverplayer2) {
               return serverplayer2;
            }
         }

         return null;
      }
   }

   public void onSignalReceive(ServerLevel pLevel, GameEventListener pListener, BlockPos pSourcePos, GameEvent pGameEvent, @Nullable Entity pSourceEntity, @Nullable Entity pProjectileOwner, float pDistance) {
      this.tryShriek(pLevel, tryGetPlayer(pProjectileOwner != null ? pProjectileOwner : pSourceEntity));
   }

   public void tryShriek(ServerLevel pLevel, @Nullable ServerPlayer pPlayer) {
      if (pPlayer != null) {
         BlockState blockstate = this.getBlockState();
         if (!blockstate.getValue(SculkShriekerBlock.SHRIEKING)) {
            this.warningLevel = 0;
            if (!this.canRespond(pLevel) || this.tryToWarn(pLevel, pPlayer)) {
               this.shriek(pLevel, pPlayer);
            }
         }
      }
   }

   private boolean tryToWarn(ServerLevel pLevel, ServerPlayer pPlayer) {
      OptionalInt optionalint = WardenSpawnTracker.tryWarn(pLevel, this.getBlockPos(), pPlayer);
      optionalint.ifPresent((p_222838_) -> {
         this.warningLevel = p_222838_;
      });
      return optionalint.isPresent();
   }

   private void shriek(ServerLevel pLevel, @Nullable Entity pSourceEntity) {
      BlockPos blockpos = this.getBlockPos();
      BlockState blockstate = this.getBlockState();
      pLevel.setBlock(blockpos, blockstate.setValue(SculkShriekerBlock.SHRIEKING, Boolean.valueOf(true)), 2);
      pLevel.scheduleTick(blockpos, blockstate.getBlock(), 90);
      pLevel.levelEvent(3007, blockpos, 0);
      pLevel.gameEvent(GameEvent.SHRIEK, blockpos, GameEvent.Context.of(pSourceEntity));
   }

   private boolean canRespond(ServerLevel pLevel) {
      return this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON) && pLevel.getDifficulty() != Difficulty.PEACEFUL && pLevel.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING);
   }

   public void tryRespond(ServerLevel pLevel) {
      if (this.canRespond(pLevel) && this.warningLevel > 0) {
         if (!this.trySummonWarden(pLevel)) {
            this.playWardenReplySound();
         }

         Warden.applyDarknessAround(pLevel, Vec3.atCenterOf(this.getBlockPos()), (Entity)null, 40);
      }

   }

   private void playWardenReplySound() {
      SoundEvent soundevent = SOUND_BY_LEVEL.get(this.warningLevel);
      if (soundevent != null) {
         BlockPos blockpos = this.getBlockPos();
         int i = blockpos.getX() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
         int j = blockpos.getY() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
         int k = blockpos.getZ() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
         this.level.playSound((Player)null, (double)i, (double)j, (double)k, soundevent, SoundSource.HOSTILE, 5.0F, 1.0F);
      }

   }

   private boolean trySummonWarden(ServerLevel pLevel) {
      return this.warningLevel < 4 ? false : SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, pLevel, this.getBlockPos(), 20, 5, 6, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER).isPresent();
   }

   public void onSignalSchedule() {
      this.setChanged();
   }
}
package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public class SonicBoom extends Behavior<Warden> {
   private static final int DISTANCE_XZ = 15;
   private static final int DISTANCE_Y = 20;
   private static final double KNOCKBACK_VERTICAL = 0.5D;
   private static final double KNOCKBACK_HORIZONTAL = 2.5D;
   public static final int COOLDOWN = 40;
   private static final int TICKS_BEFORE_PLAYING_SOUND = Mth.ceil(34.0D);
   private static final int DURATION = Mth.ceil(60.0F);

   public SonicBoom() {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryStatus.REGISTERED, MemoryModuleType.SONIC_BOOM_SOUND_DELAY, MemoryStatus.REGISTERED), DURATION);
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Warden pOwner) {
      return pOwner.closerThan(pOwner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0D, 20.0D);
   }

   protected boolean canStillUse(ServerLevel pLevel, Warden pEntity, long pGameTime) {
      return true;
   }

   protected void start(ServerLevel pLevel, Warden pEntity, long pGameTime) {
      pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)DURATION);
      pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, (long)TICKS_BEFORE_PLAYING_SOUND);
      pLevel.broadcastEntityEvent(pEntity, (byte)62);
      pEntity.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
   }

   protected void tick(ServerLevel pLevel, Warden pOwner, long pGameTime) {
      pOwner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((p_217718_) -> {
         pOwner.getLookControl().setLookAt(p_217718_.position());
      });
      if (!pOwner.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY) && !pOwner.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
         pOwner.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, (long)(DURATION - TICKS_BEFORE_PLAYING_SOUND));
         pOwner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(pOwner::canTargetEntity).filter((p_217707_) -> {
            return pOwner.closerThan(p_217707_, 15.0D, 20.0D);
         }).ifPresent((p_217704_) -> {
            Vec3 vec3 = pOwner.position().add(0.0D, (double)1.6F, 0.0D);
            Vec3 vec31 = p_217704_.getEyePosition().subtract(vec3);
            Vec3 vec32 = vec31.normalize();

            for(int i = 1; i < Mth.floor(vec31.length()) + 7; ++i) {
               Vec3 vec33 = vec3.add(vec32.scale((double)i));
               pLevel.sendParticles(ParticleTypes.SONIC_BOOM, vec33.x, vec33.y, vec33.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            pOwner.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
            p_217704_.hurt(DamageSource.sonicBoom(pOwner), 10.0F);
            double d1 = 0.5D * (1.0D - p_217704_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double d0 = 2.5D * (1.0D - p_217704_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            p_217704_.push(vec32.x() * d0, vec32.y() * d1, vec32.z() * d0);
         });
      }
   }

   protected void stop(ServerLevel pLevel, Warden pEntity, long pGameTime) {
      setCooldown(pEntity, 40);
   }

   public static void setCooldown(LivingEntity pEntity, int pCooldown) {
      pEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, (long)pCooldown);
   }
}
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class RunOne<E extends LivingEntity> extends GateBehavior<E> {
   public RunOne(List<Pair<Behavior<? super E>, Integer>> pEntryCondition) {
      this(ImmutableMap.of(), pEntryCondition);
   }

   public RunOne(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, List<Pair<Behavior<? super E>, Integer>> pDurations) {
      super(pEntryCondition, ImmutableSet.of(), GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE, pDurations);
   }
}
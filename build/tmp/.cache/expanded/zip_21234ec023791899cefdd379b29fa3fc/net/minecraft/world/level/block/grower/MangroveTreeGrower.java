package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class MangroveTreeGrower extends AbstractTreeGrower {
   private final float tallProbability;

   public MangroveTreeGrower(float pTallProbability) {
      this.tallProbability = pTallProbability;
   }

   /**
    * @return a {@link net.minecraft.world.level.levelgen.feature.ConfiguredFeature} of this tree
    */
   @Nullable
   protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pLargeHive) {
      return pRandom.nextFloat() < this.tallProbability ? TreeFeatures.TALL_MANGROVE : TreeFeatures.MANGROVE;
   }
}
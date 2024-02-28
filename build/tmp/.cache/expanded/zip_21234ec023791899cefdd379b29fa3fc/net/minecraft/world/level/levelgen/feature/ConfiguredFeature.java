package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>>(F feature, FC config) {
   public static final Codec<ConfiguredFeature<?, ?>> DIRECT_CODEC = Registry.FEATURE.byNameCodec().dispatch((p_65391_) -> {
      return p_65391_.feature;
   }, Feature::configuredCodec);
   public static final Codec<Holder<ConfiguredFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<HolderSet<ConfiguredFeature<?, ?>>> LIST_CODEC = RegistryCodecs.homogeneousList(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);

   public boolean place(WorldGenLevel pReader, ChunkGenerator pChunkGenerator, RandomSource pRandom, BlockPos pPos) {
      return this.feature.place(this.config, pReader, pChunkGenerator, pRandom, pPos);
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(Stream.of(this), this.config.getFeatures());
   }

   public String toString() {
      return "Configured: " + this.feature + ": " + this.config;
   }
}
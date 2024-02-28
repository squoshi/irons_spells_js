package net.minecraft.world.level.levelgen.presets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class WorldPreset {
   public static final Codec<WorldPreset> DIRECT_CODEC = RecordCodecBuilder.<WorldPreset>create((p_226426_) -> {
      return p_226426_.group(Codec.unboundedMap(ResourceKey.codec(Registry.LEVEL_STEM_REGISTRY), LevelStem.CODEC).fieldOf("dimensions").forGetter((p_226430_) -> {
         return p_226430_.dimensions;
      })).apply(p_226426_, WorldPreset::new);
   }).flatXmap(WorldPreset::requireOverworld, WorldPreset::requireOverworld);
   public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registry.WORLD_PRESET_REGISTRY, DIRECT_CODEC);
   private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

   public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> p_226419_) {
      this.dimensions = p_226419_;
   }

   private Registry<LevelStem> createRegistry() {
      WritableRegistry<LevelStem> writableregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), (Function<LevelStem, Holder.Reference<LevelStem>>)null);
      LevelStem.keysInOrder(this.dimensions.keySet().stream()).forEach((p_226433_) -> {
         LevelStem levelstem = this.dimensions.get(p_226433_);
         if (levelstem != null) {
            writableregistry.register(p_226433_, levelstem, Lifecycle.stable());
         }

      });
      return writableregistry.freeze();
   }

   public WorldGenSettings createWorldGenSettings(long pSeed, boolean pGenerateStructures, boolean pGenerateBonusChest) {
      return new WorldGenSettings(pSeed, pGenerateStructures, pGenerateBonusChest, this.createRegistry());
   }

   public WorldGenSettings recreateWorldGenSettings(WorldGenSettings pWorldGenSettings) {
      return this.createWorldGenSettings(pWorldGenSettings.seed(), pWorldGenSettings.generateStructures(), pWorldGenSettings.generateBonusChest());
   }

   public Optional<LevelStem> overworld() {
      return Optional.ofNullable(this.dimensions.get(LevelStem.OVERWORLD));
   }

   public LevelStem overworldOrThrow() {
      return this.overworld().orElseThrow(() -> {
         return new IllegalStateException("Can't find overworld in this preset");
      });
   }

   private static DataResult<WorldPreset> requireOverworld(WorldPreset p_238379_) {
      return p_238379_.overworld().isEmpty() ? DataResult.error("Missing overworld dimension") : DataResult.success(p_238379_, Lifecycle.stable());
   }
}
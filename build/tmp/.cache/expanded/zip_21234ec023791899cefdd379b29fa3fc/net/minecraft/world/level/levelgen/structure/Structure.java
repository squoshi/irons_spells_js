package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class Structure {
   public static final Codec<Structure> DIRECT_CODEC = Registry.STRUCTURE_TYPES.byNameCodec().dispatch(Structure::type, StructureType::codec);
   public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create(Registry.STRUCTURE_REGISTRY, DIRECT_CODEC);
   //Forge: Make this field private so that the redirect coremod can target it
   private final Structure.StructureSettings settings;

   public static <S extends Structure> RecordCodecBuilder<S, Structure.StructureSettings> settingsCodec(RecordCodecBuilder.Instance<S> p_226568_) {
      return Structure.StructureSettings.CODEC.forGetter((p_226595_) -> {
         return p_226595_.modifiableStructureInfo().getOriginalStructureInfo().structureSettings(); // FORGE: Patch codec to ignore field redirect coremods.
      });
   }

   public static <S extends Structure> Codec<S> simpleCodec(Function<Structure.StructureSettings, S> p_226608_) {
      return RecordCodecBuilder.create((p_226611_) -> {
         return p_226611_.group(settingsCodec(p_226611_)).apply(p_226611_, p_226608_);
      });
   }

   protected Structure(Structure.StructureSettings pSettings) {
      this.settings = pSettings;
      this.modifiableStructureInfo = new net.minecraftforge.common.world.ModifiableStructureInfo(new net.minecraftforge.common.world.ModifiableStructureInfo.StructureInfo(pSettings)); // FORGE: cache original structure info on construction so we can bypass our field read coremods where necessary
   }

   public HolderSet<Biome> biomes() {
      return this.settings.biomes;
   }

   public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
      return this.settings.spawnOverrides;
   }

   public GenerationStep.Decoration step() {
      return this.settings.step;
   }

   public TerrainAdjustment terrainAdaptation() {
      return this.settings.terrainAdaptation;
   }

   public BoundingBox adjustBoundingBox(BoundingBox pBoundingBox) {
      return this.terrainAdaptation() != TerrainAdjustment.NONE ? pBoundingBox.inflatedBy(12) : pBoundingBox;
   }

   public StructureStart generate(RegistryAccess pRegistryAccess, ChunkGenerator pChunkGenerator, BiomeSource pBiomeSource, RandomState pRandomState, StructureTemplateManager pStructureTemplateManager, long pSeed, ChunkPos pChunkPos, int p_226604_, LevelHeightAccessor pHeightAccessor, Predicate<Holder<Biome>> pValidBiome) {
      Optional<Structure.GenerationStub> optional = this.findGenerationPoint(new Structure.GenerationContext(pRegistryAccess, pChunkGenerator, pBiomeSource, pRandomState, pStructureTemplateManager, pSeed, pChunkPos, pHeightAccessor, pValidBiome));
      if (optional.isPresent() && isValidBiome(optional.get(), pChunkGenerator, pRandomState, pValidBiome)) {
         StructurePiecesBuilder structurepiecesbuilder = optional.get().getPiecesBuilder();
         StructureStart structurestart = new StructureStart(this, pChunkPos, p_226604_, structurepiecesbuilder.build());
         if (structurestart.isValid()) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   protected static Optional<Structure.GenerationStub> onTopOfChunkCenter(Structure.GenerationContext pContext, Heightmap.Types pHeightmapTypes, Consumer<StructurePiecesBuilder> pGenerator) {
      ChunkPos chunkpos = pContext.chunkPos();
      int i = chunkpos.getMiddleBlockX();
      int j = chunkpos.getMiddleBlockZ();
      int k = pContext.chunkGenerator().getFirstOccupiedHeight(i, j, pHeightmapTypes, pContext.heightAccessor(), pContext.randomState());
      return Optional.of(new Structure.GenerationStub(new BlockPos(i, k, j), pGenerator));
   }

   private static boolean isValidBiome(Structure.GenerationStub p_226590_, ChunkGenerator pChunkGenerator, RandomState pRandomState, Predicate<Holder<Biome>> pValidBiome) {
      BlockPos blockpos = p_226590_.position();
      return pValidBiome.test(pChunkGenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockpos.getX()), QuartPos.fromBlock(blockpos.getY()), QuartPos.fromBlock(blockpos.getZ()), pRandomState.sampler()));
   }

   public void afterPlace(WorldGenLevel p_226560_, StructureManager p_226561_, ChunkGenerator p_226562_, RandomSource p_226563_, BoundingBox p_226564_, ChunkPos p_226565_, PiecesContainer p_226566_) {
   }

   private static int[] getCornerHeights(Structure.GenerationContext pContext, int p_226615_, int p_226616_, int p_226617_, int p_226618_) {
      ChunkGenerator chunkgenerator = pContext.chunkGenerator();
      LevelHeightAccessor levelheightaccessor = pContext.heightAccessor();
      RandomState randomstate = pContext.randomState();
      return new int[]{chunkgenerator.getFirstOccupiedHeight(p_226615_, p_226617_, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(p_226615_, p_226617_ + p_226618_, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(p_226615_ + p_226616_, p_226617_, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(p_226615_ + p_226616_, p_226617_ + p_226618_, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate)};
   }

   protected static int getLowestY(Structure.GenerationContext pContext, int p_226574_, int p_226575_) {
      ChunkPos chunkpos = pContext.chunkPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      return getLowestY(pContext, i, j, p_226574_, p_226575_);
   }

   protected static int getLowestY(Structure.GenerationContext pContext, int p_226578_, int p_226579_, int p_226580_, int p_226581_) {
      int[] aint = getCornerHeights(pContext, p_226578_, p_226580_, p_226579_, p_226581_);
      return Math.min(Math.min(aint[0], aint[1]), Math.min(aint[2], aint[3]));
   }

   /** @deprecated */
   @Deprecated
   protected BlockPos getLowestYIn5by5BoxOffset7Blocks(Structure.GenerationContext pContext, Rotation pRotation) {
      int i = 5;
      int j = 5;
      if (pRotation == Rotation.CLOCKWISE_90) {
         i = -5;
      } else if (pRotation == Rotation.CLOCKWISE_180) {
         i = -5;
         j = -5;
      } else if (pRotation == Rotation.COUNTERCLOCKWISE_90) {
         j = -5;
      }

      ChunkPos chunkpos = pContext.chunkPos();
      int k = chunkpos.getBlockX(7);
      int l = chunkpos.getBlockZ(7);
      return new BlockPos(k, getLowestY(pContext, k, l, i, j), l);
   }

   public abstract Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext);

   public abstract StructureType<?> type();

   // FORGE START

   private final net.minecraftforge.common.world.ModifiableStructureInfo modifiableStructureInfo;

   /**
    * {@return Cache of original structure data and structure data modified by structure modifiers}
    * Modified structure data is set by server after datapacks and serverconfigs load.
    * Settings field reads are coremodded to redirect to this.
    **/
   public net.minecraftforge.common.world.ModifiableStructureInfo modifiableStructureInfo()
   {
      return this.modifiableStructureInfo;
   }

   /**
    * {@return The structure's settings, with modifications if called after modifiers are applied in server init.}
    */
   public StructureSettings getModifiedStructureSettings() {
      return this.modifiableStructureInfo().get().structureSettings();
   }

   // FORGE END

   public static record GenerationContext(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, WorldgenRandom random, long seed, ChunkPos chunkPos, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome) {
      public GenerationContext(RegistryAccess pRegistryAccess, ChunkGenerator pChunkGenerator, BiomeSource pBiomeSource, RandomState pRandomState, StructureTemplateManager pStructureTemplateManager, long pSeed, ChunkPos pChunkPos, LevelHeightAccessor pHeightAccessor, Predicate<Holder<Biome>> pValidBiome) {
         this(pRegistryAccess, pChunkGenerator, pBiomeSource, pRandomState, pStructureTemplateManager, makeRandom(pSeed, pChunkPos), pSeed, pChunkPos, pHeightAccessor, pValidBiome);
      }

      private static WorldgenRandom makeRandom(long pSeed, ChunkPos pChunkPos) {
         WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
         worldgenrandom.setLargeFeatureSeed(pSeed, pChunkPos.x, pChunkPos.z);
         return worldgenrandom;
      }
   }

   public static record GenerationStub(BlockPos position, Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder> generator) {
      public GenerationStub(BlockPos pPosition, Consumer<StructurePiecesBuilder> pGenerator) {
         this(pPosition, Either.left(pGenerator));
      }

      public StructurePiecesBuilder getPiecesBuilder() {
         return this.generator.map((p_226681_) -> {
            StructurePiecesBuilder structurepiecesbuilder = new StructurePiecesBuilder();
            p_226681_.accept(structurepiecesbuilder);
            return structurepiecesbuilder;
         }, (p_226679_) -> {
            return p_226679_;
         });
      }
   }

   public static record StructureSettings(HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> spawnOverrides, GenerationStep.Decoration step, TerrainAdjustment terrainAdaptation) {
      public static final MapCodec<Structure.StructureSettings> CODEC = RecordCodecBuilder.mapCodec((p_226701_) -> {
         return p_226701_.group(RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(Structure.StructureSettings::biomes), Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values())).fieldOf("spawn_overrides").forGetter(Structure.StructureSettings::spawnOverrides), GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(Structure.StructureSettings::step), TerrainAdjustment.CODEC.optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE).forGetter(Structure.StructureSettings::terrainAdaptation)).apply(p_226701_, Structure.StructureSettings::new);
      });
   }
}

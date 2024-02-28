package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class SinglePoolElement extends StructurePoolElement {
   private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map(Either::left));
   public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create((p_210429_) -> {
      return p_210429_.group(templateCodec(), processorsCodec(), projectionCodec()).apply(p_210429_, SinglePoolElement::new);
   });
   protected final Either<ResourceLocation, StructureTemplate> template;
   protected final Holder<StructureProcessorList> processors;

   private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> p_210425_, DynamicOps<T> p_210426_, T p_210427_) {
      Optional<ResourceLocation> optional = p_210425_.left();
      return !optional.isPresent() ? DataResult.error("Can not serialize a runtime pool element") : ResourceLocation.CODEC.encode(optional.get(), p_210426_, p_210427_);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Holder<StructureProcessorList>> processorsCodec() {
      return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter((p_210464_) -> {
         return p_210464_.processors;
      });
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
      return TEMPLATE_CODEC.fieldOf("location").forGetter((p_210431_) -> {
         return p_210431_.template;
      });
   }

   protected SinglePoolElement(Either<ResourceLocation, StructureTemplate> p_210415_, Holder<StructureProcessorList> p_210416_, StructureTemplatePool.Projection p_210417_) {
      super(p_210417_);
      this.template = p_210415_;
      this.processors = p_210416_;
   }

   public SinglePoolElement(StructureTemplate pStructureTemplate) {
      this(Either.right(pStructureTemplate), ProcessorLists.EMPTY, StructureTemplatePool.Projection.RIGID);
   }

   public Vec3i getSize(StructureTemplateManager pStructureTemplateManager, Rotation pRotation) {
      StructureTemplate structuretemplate = this.getTemplate(pStructureTemplateManager);
      return structuretemplate.getSize(pRotation);
   }

   private StructureTemplate getTemplate(StructureTemplateManager pStructureTemplateManager) {
      return this.template.map(pStructureTemplateManager::getOrCreate, Function.identity());
   }

   public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureTemplateManager pStructureTemplateManager, BlockPos pPos, Rotation pRotation, boolean p_227328_) {
      StructureTemplate structuretemplate = this.getTemplate(pStructureTemplateManager);
      List<StructureTemplate.StructureBlockInfo> list = structuretemplate.filterBlocks(pPos, (new StructurePlaceSettings()).setRotation(pRotation), Blocks.STRUCTURE_BLOCK, p_227328_);
      List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();

      for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : list) {
         if (structuretemplate$structureblockinfo.nbt != null) {
            StructureMode structuremode = StructureMode.valueOf(structuretemplate$structureblockinfo.nbt.getString("mode"));
            if (structuremode == StructureMode.DATA) {
               list1.add(structuretemplate$structureblockinfo);
            }
         }
      }

      return list1;
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager p_227320_, BlockPos p_227321_, Rotation p_227322_, RandomSource p_227323_) {
      StructureTemplate structuretemplate = this.getTemplate(p_227320_);
      ObjectArrayList<StructureTemplate.StructureBlockInfo> objectarraylist = structuretemplate.filterBlocks(p_227321_, (new StructurePlaceSettings()).setRotation(p_227322_), Blocks.JIGSAW, true);
      Util.shuffle(objectarraylist, p_227323_);
      return objectarraylist;
   }

   public BoundingBox getBoundingBox(StructureTemplateManager pStructureTemplateManager, BlockPos p_227317_, Rotation pRotation) {
      StructureTemplate structuretemplate = this.getTemplate(pStructureTemplateManager);
      return structuretemplate.getBoundingBox((new StructurePlaceSettings()).setRotation(pRotation), p_227317_);
   }

   public boolean place(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, BlockPos p_227306_, BlockPos p_227307_, Rotation pRotation, BoundingBox pBox, RandomSource pRandom, boolean p_227311_) {
      StructureTemplate structuretemplate = this.getTemplate(pStructureTemplateManager);
      StructurePlaceSettings structureplacesettings = this.getSettings(pRotation, pBox, p_227311_);
      if (!structuretemplate.placeInWorld(pLevel, p_227306_, p_227307_, structureplacesettings, pRandom, 18)) {
         return false;
      } else {
         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : StructureTemplate.processBlockInfos(pLevel, p_227306_, p_227307_, structureplacesettings, this.getDataMarkers(pStructureTemplateManager, p_227306_, pRotation, false))) {
            this.handleDataMarker(pLevel, structuretemplate$structureblockinfo, p_227306_, pRotation, pRandom, pBox);
         }

         return true;
      }
   }

   protected StructurePlaceSettings getSettings(Rotation p_210421_, BoundingBox p_210422_, boolean p_210423_) {
      StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
      structureplacesettings.setBoundingBox(p_210422_);
      structureplacesettings.setRotation(p_210421_);
      structureplacesettings.setKnownShape(true);
      structureplacesettings.setIgnoreEntities(false);
      structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      structureplacesettings.setFinalizeEntities(true);
      if (!p_210423_) {
         structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
      }

      this.processors.value().list().forEach(structureplacesettings::addProcessor);
      this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
      return structureplacesettings;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.SINGLE;
   }

   public String toString() {
      return "Single[" + this.template + "]";
   }
}
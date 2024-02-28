package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
   static final Logger LOGGER = LogUtils.getLogger();

   public static Optional<Structure.GenerationStub> addPieces(Structure.GenerationContext pContext, Holder<StructureTemplatePool> p_227240_, Optional<ResourceLocation> p_227241_, int p_227242_, BlockPos p_227243_, boolean p_227244_, Optional<Heightmap.Types> p_227245_, int p_227246_) {
      RegistryAccess registryaccess = pContext.registryAccess();
      ChunkGenerator chunkgenerator = pContext.chunkGenerator();
      StructureTemplateManager structuretemplatemanager = pContext.structureTemplateManager();
      LevelHeightAccessor levelheightaccessor = pContext.heightAccessor();
      WorldgenRandom worldgenrandom = pContext.random();
      Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
      Rotation rotation = Rotation.getRandom(worldgenrandom);
      StructureTemplatePool structuretemplatepool = p_227240_.value();
      StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(worldgenrandom);
      if (structurepoolelement == EmptyPoolElement.INSTANCE) {
         return Optional.empty();
      } else {
         BlockPos blockpos;
         if (p_227241_.isPresent()) {
            ResourceLocation resourcelocation = p_227241_.get();
            Optional<BlockPos> optional = getRandomNamedJigsaw(structurepoolelement, resourcelocation, p_227243_, rotation, structuretemplatemanager, worldgenrandom);
            if (optional.isEmpty()) {
               LOGGER.error("No starting jigsaw {} found in start pool {}", resourcelocation, p_227240_.unwrapKey().get().location());
               return Optional.empty();
            }

            blockpos = optional.get();
         } else {
            blockpos = p_227243_;
         }

         Vec3i vec3i = blockpos.subtract(p_227243_);
         BlockPos blockpos1 = p_227243_.subtract(vec3i);
         PoolElementStructurePiece poolelementstructurepiece = new PoolElementStructurePiece(structuretemplatemanager, structurepoolelement, blockpos1, structurepoolelement.getGroundLevelDelta(), rotation, structurepoolelement.getBoundingBox(structuretemplatemanager, blockpos1, rotation));
         BoundingBox boundingbox = poolelementstructurepiece.getBoundingBox();
         int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
         int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
         int k;
         if (p_227245_.isPresent()) {
            k = p_227243_.getY() + chunkgenerator.getFirstFreeHeight(i, j, p_227245_.get(), levelheightaccessor, pContext.randomState());
         } else {
            k = blockpos1.getY();
         }

         int l = boundingbox.minY() + poolelementstructurepiece.getGroundLevelDelta();
         poolelementstructurepiece.move(0, k - l, 0);
         int i1 = k + vec3i.getY();
         return Optional.of(new Structure.GenerationStub(new BlockPos(i, i1, j), (p_227237_) -> {
            List<PoolElementStructurePiece> list = Lists.newArrayList();
            list.add(poolelementstructurepiece);
            if (p_227242_ > 0) {
               AABB aabb = new AABB((double)(i - p_227246_), (double)(i1 - p_227246_), (double)(j - p_227246_), (double)(i + p_227246_ + 1), (double)(i1 + p_227246_ + 1), (double)(j + p_227246_ + 1));
               VoxelShape voxelshape = Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST);
               addPieces(pContext.randomState(), p_227242_, p_227244_, chunkgenerator, structuretemplatemanager, levelheightaccessor, worldgenrandom, registry, poolelementstructurepiece, list, voxelshape);
               list.forEach(p_227237_::addPiece);
            }
         }));
      }
   }

   private static Optional<BlockPos> getRandomNamedJigsaw(StructurePoolElement p_227248_, ResourceLocation p_227249_, BlockPos p_227250_, Rotation p_227251_, StructureTemplateManager p_227252_, WorldgenRandom p_227253_) {
      List<StructureTemplate.StructureBlockInfo> list = p_227248_.getShuffledJigsawBlocks(p_227252_, p_227250_, p_227251_, p_227253_);
      Optional<BlockPos> optional = Optional.empty();

      for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : list) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(structuretemplate$structureblockinfo.nbt.getString("name"));
         if (p_227249_.equals(resourcelocation)) {
            optional = Optional.of(structuretemplate$structureblockinfo.pos);
            break;
         }
      }

      return optional;
   }

   private static void addPieces(RandomState p_227211_, int pMaxDepth, boolean p_227213_, ChunkGenerator pChunkGenerator, StructureTemplateManager pStructureTemplateManager, LevelHeightAccessor p_227216_, RandomSource pRandom, Registry<StructureTemplatePool> pPools, PoolElementStructurePiece p_227219_, List<PoolElementStructurePiece> pPieces, VoxelShape p_227221_) {
      JigsawPlacement.Placer jigsawplacement$placer = new JigsawPlacement.Placer(pPools, pMaxDepth, pChunkGenerator, pStructureTemplateManager, pPieces, pRandom);
      jigsawplacement$placer.placing.addLast(new JigsawPlacement.PieceState(p_227219_, new MutableObject<>(p_227221_), 0));

      while(!jigsawplacement$placer.placing.isEmpty()) {
         JigsawPlacement.PieceState jigsawplacement$piecestate = jigsawplacement$placer.placing.removeFirst();
         jigsawplacement$placer.tryPlacingChildren(jigsawplacement$piecestate.piece, jigsawplacement$piecestate.free, jigsawplacement$piecestate.depth, p_227213_, p_227216_, p_227211_);
      }

   }

   public static boolean generateJigsaw(ServerLevel pLevel, Holder<StructureTemplatePool> p_227205_, ResourceLocation p_227206_, int p_227207_, BlockPos p_227208_, boolean p_227209_) {
      ChunkGenerator chunkgenerator = pLevel.getChunkSource().getGenerator();
      StructureTemplateManager structuretemplatemanager = pLevel.getStructureManager();
      StructureManager structuremanager = pLevel.structureManager();
      RandomSource randomsource = pLevel.getRandom();
      Structure.GenerationContext structure$generationcontext = new Structure.GenerationContext(pLevel.registryAccess(), chunkgenerator, chunkgenerator.getBiomeSource(), pLevel.getChunkSource().randomState(), structuretemplatemanager, pLevel.getSeed(), new ChunkPos(p_227208_), pLevel, (p_227255_) -> {
         return true;
      });
      Optional<Structure.GenerationStub> optional = addPieces(structure$generationcontext, p_227205_, Optional.of(p_227206_), p_227207_, p_227208_, false, Optional.empty(), 128);
      if (optional.isPresent()) {
         StructurePiecesBuilder structurepiecesbuilder = optional.get().getPiecesBuilder();

         for(StructurePiece structurepiece : structurepiecesbuilder.build().pieces()) {
            if (structurepiece instanceof PoolElementStructurePiece) {
               PoolElementStructurePiece poolelementstructurepiece = (PoolElementStructurePiece)structurepiece;
               poolelementstructurepiece.place(pLevel, structuremanager, chunkgenerator, randomsource, BoundingBox.infinite(), p_227208_, p_227209_);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static final class PieceState {
      final PoolElementStructurePiece piece;
      final MutableObject<VoxelShape> free;
      final int depth;

      PieceState(PoolElementStructurePiece pPiece, MutableObject<VoxelShape> pFree, int pDepth) {
         this.piece = pPiece;
         this.free = pFree;
         this.depth = pDepth;
      }
   }

   static final class Placer {
      private final Registry<StructureTemplatePool> pools;
      private final int maxDepth;
      private final ChunkGenerator chunkGenerator;
      private final StructureTemplateManager structureTemplateManager;
      private final List<? super PoolElementStructurePiece> pieces;
      private final RandomSource random;
      final Deque<JigsawPlacement.PieceState> placing = Queues.newArrayDeque();

      Placer(Registry<StructureTemplatePool> pPools, int pMaxDepth, ChunkGenerator pChunkGenerator, StructureTemplateManager pStructureTemplateManager, List<? super PoolElementStructurePiece> pPieces, RandomSource pRandom) {
         this.pools = pPools;
         this.maxDepth = pMaxDepth;
         this.chunkGenerator = pChunkGenerator;
         this.structureTemplateManager = pStructureTemplateManager;
         this.pieces = pPieces;
         this.random = pRandom;
      }

      void tryPlacingChildren(PoolElementStructurePiece pPiece, MutableObject<VoxelShape> p_227266_, int pDepth, boolean p_227268_, LevelHeightAccessor p_227269_, RandomState p_227270_) {
         StructurePoolElement structurepoolelement = pPiece.getElement();
         BlockPos blockpos = pPiece.getPosition();
         Rotation rotation = pPiece.getRotation();
         StructureTemplatePool.Projection structuretemplatepool$projection = structurepoolelement.getProjection();
         boolean flag = structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID;
         MutableObject<VoxelShape> mutableobject = new MutableObject<>();
         BoundingBox boundingbox = pPiece.getBoundingBox();
         int i = boundingbox.minY();

         label139:
         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : structurepoolelement.getShuffledJigsawBlocks(this.structureTemplateManager, blockpos, rotation, this.random)) {
            Direction direction = JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state);
            BlockPos blockpos1 = structuretemplate$structureblockinfo.pos;
            BlockPos blockpos2 = blockpos1.relative(direction);
            int j = blockpos1.getY() - i;
            int k = -1;
            ResourceLocation resourcelocation = new ResourceLocation(structuretemplate$structureblockinfo.nbt.getString("pool"));
            Optional<StructureTemplatePool> optional = this.pools.getOptional(resourcelocation);
            if (optional.isPresent() && (optional.get().size() != 0 || Objects.equals(resourcelocation, Pools.EMPTY.location()))) {
               ResourceLocation resourcelocation1 = optional.get().getFallback();
               Optional<StructureTemplatePool> optional1 = this.pools.getOptional(resourcelocation1);
               if (optional1.isPresent() && (optional1.get().size() != 0 || Objects.equals(resourcelocation1, Pools.EMPTY.location()))) {
                  boolean flag1 = boundingbox.isInside(blockpos2);
                  MutableObject<VoxelShape> mutableobject1;
                  if (flag1) {
                     mutableobject1 = mutableobject;
                     if (mutableobject.getValue() == null) {
                        mutableobject.setValue(Shapes.create(AABB.of(boundingbox)));
                     }
                  } else {
                     mutableobject1 = p_227266_;
                  }

                  List<StructurePoolElement> list = Lists.newArrayList();
                  if (pDepth != this.maxDepth) {
                     list.addAll(optional.get().getShuffledTemplates(this.random));
                  }

                  list.addAll(optional1.get().getShuffledTemplates(this.random));

                  for(StructurePoolElement structurepoolelement1 : list) {
                     if (structurepoolelement1 == EmptyPoolElement.INSTANCE) {
                        break;
                     }

                     for(Rotation rotation1 : Rotation.getShuffled(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list1 = structurepoolelement1.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPos.ZERO, rotation1, this.random);
                        BoundingBox boundingbox1 = structurepoolelement1.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation1);
                        int l;
                        if (p_227268_ && boundingbox1.getYSpan() <= 16) {
                           l = list1.stream().mapToInt((p_210332_) -> {
                              if (!boundingbox1.isInside(p_210332_.pos.relative(JigsawBlock.getFrontFacing(p_210332_.state)))) {
                                 return 0;
                              } else {
                                 ResourceLocation resourcelocation2 = new ResourceLocation(p_210332_.nbt.getString("pool"));
                                 Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourcelocation2);
                                 Optional<StructureTemplatePool> optional3 = optional2.flatMap((p_210344_) -> {
                                    return this.pools.getOptional(p_210344_.getFallback());
                                 });
                                 int j3 = optional2.map((p_210342_) -> {
                                    return p_210342_.getMaxSize(this.structureTemplateManager);
                                 }).orElse(0);
                                 int k3 = optional3.map((p_210340_) -> {
                                    return p_210340_.getMaxSize(this.structureTemplateManager);
                                 }).orElse(0);
                                 return Math.max(j3, k3);
                              }
                           }).max().orElse(0);
                        } else {
                           l = 0;
                        }

                        for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : list1) {
                           if (JigsawBlock.canAttach(structuretemplate$structureblockinfo, structuretemplate$structureblockinfo1)) {
                              BlockPos blockpos3 = structuretemplate$structureblockinfo1.pos;
                              BlockPos blockpos4 = blockpos2.subtract(blockpos3);
                              BoundingBox boundingbox2 = structurepoolelement1.getBoundingBox(this.structureTemplateManager, blockpos4, rotation1);
                              int i1 = boundingbox2.minY();
                              StructureTemplatePool.Projection structuretemplatepool$projection1 = structurepoolelement1.getProjection();
                              boolean flag2 = structuretemplatepool$projection1 == StructureTemplatePool.Projection.RIGID;
                              int j1 = blockpos3.getY();
                              int k1 = j - j1 + JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state).getStepY();
                              int l1;
                              if (flag && flag2) {
                                 l1 = i + k1;
                              } else {
                                 if (k == -1) {
                                    k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, p_227269_, p_227270_);
                                 }

                                 l1 = k - j1;
                              }

                              int i2 = l1 - i1;
                              BoundingBox boundingbox3 = boundingbox2.moved(0, i2, 0);
                              BlockPos blockpos5 = blockpos4.offset(0, i2, 0);
                              if (l > 0) {
                                 int j2 = Math.max(l + 1, boundingbox3.maxY() - boundingbox3.minY());
                                 boundingbox3.encapsulate(new BlockPos(boundingbox3.minX(), boundingbox3.minY() + j2, boundingbox3.minZ()));
                              }

                              if (!Shapes.joinIsNotEmpty(mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3).deflate(0.25D)), BooleanOp.ONLY_SECOND)) {
                                 mutableobject1.setValue(Shapes.joinUnoptimized(mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3)), BooleanOp.ONLY_FIRST));
                                 int i3 = pPiece.getGroundLevelDelta();
                                 int k2;
                                 if (flag2) {
                                    k2 = i3 - k1;
                                 } else {
                                    k2 = structurepoolelement1.getGroundLevelDelta();
                                 }

                                 PoolElementStructurePiece poolelementstructurepiece = new PoolElementStructurePiece(this.structureTemplateManager, structurepoolelement1, blockpos5, k2, rotation1, boundingbox3);
                                 int l2;
                                 if (flag) {
                                    l2 = i + j;
                                 } else if (flag2) {
                                    l2 = l1 + j1;
                                 } else {
                                    if (k == -1) {
                                       k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, p_227269_, p_227270_);
                                    }

                                    l2 = k + k1 / 2;
                                 }

                                 pPiece.addJunction(new JigsawJunction(blockpos2.getX(), l2 - j + i3, blockpos2.getZ(), k1, structuretemplatepool$projection1));
                                 poolelementstructurepiece.addJunction(new JigsawJunction(blockpos1.getX(), l2 - j1 + k2, blockpos1.getZ(), -k1, structuretemplatepool$projection));
                                 this.pieces.add(poolelementstructurepiece);
                                 if (pDepth + 1 <= this.maxDepth) {
                                    this.placing.addLast(new JigsawPlacement.PieceState(poolelementstructurepiece, mutableobject1, pDepth + 1));
                                 }
                                 continue label139;
                              }
                           }
                        }
                     }
                  }
               } else {
                  JigsawPlacement.LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)resourcelocation1);
               }
            } else {
               JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", (Object)resourcelocation);
            }
         }

      }
   }
}
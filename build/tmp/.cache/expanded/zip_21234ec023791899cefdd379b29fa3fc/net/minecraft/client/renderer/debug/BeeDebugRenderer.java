package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
   private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
   private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
   private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
   private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
   private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
   private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_HIVE_MEMBERS = true;
   private static final boolean SHOW_BLACKLISTS = true;
   private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
   private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
   private static final int MAX_TARGETING_DIST = 8;
   private static final int HIVE_TIMEOUT = 20;
   private static final float TEXT_SCALE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int ORANGE = -23296;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private final Minecraft minecraft;
   private final Map<BlockPos, BeeDebugRenderer.HiveInfo> hives = Maps.newHashMap();
   private final Map<UUID, BeeDebugRenderer.BeeInfo> beeInfosPerEntity = Maps.newHashMap();
   private UUID lastLookedAtUuid;

   public BeeDebugRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void clear() {
      this.hives.clear();
      this.beeInfosPerEntity.clear();
      this.lastLookedAtUuid = null;
   }

   public void addOrUpdateHiveInfo(BeeDebugRenderer.HiveInfo pHiveInfo) {
      this.hives.put(pHiveInfo.pos, pHiveInfo);
   }

   public void addOrUpdateBeeInfo(BeeDebugRenderer.BeeInfo pBeeInfo) {
      this.beeInfosPerEntity.put(pBeeInfo.uuid, pBeeInfo);
   }

   public void removeBeeInfo(int pId) {
      this.beeInfosPerEntity.values().removeIf((p_173767_) -> {
         return p_173767_.id == pId;
      });
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBufferSource, double pCamX, double pCamY, double pCamZ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableTexture();
      this.clearRemovedHives();
      this.clearRemovedBees();
      this.doRender();
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
      if (!this.minecraft.player.isSpectator()) {
         this.updateLastLookedAtUuid();
      }

   }

   private void clearRemovedBees() {
      this.beeInfosPerEntity.entrySet().removeIf((p_113132_) -> {
         return this.minecraft.level.getEntity((p_113132_.getValue()).id) == null;
      });
   }

   private void clearRemovedHives() {
      long i = this.minecraft.level.getGameTime() - 20L;
      this.hives.entrySet().removeIf((p_113057_) -> {
         return (p_113057_.getValue()).lastSeen < i;
      });
   }

   private void doRender() {
      BlockPos blockpos = this.getCamera().getBlockPosition();
      this.beeInfosPerEntity.values().forEach((p_113153_) -> {
         if (this.isPlayerCloseEnoughToMob(p_113153_)) {
            this.renderBeeInfo(p_113153_);
         }

      });
      this.renderFlowerInfos();

      for(BlockPos blockpos1 : this.hives.keySet()) {
         if (blockpos.closerThan(blockpos1, 30.0D)) {
            highlightHive(blockpos1);
         }
      }

      Map<BlockPos, Set<UUID>> map = this.createHiveBlacklistMap();
      this.hives.values().forEach((p_113098_) -> {
         if (blockpos.closerThan(p_113098_.pos, 30.0D)) {
            Set<UUID> set = map.get(p_113098_.pos);
            this.renderHiveInfo(p_113098_, (Collection<UUID>)(set == null ? Sets.newHashSet() : set));
         }

      });
      this.getGhostHives().forEach((p_113090_, p_113091_) -> {
         if (blockpos.closerThan(p_113090_, 30.0D)) {
            this.renderGhostHive(p_113090_, p_113091_);
         }

      });
   }

   private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.beeInfosPerEntity.values().forEach((p_113135_) -> {
         p_113135_.blacklistedHives.forEach((p_173771_) -> {
            map.computeIfAbsent(p_173771_, (p_173777_) -> {
               return Sets.newHashSet();
            }).add(p_113135_.getUuid());
         });
      });
      return map;
   }

   private void renderFlowerInfos() {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.beeInfosPerEntity.values().stream().filter(BeeDebugRenderer.BeeInfo::hasFlower).forEach((p_113121_) -> {
         map.computeIfAbsent(p_113121_.flowerPos, (p_173775_) -> {
            return Sets.newHashSet();
         }).add(p_113121_.getUuid());
      });
      map.entrySet().forEach((p_113118_) -> {
         BlockPos blockpos = p_113118_.getKey();
         Set<UUID> set = p_113118_.getValue();
         Set<String> set1 = set.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
         int i = 1;
         renderTextOverPos(set1.toString(), blockpos, i++, -256);
         renderTextOverPos("Flower", blockpos, i++, -1);
         float f = 0.05F;
         renderTransparentFilledBox(blockpos, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
      });
   }

   private static String getBeeUuidsAsString(Collection<UUID> pBeeUuids) {
      if (pBeeUuids.isEmpty()) {
         return "-";
      } else {
         return pBeeUuids.size() > 3 ? pBeeUuids.size() + " bees" : pBeeUuids.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet()).toString();
      }
   }

   private static void highlightHive(BlockPos pPos) {
      float f = 0.05F;
      renderTransparentFilledBox(pPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void renderGhostHive(BlockPos pPos, List<String> pBeeNames) {
      float f = 0.05F;
      renderTransparentFilledBox(pPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      renderTextOverPos("" + pBeeNames, pPos, 0, -256);
      renderTextOverPos("Ghost Hive", pPos, 1, -65536);
   }

   private static void renderTransparentFilledBox(BlockPos pPos, float pSize, float pRed, float pGreen, float pBlue, float pAlpha) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      DebugRenderer.renderFilledBox(pPos, pSize, pRed, pGreen, pBlue, pAlpha);
   }

   private void renderHiveInfo(BeeDebugRenderer.HiveInfo pHiveInfo, Collection<UUID> pBeeUuids) {
      int i = 0;
      if (!pBeeUuids.isEmpty()) {
         renderTextOverHive("Blacklisted by " + getBeeUuidsAsString(pBeeUuids), pHiveInfo, i++, -65536);
      }

      renderTextOverHive("Out: " + getBeeUuidsAsString(this.getHiveMembers(pHiveInfo.pos)), pHiveInfo, i++, -3355444);
      if (pHiveInfo.occupantCount == 0) {
         renderTextOverHive("In: -", pHiveInfo, i++, -256);
      } else if (pHiveInfo.occupantCount == 1) {
         renderTextOverHive("In: 1 bee", pHiveInfo, i++, -256);
      } else {
         renderTextOverHive("In: " + pHiveInfo.occupantCount + " bees", pHiveInfo, i++, -256);
      }

      renderTextOverHive("Honey: " + pHiveInfo.honeyLevel, pHiveInfo, i++, -23296);
      renderTextOverHive(pHiveInfo.hiveType + (pHiveInfo.sedated ? " (sedated)" : ""), pHiveInfo, i++, -1);
   }

   private void renderPath(BeeDebugRenderer.BeeInfo pBeeInfo) {
      if (pBeeInfo.path != null) {
         PathfindingRenderer.renderPath(pBeeInfo.path, 0.5F, false, false, this.getCamera().getPosition().x(), this.getCamera().getPosition().y(), this.getCamera().getPosition().z());
      }

   }

   private void renderBeeInfo(BeeDebugRenderer.BeeInfo pBeeInfo) {
      boolean flag = this.isBeeSelected(pBeeInfo);
      int i = 0;
      renderTextOverMob(pBeeInfo.pos, i++, pBeeInfo.toString(), -1, 0.03F);
      if (pBeeInfo.hivePos == null) {
         renderTextOverMob(pBeeInfo.pos, i++, "No hive", -98404, 0.02F);
      } else {
         renderTextOverMob(pBeeInfo.pos, i++, "Hive: " + this.getPosDescription(pBeeInfo, pBeeInfo.hivePos), -256, 0.02F);
      }

      if (pBeeInfo.flowerPos == null) {
         renderTextOverMob(pBeeInfo.pos, i++, "No flower", -98404, 0.02F);
      } else {
         renderTextOverMob(pBeeInfo.pos, i++, "Flower: " + this.getPosDescription(pBeeInfo, pBeeInfo.flowerPos), -256, 0.02F);
      }

      for(String s : pBeeInfo.goals) {
         renderTextOverMob(pBeeInfo.pos, i++, s, -16711936, 0.02F);
      }

      if (flag) {
         this.renderPath(pBeeInfo);
      }

      if (pBeeInfo.travelTicks > 0) {
         int j = pBeeInfo.travelTicks < 600 ? -3355444 : -23296;
         renderTextOverMob(pBeeInfo.pos, i++, "Travelling: " + pBeeInfo.travelTicks + " ticks", j, 0.02F);
      }

   }

   private static void renderTextOverHive(String pText, BeeDebugRenderer.HiveInfo pHiveInfo, int pYScale, int pColor) {
      BlockPos blockpos = pHiveInfo.pos;
      renderTextOverPos(pText, blockpos, pYScale, pColor);
   }

   private static void renderTextOverPos(String pText, BlockPos pPos, int pYScale, int pColor) {
      double d0 = 1.3D;
      double d1 = 0.2D;
      double d2 = (double)pPos.getX() + 0.5D;
      double d3 = (double)pPos.getY() + 1.3D + (double)pYScale * 0.2D;
      double d4 = (double)pPos.getZ() + 0.5D;
      DebugRenderer.renderFloatingText(pText, d2, d3, d4, pColor, 0.02F, true, 0.0F, true);
   }

   private static void renderTextOverMob(Position pPos, int pYScale, String pText, int pColor, float pScale) {
      double d0 = 2.4D;
      double d1 = 0.25D;
      BlockPos blockpos = new BlockPos(pPos);
      double d2 = (double)blockpos.getX() + 0.5D;
      double d3 = pPos.y() + 2.4D + (double)pYScale * 0.25D;
      double d4 = (double)blockpos.getZ() + 0.5D;
      float f = 0.5F;
      DebugRenderer.renderFloatingText(pText, d2, d3, d4, pColor, pScale, false, 0.5F, true);
   }

   private Camera getCamera() {
      return this.minecraft.gameRenderer.getMainCamera();
   }

   private Set<String> getHiveMemberNames(BeeDebugRenderer.HiveInfo pHiveInfo) {
      return this.getHiveMembers(pHiveInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private String getPosDescription(BeeDebugRenderer.BeeInfo pBeeInfo, BlockPos pPos) {
      double d0 = Math.sqrt(pPos.distToCenterSqr(pBeeInfo.pos));
      double d1 = (double)Math.round(d0 * 10.0D) / 10.0D;
      return pPos.toShortString() + " (dist " + d1 + ")";
   }

   private boolean isBeeSelected(BeeDebugRenderer.BeeInfo pBeeInfo) {
      return Objects.equals(this.lastLookedAtUuid, pBeeInfo.uuid);
   }

   private boolean isPlayerCloseEnoughToMob(BeeDebugRenderer.BeeInfo pBeeInfo) {
      Player player = this.minecraft.player;
      BlockPos blockpos = new BlockPos(player.getX(), pBeeInfo.pos.y(), player.getZ());
      BlockPos blockpos1 = new BlockPos(pBeeInfo.pos);
      return blockpos.closerThan(blockpos1, 30.0D);
   }

   private Collection<UUID> getHiveMembers(BlockPos pPos) {
      return this.beeInfosPerEntity.values().stream().filter((p_113087_) -> {
         return p_113087_.hasHive(pPos);
      }).map(BeeDebugRenderer.BeeInfo::getUuid).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getGhostHives() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();

      for(BeeDebugRenderer.BeeInfo beedebugrenderer$beeinfo : this.beeInfosPerEntity.values()) {
         if (beedebugrenderer$beeinfo.hivePos != null && !this.hives.containsKey(beedebugrenderer$beeinfo.hivePos)) {
            map.computeIfAbsent(beedebugrenderer$beeinfo.hivePos, (p_113140_) -> {
               return Lists.newArrayList();
            }).add(beedebugrenderer$beeinfo.getName());
         }
      }

      return map;
   }

   private void updateLastLookedAtUuid() {
      DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent((p_113059_) -> {
         this.lastLookedAtUuid = p_113059_.getUUID();
      });
   }

   @OnlyIn(Dist.CLIENT)
   public static class BeeInfo {
      public final UUID uuid;
      public final int id;
      public final Position pos;
      @Nullable
      public final Path path;
      @Nullable
      public final BlockPos hivePos;
      @Nullable
      public final BlockPos flowerPos;
      public final int travelTicks;
      public final List<String> goals = Lists.newArrayList();
      public final Set<BlockPos> blacklistedHives = Sets.newHashSet();

      public BeeInfo(UUID pUuid, int pId, Position pPos, @Nullable Path pPath, @Nullable BlockPos pHivePos, @Nullable BlockPos pFlowerPos, int pTravelTicks) {
         this.uuid = pUuid;
         this.id = pId;
         this.pos = pPos;
         this.path = pPath;
         this.hivePos = pHivePos;
         this.flowerPos = pFlowerPos;
         this.travelTicks = pTravelTicks;
      }

      public boolean hasHive(BlockPos pPos) {
         return this.hivePos != null && this.hivePos.equals(pPos);
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getName() {
         return DebugEntityNameGenerator.getEntityName(this.uuid);
      }

      public String toString() {
         return this.getName();
      }

      public boolean hasFlower() {
         return this.flowerPos != null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class HiveInfo {
      public final BlockPos pos;
      public final String hiveType;
      public final int occupantCount;
      public final int honeyLevel;
      public final boolean sedated;
      public final long lastSeen;

      public HiveInfo(BlockPos pPos, String pHiveType, int pOccupantCount, int pHoneyLevel, boolean pSedated, long pLastSeen) {
         this.pos = pPos;
         this.hiveType = pHiveType;
         this.occupantCount = pOccupantCount;
         this.honeyLevel = pHoneyLevel;
         this.sedated = pSedated;
         this.lastSeen = pLastSeen;
      }
   }
}
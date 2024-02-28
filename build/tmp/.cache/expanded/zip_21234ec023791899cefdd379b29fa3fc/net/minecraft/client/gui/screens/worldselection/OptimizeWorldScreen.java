package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class OptimizeWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Object2IntMap<ResourceKey<Level>> DIMENSION_COLORS = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), (p_101324_) -> {
      p_101324_.put(Level.OVERWORLD, -13408734);
      p_101324_.put(Level.NETHER, -10075085);
      p_101324_.put(Level.END, -8943531);
      p_101324_.defaultReturnValue(-2236963);
   });
   private final BooleanConsumer callback;
   private final WorldUpgrader upgrader;

   @Nullable
   public static OptimizeWorldScreen create(Minecraft pMinecraft, BooleanConsumer pCallback, DataFixer pDataFixer, LevelStorageSource.LevelStorageAccess pLevelStorage, boolean pEraseCache) {
      try {
         WorldStem worldstem = pMinecraft.createWorldOpenFlows().loadWorldStem(pLevelStorage, false);

         OptimizeWorldScreen optimizeworldscreen;
         try {
            WorldData worlddata = worldstem.worldData();
            pLevelStorage.saveDataTag(worldstem.registryAccess(), worlddata);
            optimizeworldscreen = new OptimizeWorldScreen(pCallback, pDataFixer, pLevelStorage, worlddata.getLevelSettings(), pEraseCache, worlddata.worldGenSettings());
         } catch (Throwable throwable1) {
            if (worldstem != null) {
               try {
                  worldstem.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (worldstem != null) {
            worldstem.close();
         }

         return optimizeworldscreen;
      } catch (Exception exception) {
         LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)exception);
         return null;
      }
   }

   private OptimizeWorldScreen(BooleanConsumer pCallback, DataFixer pDataFixer, LevelStorageSource.LevelStorageAccess pLevelStorage, LevelSettings pLevelSettings, boolean pEraseCache, WorldGenSettings pLevels) {
      super(Component.translatable("optimizeWorld.title", pLevelSettings.levelName()));
      this.callback = pCallback;
      this.upgrader = new WorldUpgrader(pLevelStorage, pDataFixer, pLevels, pEraseCache);
   }

   protected void init() {
      super.init();
      this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 150, 200, 20, CommonComponents.GUI_CANCEL, (p_101322_) -> {
         this.upgrader.cancel();
         this.callback.accept(false);
      }));
   }

   public void tick() {
      if (this.upgrader.isFinished()) {
         this.callback.accept(true);
      }

   }

   public void onClose() {
      this.callback.accept(false);
   }

   public void removed() {
      this.upgrader.cancel();
   }

   public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pPoseStack);
      drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 20, 16777215);
      int i = this.width / 2 - 150;
      int j = this.width / 2 + 150;
      int k = this.height / 4 + 100;
      int l = k + 10;
      drawCenteredString(pPoseStack, this.font, this.upgrader.getStatus(), this.width / 2, k - 9 - 2, 10526880);
      if (this.upgrader.getTotalChunks() > 0) {
         fill(pPoseStack, i - 1, k - 1, j + 1, l + 1, -16777216);
         drawString(pPoseStack, this.font, Component.translatable("optimizeWorld.info.converted", this.upgrader.getConverted()), i, 40, 10526880);
         drawString(pPoseStack, this.font, Component.translatable("optimizeWorld.info.skipped", this.upgrader.getSkipped()), i, 40 + 9 + 3, 10526880);
         drawString(pPoseStack, this.font, Component.translatable("optimizeWorld.info.total", this.upgrader.getTotalChunks()), i, 40 + (9 + 3) * 2, 10526880);
         int i1 = 0;

         for(ResourceKey<Level> resourcekey : this.upgrader.levels()) {
            int j1 = Mth.floor(this.upgrader.dimensionProgress(resourcekey) * (float)(j - i));
            fill(pPoseStack, i + i1, k, i + i1 + j1, l, DIMENSION_COLORS.getInt(resourcekey));
            i1 += j1;
         }

         int k1 = this.upgrader.getConverted() + this.upgrader.getSkipped();
         drawCenteredString(pPoseStack, this.font, k1 + " / " + this.upgrader.getTotalChunks(), this.width / 2, k + 2 * 9 + 2, 10526880);
         drawCenteredString(pPoseStack, this.font, Mth.floor(this.upgrader.getProgress() * 100.0F) + "%", this.width / 2, k + (l - k) / 2 - 9 / 2, 10526880);
      }

      super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
   }
}
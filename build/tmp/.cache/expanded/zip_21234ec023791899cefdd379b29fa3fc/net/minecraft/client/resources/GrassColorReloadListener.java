package net.minecraft.client.resources;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GrassColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrassColorReloadListener extends SimplePreparableReloadListener<int[]> {
   private static final ResourceLocation LOCATION = new ResourceLocation("textures/colormap/grass.png");

   /**
    * Performs any reloading that can be done off-thread, such as file IO
    */
   protected int[] prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      try {
         return LegacyStuffWrapper.getPixels(pResourceManager, LOCATION);
      } catch (IOException ioexception) {
         throw new IllegalStateException("Failed to load grass color texture", ioexception);
      }
   }

   protected void apply(int[] pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      GrassColor.init(pObject);
   }
}
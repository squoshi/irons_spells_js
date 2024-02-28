package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public record FrogVariant(ResourceLocation texture) {
   public static final FrogVariant TEMPERATE = register("temperate", "textures/entity/frog/temperate_frog.png");
   public static final FrogVariant WARM = register("warm", "textures/entity/frog/warm_frog.png");
   public static final FrogVariant COLD = register("cold", "textures/entity/frog/cold_frog.png");

   private static FrogVariant register(String pName, String pTextureLocation) {
      return Registry.register(Registry.FROG_VARIANT, pName, new FrogVariant(new ResourceLocation(pTextureLocation)));
   }
}
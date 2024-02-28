package net.minecraft.data.models.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModelTemplate {
   private final Optional<ResourceLocation> model;
   private final Set<TextureSlot> requiredSlots;
   private final Optional<String> suffix;

   public ModelTemplate(Optional<ResourceLocation> pModel, Optional<String> pSuffix, TextureSlot... pRequiredSlots) {
      this.model = pModel;
      this.suffix = pSuffix;
      this.requiredSlots = ImmutableSet.copyOf(pRequiredSlots);
   }

   public ResourceLocation create(Block pModelBlock, TextureMapping pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.create(ModelLocationUtils.getModelLocation(pModelBlock, this.suffix.orElse("")), pTextureMapping, pModelOutput);
   }

   public ResourceLocation createWithSuffix(Block pModelBlock, String pModelLocationSuffix, TextureMapping pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.create(ModelLocationUtils.getModelLocation(pModelBlock, pModelLocationSuffix + (String)this.suffix.orElse("")), pTextureMapping, pModelOutput);
   }

   public ResourceLocation createWithOverride(Block pModelBlock, String pModelLocationSuffix, TextureMapping pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.create(ModelLocationUtils.getModelLocation(pModelBlock, pModelLocationSuffix), pTextureMapping, pModelOutput);
   }

   public ResourceLocation create(ResourceLocation pModelLocation, TextureMapping pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      Map<TextureSlot, ResourceLocation> map = this.createMap(pTextureMapping);
      pModelOutput.accept(pModelLocation, () -> {
         JsonObject jsonobject = new JsonObject();
         this.model.ifPresent((p_176461_) -> {
            jsonobject.addProperty("parent", p_176461_.toString());
         });
         if (!map.isEmpty()) {
            JsonObject jsonobject1 = new JsonObject();
            map.forEach((p_176457_, p_176458_) -> {
               jsonobject1.addProperty(p_176457_.getId(), p_176458_.toString());
            });
            jsonobject.add("textures", jsonobject1);
         }

         return jsonobject;
      });
      return pModelLocation;
   }

   private Map<TextureSlot, ResourceLocation> createMap(TextureMapping pTextureMapping) {
      return Streams.concat(this.requiredSlots.stream(), pTextureMapping.getForced()).collect(ImmutableMap.toImmutableMap(Function.identity(), pTextureMapping::get));
   }
}
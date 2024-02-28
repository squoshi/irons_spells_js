package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerProfessionLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T, M> {
   private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), (p_117657_) -> {
      p_117657_.put(1, new ResourceLocation("stone"));
      p_117657_.put(2, new ResourceLocation("iron"));
      p_117657_.put(3, new ResourceLocation("gold"));
      p_117657_.put(4, new ResourceLocation("emerald"));
      p_117657_.put(5, new ResourceLocation("diamond"));
   });
   private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
   private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
   private final ResourceManager resourceManager;
   private final String path;

   public VillagerProfessionLayer(RenderLayerParent<T, M> pRenderer, ResourceManager pResourceManager, String pPath) {
      super(pRenderer);
      this.resourceManager = pResourceManager;
      this.path = pPath;
   }

   public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isInvisible()) {
         VillagerData villagerdata = pLivingEntity.getVillagerData();
         VillagerType villagertype = villagerdata.getType();
         VillagerProfession villagerprofession = villagerdata.getProfession();
         VillagerMetaDataSection.Hat villagermetadatasection$hat = this.getHatData(this.typeHatCache, "type", Registry.VILLAGER_TYPE, villagertype);
         VillagerMetaDataSection.Hat villagermetadatasection$hat1 = this.getHatData(this.professionHatCache, "profession", Registry.VILLAGER_PROFESSION, villagerprofession);
         M m = this.getParentModel();
         m.hatVisible(villagermetadatasection$hat1 == VillagerMetaDataSection.Hat.NONE || villagermetadatasection$hat1 == VillagerMetaDataSection.Hat.PARTIAL && villagermetadatasection$hat != VillagerMetaDataSection.Hat.FULL);
         ResourceLocation resourcelocation = this.getResourceLocation("type", Registry.VILLAGER_TYPE.getKey(villagertype));
         renderColoredCutoutModel(m, resourcelocation, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, 1.0F, 1.0F, 1.0F);
         m.hatVisible(true);
         if (villagerprofession != VillagerProfession.NONE && !pLivingEntity.isBaby()) {
            ResourceLocation resourcelocation1 = this.getResourceLocation("profession", Registry.VILLAGER_PROFESSION.getKey(villagerprofession));
            renderColoredCutoutModel(m, resourcelocation1, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, 1.0F, 1.0F, 1.0F);
            if (villagerprofession != VillagerProfession.NITWIT) {
               ResourceLocation resourcelocation2 = this.getResourceLocation("profession_level", LEVEL_LOCATIONS.get(Mth.clamp(villagerdata.getLevel(), 1, LEVEL_LOCATIONS.size())));
               renderColoredCutoutModel(m, resourcelocation2, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, 1.0F, 1.0F, 1.0F);
            }
         }

      }
   }

   private ResourceLocation getResourceLocation(String p_117669_, ResourceLocation p_117670_) {
      return new ResourceLocation(p_117670_.getNamespace(), "textures/entity/" + this.path + "/" + p_117669_ + "/" + p_117670_.getPath() + ".png");
   }

   public <K> VillagerMetaDataSection.Hat getHatData(Object2ObjectMap<K, VillagerMetaDataSection.Hat> p_117659_, String p_117660_, DefaultedRegistry<K> p_117661_, K p_117662_) {
      return p_117659_.computeIfAbsent(p_117662_, (p_234880_) -> {
         return this.resourceManager.getResource(this.getResourceLocation(p_117660_, p_117661_.getKey(p_117662_))).flatMap((p_234875_) -> {
            try {
               return p_234875_.metadata().getSection(VillagerMetaDataSection.SERIALIZER).map(VillagerMetaDataSection::getHat);
            } catch (IOException ioexception) {
               return Optional.empty();
            }
         }).orElse(VillagerMetaDataSection.Hat.NONE);
      });
   }
}
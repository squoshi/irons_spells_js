package com.squoshi.irons_spells_js;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import io.redspace.ironsspellbooks.render.StaffArmPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = IronsSpellsJSMod.MODID, value = Dist.CLIENT)
public class IronsSpellsJSModClient {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> RegistryObjectStorage.ITEM.objects.forEach((id, builderBase) -> {
			if (builderBase.get() instanceof SpellBook) {
				CuriosRendererRegistry.register(builderBase.get(), SpellBookCurioRenderer::new);
			}
		}));
	}

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		Item[] registeredStaves = RegistryObjectStorage.ITEM.objects.values().stream()
			.map(BuilderBase::get).filter(item -> item instanceof StaffItem)
			.toArray(Item[]::new);
		if (registeredStaves.length == 0) {
			return;
		}
		event.registerItem(new IClientItemExtensions() {
			@Nullable
			@Override
			public HumanoidModel.ArmPose getArmPose(@NotNull LivingEntity entityLiving, @NotNull InteractionHand hand, @NotNull ItemStack itemStack) {
				return StaffArmPose.STAFF_ARM_POSE.getValue();
			}
		}, registeredStaves);
	}
}

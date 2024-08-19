package com.squoshi.irons_spells_js.mixin;

import com.squoshi.irons_spells_js.spell.MagicEntityKJS;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements MagicEntityKJS {
	@Override
	public LivingEntity irons_spells_js$self() {
		return (LivingEntity) (Object) this;
	}
}

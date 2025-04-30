package com.squoshi.irons_spells_js.item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class CustomSpellBook extends SpellBook {
	List<SpellData> spellData = new ArrayList<>();
	SpellDataRegistryHolder[] spellDataRegistryHolders;
	boolean unique;
	Holder<AbstractSpell> affinitySpell;

	public CustomSpellBook(Builder b) {
		super(b.maxSpellSlots, b.createItemProperties().stacksTo(1));
		this.spellDataRegistryHolders = b.spellHolders.toArray(new SpellDataRegistryHolder[0]);
		this.unique = b.unique;
		withSpellbookAttributes(b.defaultModifiers.toArray(new AttributeContainer[0]));
		this.affinitySpell = b.affinitySpell;
	}

	@Override
	public boolean isUnique() {
		return unique;
	}

	private List<SpellData> getSpells() {
		if (spellDataRegistryHolders != null) {
			spellData.addAll(Arrays.stream(spellDataRegistryHolders).map(SpellDataRegistryHolder::getSpellData).toList());
			spellDataRegistryHolders = null;
		}
		return spellData;
	}

	@Override
	@NotNull
	public Component getName(@NotNull ItemStack stack) {
		if (isUnique() && stack.get(ComponentRegistry.SPELL_CONTAINER) instanceof ISpellContainer container && container.isImproved()) {
			return Component.translatable("tooltip.irons_spellbooks.improved_format", super.getName(stack));
		}
		return super.getName(stack);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack itemStack, TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
		super.appendHoverText(itemStack, context, lines, flag);
		var affinityData = AffinityData.getAffinityData(itemStack);
		if (!affinityData.affinityData().isEmpty()) {
			int i = TooltipsUtils.indexOfComponent(lines, "tooltip.irons_spellbooks.spellbook_spell_count");
			lines.addAll(i < 0 ? lines.size() : i + 1, affinityData.getDescriptionComponent());
		}
	}

	@Override
	public void initializeSpellContainer(ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}

		if (!ISpellContainer.isSpellContainer(itemStack)) {
			var spellContainer = ISpellContainer.create(getMaxSpellSlots(), true, true).mutableCopy();
			var spells = getSpells();
			if (!spells.isEmpty()) {
				spells.forEach(spellSlot -> spellContainer.addSpell(spellSlot.getSpell(), spellSlot.getLevel(), true));
			}
			itemStack.set(ComponentRegistry.SPELL_CONTAINER, spellContainer.toImmutable());
		}
		if (affinitySpell != null) {
			AffinityData.setAffinityData(itemStack, affinitySpell.value());
		}
	}

	@ReturnsSelf
	public static class Builder extends ItemBuilder {
		public transient List<SpellDataRegistryHolder> spellHolders = new ArrayList<>();
		public transient int maxSpellSlots = 1;
		public transient List<AttributeContainer> defaultModifiers = new ArrayList<>();
		public transient boolean unique = false;
		public transient Holder<AbstractSpell> affinitySpell;

		public Builder(ResourceLocation i) {
			super(i);
			tag(new ResourceLocation[]{ResourceLocation.parse("curios:spellbook")});
		}

		@Info("""
			Adds a default attribute to the item. Can be used multiple times.
			The modifier operation can be either `ADD_MULTIPLIED_BASE`, `ADD_MULTIPLIED_TOTAL` or `ADD_VALUE`.
			""")
		public Builder addAttribute(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation) {
			defaultModifiers.add(new AttributeContainer(attribute, value, operation));
			return this;
		}

		@Info("""
			Sets the maximum amount of spell slots the spell book can have.
			""")
		public Builder setMaxSpellSlots(int maxSpellSlots) {
			this.maxSpellSlots = maxSpellSlots;
			return this;
		}

		@Info("""
			Adds a default spell to the item. Can be used multiple times. It takes a spell ID (or a spell object) and the spell level.
			This will turn into Unique Spellbook.
			""")
		public Builder addSpell(Holder<AbstractSpell> spell, int spellLevel) {
			this.spellHolders.add(new SpellDataRegistryHolder(spell::value, spellLevel));
			return this;
		}

		@Info("""
			Sets an affinity that will make this spell a +1 level boost.
			""")
		public Builder setAffinitySpell(Holder<AbstractSpell> affinitySpell) {
			this.affinitySpell = affinitySpell;
			return this;
		}

		@Override
		public SpellBook createObject() {
			maxSpellSlots = Math.max(spellHolders.size(), maxSpellSlots);
			if (!spellHolders.isEmpty()) {
				unique = true;
			}
			return new CustomSpellBook(this);
		}

	}

}



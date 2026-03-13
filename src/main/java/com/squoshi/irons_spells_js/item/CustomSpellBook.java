package com.squoshi.irons_spells_js.item;

import com.squoshi.irons_spells_js.util.ISSKJSUtils;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class CustomSpellBook extends SpellBook {
    List<SpellData> spellData = new ArrayList<>();
    SpellHolder[] spellHolders;
    boolean unique;
    ResourceLocation affinitySpellId;

    public CustomSpellBook(Builder b) {
        super(b.maxSpellSlots, b.createItemProperties().stacksTo(1).rarity(b.rarity));
        this.spellHolders = b.pendingSpells.toArray(new SpellHolder[0]);
        this.unique = b.unique;
        withSpellbookAttributes(b.defaultModifiers.toArray(new AttributeContainer[0]));
        this.affinitySpellId = b.affinitySpellId;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    private List<SpellData> getSpells() {
        if (spellHolders != null) {
            spellData.addAll(Arrays.stream(spellHolders)
                    .map(h -> new SpellData(SpellRegistry.getSpell(h.spell()), h.spellLevel()))
                    .toList());
            spellHolders = null;
        }
        return spellData;
    }

    @Override
    @NotNull
    public Component getName(@NotNull ItemStack stack) {
        var container = ISpellContainer.get(stack);
        if (isUnique() && container.isImproved()) {
            return Component.translatable("tooltip.irons_spellbooks.improved_format", super.getName(stack));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Level context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
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
            ISpellContainer.set(itemStack, spellContainer.toImmutable());
        }
        if (affinitySpellId != null) {
            AffinityData.setAffinityData(itemStack, SpellRegistry.getSpell(affinitySpellId));
        }
    }

    public static class Builder extends ItemBuilder {
        public transient List<SpellHolder> pendingSpells = new ArrayList<>();
        public transient int maxSpellSlots = 1;
        public transient List<AttributeContainer> defaultModifiers = new ArrayList<>();
        public transient boolean unique = false;
        public transient ResourceLocation affinitySpellId;
        public transient Rarity itemRarity = Rarity.UNCOMMON;
        public transient SpellRarity spellRarity = SpellRarity.LEGENDARY;
        public Builder(ResourceLocation i) {
            super(i);
            var tags = new ResourceLocation[]{ResourceLocation.parse("curios:spellbook")};
            for (var t : tags) {
                tag(t);
            }
        }

        @Info("""
			Adds a default attribute to the item. Can be used multiple times.
			The modifier operation can be either `ADDITION`, `MULTIPLY_BASE` or `MULTIPLY_TOTAL`.
			""")
        public Builder addAttribute(String attribute, double value, AttributeModifier.Operation operation) {
            var rl = ResourceLocation.parse(attribute);
            defaultModifiers.add(new AttributeContainer(() -> BuiltInRegistries.ATTRIBUTE.get(rl), value, operation));
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
        public Builder addSpell(ISSKJSUtils.SpellHolder spell, int spellLevel) {
            this.pendingSpells.add(new SpellHolder(spell.getLocation(), spellLevel));
            return this;
        }

        @Info("""
			Sets an affinity that will make this spell a +1 level boost.
			""")
        public Builder setAffinitySpell(String affinitySpell) {
            this.affinitySpellId = ResourceLocation.parse(affinitySpell);
            return this;
        }

        @Override
        public SpellBook createObject() {
            maxSpellSlots = Math.max(pendingSpells.size(), maxSpellSlots);
            if (!pendingSpells.isEmpty()) {
                unique = true;
            }
            return new CustomSpellBook(this);
        }
    }
    public record AttributeHolder(ResourceLocation attribute, AttributeModifier modifier) {
    }

    public record SpellHolder(ResourceLocation spell, int spellLevel) {
    }
}



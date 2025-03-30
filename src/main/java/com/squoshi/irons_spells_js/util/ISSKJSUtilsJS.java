package com.squoshi.irons_spells_js.util;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ISSKJSUtilsJS {
    public static InteractionResultHolder<ItemStack> rightClickCast(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        SpellSelectionManager spellSelectionManager = new SpellSelectionManager(player);
        SpellSelectionManager.SelectionOption selectionOption = spellSelectionManager.getSelection();
        if (selectionOption == null || selectionOption.spellData.equals(SpellData.EMPTY)) {
            return InteractionResultHolder.pass(itemStack);
        }
        SpellData spellData = selectionOption.spellData;
        int spellLevel = spellData.getSpell().getLevelFor(spellData.getLevel(), player);
        if (level.isClientSide()) {
            if (ClientMagicData.isCasting()) {
                return InteractionResultHolder.consume(itemStack);
            } else if (ClientMagicData.getPlayerMana() < spellData.getSpell().getManaCost(spellLevel)
                    || ClientMagicData.getCooldowns().isOnCooldown(spellData.getSpell())
                    || !ClientMagicData.getSyncedSpellData(player).isSpellLearned(spellData.getSpell())) {
                return InteractionResultHolder.pass(itemStack);
            } else {
                return InteractionResultHolder.consume(itemStack);
            }
        }

        var castingSlot = hand.ordinal() == 0 ? SpellSelectionManager.MAINHAND : SpellSelectionManager.OFFHAND;

        if (spellData.getSpell().attemptInitiateCast(itemStack, spellLevel, level, player, selectionOption.getCastSource(), true, castingSlot)) {
            return InteractionResultHolder.consume(itemStack);
        } else {
            return InteractionResultHolder.fail(itemStack);
        }
    }
}

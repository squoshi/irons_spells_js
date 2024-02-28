package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {
   private static final String TAG_INSTRUMENT = "instrument";
   private TagKey<Instrument> instruments;

   public InstrumentItem(Item.Properties pProperties, TagKey<Instrument> pInstruments) {
      super(pProperties);
      this.instruments = pInstruments;
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
      super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
      Optional<ResourceKey<Instrument>> optional = this.getInstrument(pStack).flatMap(Holder::unwrapKey);
      if (optional.isPresent()) {
         MutableComponent mutablecomponent = Component.translatable(Util.makeDescriptionId("instrument", optional.get().location()));
         pTooltipComponents.add(mutablecomponent.withStyle(ChatFormatting.GRAY));
      }

   }

   public static ItemStack create(Item pItem, Holder<Instrument> pInstrument) {
      ItemStack itemstack = new ItemStack(pItem);
      setSoundVariantId(itemstack, pInstrument);
      return itemstack;
   }

   public static void setRandom(ItemStack pStack, TagKey<Instrument> pInstrumentTag, RandomSource pRandom) {
      Optional<Holder<Instrument>> optional = Registry.INSTRUMENT.getTag(pInstrumentTag).flatMap((p_220103_) -> {
         return p_220103_.getRandomElement(pRandom);
      });
      if (optional.isPresent()) {
         setSoundVariantId(pStack, optional.get());
      }

   }

   private static void setSoundVariantId(ItemStack pStack, Holder<Instrument> pSoundVariantId) {
      CompoundTag compoundtag = pStack.getOrCreateTag();
      compoundtag.putString("instrument", pSoundVariantId.unwrapKey().orElseThrow(() -> {
         return new IllegalStateException("Invalid instrument");
      }).location().toString());
   }

   /**
    * Adds some {@code ItemStacks} to the given list for display in the creative tab.
    */
   public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
      if (this.allowedIn(pCategory)) {
         for(Holder<Instrument> holder : Registry.INSTRUMENT.getTagOrEmpty(this.instruments)) {
            pItems.add(create(Items.GOAT_HORN, holder));
         }
      }

   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
      Optional<Holder<Instrument>> optional = this.getInstrument(itemstack);
      if (optional.isPresent()) {
         Instrument instrument = optional.get().value();
         pPlayer.startUsingItem(pUsedHand);
         play(pLevel, pPlayer, instrument);
         pPlayer.getCooldowns().addCooldown(this, instrument.useDuration());
         return InteractionResultHolder.consume(itemstack);
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      Optional<Holder<Instrument>> optional = this.getInstrument(pStack);
      return optional.isPresent() ? optional.get().value().useDuration() : 0;
   }

   private Optional<Holder<Instrument>> getInstrument(ItemStack pStack) {
      CompoundTag compoundtag = pStack.getTag();
      if (compoundtag != null) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(compoundtag.getString("instrument"));
         if (resourcelocation != null) {
            return Registry.INSTRUMENT.getHolder(ResourceKey.create(Registry.INSTRUMENT_REGISTRY, resourcelocation));
         }
      }

      Iterator<Holder<Instrument>> iterator = Registry.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();
      return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.TOOT_HORN;
   }

   private static void play(Level pLevel, Player pPlayer, Instrument pInstrument) {
      SoundEvent soundevent = pInstrument.soundEvent();
      float f = pInstrument.range() / 16.0F;
      pLevel.playSound(pPlayer, pPlayer, soundevent, SoundSource.RECORDS, f, 1.0F);
      pLevel.gameEvent(GameEvent.INSTRUMENT_PLAY, pPlayer.position(), GameEvent.Context.of(pPlayer));
   }
}
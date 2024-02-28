package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatTrustLevel {
   SECURE,
   MODIFIED,
   FILTERED,
   NOT_SECURE,
   BROKEN_CHAIN;

   public static ChatTrustLevel evaluate(PlayerChatMessage pChatMessage, Component pDecoratedServerContent, @Nullable PlayerInfo pPlayerInfo, Instant pTimestamp) {
      if (pPlayerInfo == null) {
         return NOT_SECURE;
      } else {
         SignedMessageValidator.State signedmessagevalidator$state = pPlayerInfo.getMessageValidator().validateMessage(pChatMessage);
         if (signedmessagevalidator$state == SignedMessageValidator.State.BROKEN_CHAIN) {
            return BROKEN_CHAIN;
         } else if (signedmessagevalidator$state == SignedMessageValidator.State.NOT_SECURE) {
            return NOT_SECURE;
         } else if (pChatMessage.hasExpiredClient(pTimestamp)) {
            return NOT_SECURE;
         } else if (!pChatMessage.filterMask().isEmpty()) {
            return FILTERED;
         } else if (pChatMessage.unsignedContent().isPresent()) {
            return MODIFIED;
         } else {
            return !pDecoratedServerContent.contains(pChatMessage.signedContent().decorated()) ? MODIFIED : SECURE;
         }
      }
   }

   public boolean isNotSecure() {
      return this == NOT_SECURE || this == BROKEN_CHAIN;
   }

   @Nullable
   public GuiMessageTag createTag(PlayerChatMessage pChatMessage) {
      GuiMessageTag guimessagetag;
      switch (this) {
         case MODIFIED:
            guimessagetag = GuiMessageTag.chatModified(pChatMessage.signedContent().plain());
            break;
         case FILTERED:
            guimessagetag = GuiMessageTag.chatFiltered();
            break;
         case NOT_SECURE:
            guimessagetag = GuiMessageTag.chatNotSecure();
            break;
         default:
            guimessagetag = null;
      }

      return guimessagetag;
   }
}
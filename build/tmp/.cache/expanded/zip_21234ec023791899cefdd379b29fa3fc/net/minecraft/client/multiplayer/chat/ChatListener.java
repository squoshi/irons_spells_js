package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatListener {
   private static final Component CHAT_VALIDATION_FAILED_ERROR = Component.translatable("multiplayer.disconnect.chat_validation_failed");
   private final Minecraft minecraft;
   private final Deque<ChatListener.Message> delayedMessageQueue = Queues.newArrayDeque();
   private long messageDelay;
   private long previousMessageTime;

   public ChatListener(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void tick() {
      if (this.messageDelay != 0L) {
         if (Util.getMillis() >= this.previousMessageTime + this.messageDelay) {
            for(ChatListener.Message chatlistener$message = this.delayedMessageQueue.poll(); chatlistener$message != null && !chatlistener$message.accept(); chatlistener$message = this.delayedMessageQueue.poll()) {
            }
         }

      }
   }

   public void setMessageDelay(double pDelaySeconds) {
      long i = (long)(pDelaySeconds * 1000.0D);
      if (i == 0L && this.messageDelay > 0L) {
         this.delayedMessageQueue.forEach(ChatListener.Message::accept);
         this.delayedMessageQueue.clear();
      }

      this.messageDelay = i;
   }

   public void acceptNextDelayedMessage() {
      this.delayedMessageQueue.remove().accept();
   }

   public long queueSize() {
      return this.delayedMessageQueue.stream().filter(ChatListener.Message::isVisible).count();
   }

   public void clearQueue() {
      this.delayedMessageQueue.forEach((p_242052_) -> {
         p_242052_.remove();
         p_242052_.accept();
      });
      this.delayedMessageQueue.clear();
   }

   public boolean removeFromDelayedMessageQueue(MessageSignature pSignature) {
      for(ChatListener.Message chatlistener$message : this.delayedMessageQueue) {
         if (chatlistener$message.removeIfSignatureMatches(pSignature)) {
            return true;
         }
      }

      return false;
   }

   private boolean willDelayMessages() {
      return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
   }

   private void handleMessage(ChatListener.Message pMessage) {
      if (this.willDelayMessages()) {
         this.delayedMessageQueue.add(pMessage);
      } else {
         pMessage.accept();
      }

   }

   /**
    * Called whenever a player chat message is received from the server.
    */
   public void handleChatMessage(final PlayerChatMessage pChatMessage, final ChatType.Bound pBoundChatType) {
      final boolean flag = this.minecraft.options.onlyShowSecureChat().get();
      final PlayerChatMessage playerchatmessage = flag ? pChatMessage.removeUnsignedContent() : pChatMessage;
      final Component component = pBoundChatType.decorate(playerchatmessage.serverContent());
      MessageSigner messagesigner = pChatMessage.signer();
      if (!messagesigner.isSystem()) {
         final PlayerInfo playerinfo = this.resolveSenderPlayer(messagesigner.profileId());
         final Instant instant = Instant.now();
         this.handleMessage(new ChatListener.Message() {
            private boolean removed;

            public boolean accept() {
               if (this.removed) {
                  byte[] abyte = pChatMessage.signedBody().hash().asBytes();
                  ChatListener.this.processPlayerChatHeader(pChatMessage.signedHeader(), pChatMessage.headerSignature(), abyte);
                  return false;
               } else {
                  return ChatListener.this.processPlayerChatMessage(pBoundChatType, pChatMessage, component, playerinfo, flag, instant);
               }
            }

            public boolean removeIfSignatureMatches(MessageSignature p_242335_) {
               if (pChatMessage.headerSignature().equals(p_242335_)) {
                  this.removed = true;
                  return true;
               } else {
                  return false;
               }
            }

            public void remove() {
               this.removed = true;
            }

            public boolean isVisible() {
               return !this.removed;
            }
         });
      } else {
         this.handleMessage(new ChatListener.Message() {
            public boolean accept() {
               return ChatListener.this.processNonPlayerChatMessage(pBoundChatType, playerchatmessage, component);
            }

            public boolean isVisible() {
               return true;
            }
         });
      }

   }

   public void handleChatHeader(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest) {
      this.handleMessage(() -> {
         return this.processPlayerChatHeader(pHeader, pHeaderSignature, pBodyDigest);
      });
   }

   boolean processPlayerChatMessage(ChatType.Bound pBoundChatType, PlayerChatMessage pChatMessage, Component pDecoratedServerContent, @Nullable PlayerInfo pPlayerInfo, boolean pOnlyShowSecureChat, Instant pTimestamp) {
      boolean flag = this.showMessageToPlayer(pBoundChatType, pChatMessage, pDecoratedServerContent, pPlayerInfo, pOnlyShowSecureChat, pTimestamp);
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null) {
         clientpacketlistener.markMessageAsProcessed(pChatMessage, flag);
      }

      return flag;
   }

   private boolean showMessageToPlayer(ChatType.Bound pBoundChatType, PlayerChatMessage pChatMessage, Component pDecoratedServerContent, @Nullable PlayerInfo pPlayerInfo, boolean pOnlyShowSecureChat, Instant pTimestamp) {
      ChatTrustLevel chattrustlevel = this.evaluateTrustLevel(pChatMessage, pDecoratedServerContent, pPlayerInfo, pTimestamp);
      if (chattrustlevel == ChatTrustLevel.BROKEN_CHAIN) {
         this.onChatChainBroken();
         return true;
      } else if (pOnlyShowSecureChat && chattrustlevel.isNotSecure()) {
         return false;
      } else if (!this.minecraft.isBlocked(pChatMessage.signer().profileId()) && !pChatMessage.isFullyFiltered()) {
         GuiMessageTag guimessagetag = chattrustlevel.createTag(pChatMessage);
         MessageSignature messagesignature = pChatMessage.headerSignature();
         FilterMask filtermask = pChatMessage.filterMask();
         if (filtermask.isEmpty()) {
            Component forgeComponent = net.minecraftforge.client.ForgeHooksClient.onClientChat(pBoundChatType, pDecoratedServerContent, pChatMessage, pChatMessage.signer());
            if (forgeComponent == null) return false;
            this.minecraft.gui.getChat().addMessage(forgeComponent, messagesignature, guimessagetag);
            this.narrateChatMessage(pBoundChatType, pChatMessage.serverContent());
         } else {
            Component component = filtermask.apply(pChatMessage.signedContent());
            if (component != null) {
               Component forgeComponent = net.minecraftforge.client.ForgeHooksClient.onClientChat(pBoundChatType, pBoundChatType.decorate(component), pChatMessage, pChatMessage.signer());
               if (forgeComponent == null) return false;
               this.minecraft.gui.getChat().addMessage(forgeComponent, messagesignature, guimessagetag);
               this.narrateChatMessage(pBoundChatType, component);
            }
         }

         this.logPlayerMessage(pChatMessage, pBoundChatType, pPlayerInfo, chattrustlevel);
         this.previousMessageTime = Util.getMillis();
         return true;
      } else {
         return false;
      }
   }

   boolean processNonPlayerChatMessage(ChatType.Bound pBoundChatType, PlayerChatMessage pChatMessage, Component pDecoratedServerContent) {
      Component forgeComponent = net.minecraftforge.client.ForgeHooksClient.onClientChat(pBoundChatType, pDecoratedServerContent, pChatMessage, pChatMessage.signer());
      if (forgeComponent == null) return false;
      this.minecraft.gui.getChat().addMessage(forgeComponent);
      this.narrateChatMessage(pBoundChatType, pChatMessage.serverContent());
      this.logSystemMessage(pDecoratedServerContent, pChatMessage.timeStamp());
      this.previousMessageTime = Util.getMillis();
      return true;
   }

   boolean processPlayerChatHeader(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest) {
      PlayerInfo playerinfo = this.resolveSenderPlayer(pHeader.sender());
      if (playerinfo != null) {
         SignedMessageValidator.State signedmessagevalidator$state = playerinfo.getMessageValidator().validateHeader(pHeader, pHeaderSignature, pBodyDigest);
         if (signedmessagevalidator$state == SignedMessageValidator.State.BROKEN_CHAIN) {
            this.onChatChainBroken();
            return true;
         }
      }

      this.logPlayerHeader(pHeader, pHeaderSignature, pBodyDigest);
      return false;
   }

   private void onChatChainBroken() {
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null) {
         clientpacketlistener.getConnection().disconnect(CHAT_VALIDATION_FAILED_ERROR);
      }

   }

   private void narrateChatMessage(ChatType.Bound pBoundChatType, Component pMessage) {
      this.minecraft.getNarrator().sayChatNow(() -> {
         return pBoundChatType.decorateNarration(pMessage);
      });
   }

   private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage pChatMessage, Component pDecoratedServerContent, @Nullable PlayerInfo pPlayerInfo, Instant pTimestamp) {
      return this.isSenderLocalPlayer(pChatMessage.signer().profileId()) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(pChatMessage, pDecoratedServerContent, pPlayerInfo, pTimestamp);
   }

   private void logPlayerMessage(PlayerChatMessage pMessage, ChatType.Bound pBoundChatType, @Nullable PlayerInfo pPlayerInfo, ChatTrustLevel pTrustLevel) {
      GameProfile gameprofile;
      if (pPlayerInfo != null) {
         gameprofile = pPlayerInfo.getProfile();
      } else {
         gameprofile = new GameProfile(pMessage.signer().profileId(), pBoundChatType.name().getString());
      }

      ChatLog chatlog = this.minecraft.getReportingContext().chatLog();
      chatlog.push(LoggedChatMessage.player(gameprofile, pBoundChatType.name(), pMessage, pTrustLevel));
   }

   private void logSystemMessage(Component pMessage, Instant pTimestamp) {
      ChatLog chatlog = this.minecraft.getReportingContext().chatLog();
      chatlog.push(LoggedChatMessage.system(pMessage, pTimestamp));
   }

   private void logPlayerHeader(SignedMessageHeader pHeader, MessageSignature pHeaderSignature, byte[] pBodyDigest) {
      ChatLog chatlog = this.minecraft.getReportingContext().chatLog();
      chatlog.push(LoggedChatMessageLink.header(pHeader, pHeaderSignature, pBodyDigest));
   }

   @Nullable
   private PlayerInfo resolveSenderPlayer(UUID pSender) {
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      return clientpacketlistener != null ? clientpacketlistener.getPlayerInfo(pSender) : null;
   }

   public void handleSystemMessage(Component pMessage, boolean pIsOverlay) {
      if (!this.minecraft.options.hideMatchedNames().get() || !this.minecraft.isBlocked(this.guessChatUUID(pMessage))) {
         pMessage = net.minecraftforge.client.ForgeHooksClient.onClientSystemChat(pMessage, pIsOverlay);
         if (pMessage == null) return;
         if (pIsOverlay) {
            this.minecraft.gui.setOverlayMessage(pMessage, false);
         } else {
            this.minecraft.gui.getChat().addMessage(pMessage);
            this.logSystemMessage(pMessage, Instant.now());
         }

         this.minecraft.getNarrator().sayNow(pMessage);
      }
   }

   private UUID guessChatUUID(Component pMessage) {
      String s = StringDecomposer.getPlainText(pMessage);
      String s1 = StringUtils.substringBetween(s, "<", ">");
      return s1 == null ? Util.NIL_UUID : this.minecraft.getPlayerSocialManager().getDiscoveredUUID(s1);
   }

   private boolean isSenderLocalPlayer(UUID pSender) {
      if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
         UUID uuid = this.minecraft.player.getGameProfile().getId();
         return uuid.equals(pSender);
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   interface Message {
      default boolean removeIfSignatureMatches(MessageSignature pSignature) {
         return false;
      }

      default void remove() {
      }

      boolean accept();

      default boolean isVisible() {
         return false;
      }
   }
}

package net.minecraft.network.chat;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public interface OutgoingPlayerChatMessage {
   Component serverContent();

   void sendToPlayer(ServerPlayer pPlayer, boolean pShouldFilter, ChatType.Bound pBoundChatType);

   void sendHeadersToRemainingPlayers(PlayerList pPlayerList);

   static OutgoingPlayerChatMessage create(PlayerChatMessage pMessage) {
      return (OutgoingPlayerChatMessage)(pMessage.signer().isSystem() ? new OutgoingPlayerChatMessage.NotTracked(pMessage) : new OutgoingPlayerChatMessage.Tracked(pMessage));
   }

   public static class NotTracked implements OutgoingPlayerChatMessage {
      private final PlayerChatMessage message;

      public NotTracked(PlayerChatMessage pMessage) {
         this.message = pMessage;
      }

      public Component serverContent() {
         return this.message.serverContent();
      }

      public void sendToPlayer(ServerPlayer pPlayer, boolean pShouldFilter, ChatType.Bound pBoundChatType) {
         PlayerChatMessage playerchatmessage = this.message.filter(pShouldFilter);
         if (!playerchatmessage.isFullyFiltered()) {
            RegistryAccess registryaccess = pPlayer.level.registryAccess();
            ChatType.BoundNetwork chattype$boundnetwork = pBoundChatType.toNetwork(registryaccess);
            pPlayer.connection.send(new ClientboundPlayerChatPacket(playerchatmessage, chattype$boundnetwork));
            pPlayer.connection.addPendingMessage(playerchatmessage);
         }

      }

      public void sendHeadersToRemainingPlayers(PlayerList pPlayerList) {
      }
   }

   public static class Tracked implements OutgoingPlayerChatMessage {
      private final PlayerChatMessage message;
      private final Set<ServerPlayer> playersWithFullMessage = Sets.newIdentityHashSet();

      public Tracked(PlayerChatMessage pMessage) {
         this.message = pMessage;
      }

      public Component serverContent() {
         return this.message.serverContent();
      }

      public void sendToPlayer(ServerPlayer pPlayer, boolean pShouldFilter, ChatType.Bound pBoundChatType) {
         PlayerChatMessage playerchatmessage = this.message.filter(pShouldFilter);
         if (!playerchatmessage.isFullyFiltered()) {
            this.playersWithFullMessage.add(pPlayer);
            RegistryAccess registryaccess = pPlayer.level.registryAccess();
            ChatType.BoundNetwork chattype$boundnetwork = pBoundChatType.toNetwork(registryaccess);
            pPlayer.connection.send(new ClientboundPlayerChatPacket(playerchatmessage, chattype$boundnetwork), PacketSendListener.exceptionallySend(() -> {
               return new ClientboundPlayerChatHeaderPacket(this.message);
            }));
            pPlayer.connection.addPendingMessage(playerchatmessage);
         }

      }

      public void sendHeadersToRemainingPlayers(PlayerList pPlayerList) {
         pPlayerList.broadcastMessageHeader(this.message, this.playersWithFullMessage);
      }
   }
}
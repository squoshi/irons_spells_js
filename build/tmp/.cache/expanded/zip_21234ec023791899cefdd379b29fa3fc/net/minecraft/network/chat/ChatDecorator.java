package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
   ChatDecorator PLAIN = (p_236950_, p_236951_) -> {
      return CompletableFuture.completedFuture(p_236951_);
   };

   CompletableFuture<Component> decorate(@Nullable ServerPlayer pSender, Component pMessage);

   default CompletableFuture<PlayerChatMessage> decorate(@Nullable ServerPlayer pSender, PlayerChatMessage pMessage) {
      return pMessage.signedContent().isDecorated() ? CompletableFuture.completedFuture(pMessage) : this.decorate(pSender, pMessage.serverContent()).thenApply(pMessage::withUnsignedContent);
   }

   static PlayerChatMessage attachIfNotDecorated(PlayerChatMessage pMessage, Component pDecorated) {
      return !pMessage.signedContent().isDecorated() ? pMessage.withUnsignedContent(pDecorated) : pMessage;
   }
}
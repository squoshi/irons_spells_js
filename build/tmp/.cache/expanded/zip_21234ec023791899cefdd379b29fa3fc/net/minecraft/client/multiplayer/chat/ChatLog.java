package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ChatLog {
   int NO_MESSAGE = -1;

   void push(LoggedChatEvent pEvent);

   @Nullable
   LoggedChatEvent lookup(int pId);

   @Nullable
   default ChatLog.Entry<LoggedChatEvent> lookupEntry(int pId) {
      LoggedChatEvent loggedchatevent = this.lookup(pId);
      return loggedchatevent != null ? new ChatLog.Entry<>(pId, loggedchatevent) : null;
   }

   default boolean contains(int pId) {
      return this.lookup(pId) != null;
   }

   int offset(int pId, int pOffset);

   default int before(int p_239680_) {
      return this.offset(p_239680_, -1);
   }

   default int after(int p_239584_) {
      return this.offset(p_239584_, 1);
   }

   int newest();

   int oldest();

   default ChatLog.Selection selectAll() {
      return this.selectAfter(this.oldest());
   }

   default ChatLog.Selection selectAllDescending() {
      return this.selectBefore(this.newest());
   }

   default ChatLog.Selection selectAfter(int pId) {
      return this.selectSequence(pId, this::after);
   }

   default ChatLog.Selection selectBefore(int pId) {
      return this.selectSequence(pId, this::before);
   }

   default ChatLog.Selection selectBetween(int pStartId, int pEndId) {
      return this.contains(pStartId) && this.contains(pEndId) ? this.selectSequence(pStartId, (p_239928_) -> {
         return p_239928_ == pEndId ? -1 : this.after(p_239928_);
      }) : this.selectNone();
   }

   default ChatLog.Selection selectSequence(final int pId, final IntUnaryOperator pNextIdSelector) {
      return !this.contains(pId) ? this.selectNone() : new ChatLog.Selection(this, new PrimitiveIterator.OfInt() {
         private int nextId = pId;

         public int nextInt() {
            int i = this.nextId;
            this.nextId = pNextIdSelector.applyAsInt(i);
            return i;
         }

         public boolean hasNext() {
            return this.nextId != -1;
         }
      });
   }

   private ChatLog.Selection selectNone() {
      return new ChatLog.Selection(this, IntList.of().iterator());
   }

   @OnlyIn(Dist.CLIENT)
   public static record Entry<T extends LoggedChatEvent>(int id, T event) {
      @Nullable
      public <U extends LoggedChatEvent> ChatLog.Entry<U> tryCast(Class<U> p_242327_) {
         return p_242327_.isInstance(this.event) ? new ChatLog.Entry<>(this.id, p_242327_.cast(this.event)) : null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Selection {
      private static final int CHARACTERISTICS = 1041;
      private final ChatLog log;
      private final PrimitiveIterator.OfInt ids;

      Selection(ChatLog pLog, PrimitiveIterator.OfInt pIds) {
         this.log = pLog;
         this.ids = pIds;
      }

      public IntStream ids() {
         return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(this.ids, 1041), false);
      }

      public Stream<LoggedChatEvent> events() {
         return this.ids().mapToObj(this.log::lookup).filter(Objects::nonNull);
      }

      public Collection<GameProfile> reportableGameProfiles() {
         return this.events().map((p_243150_) -> {
            if (p_243150_ instanceof LoggedChatMessage.Player loggedchatmessage$player) {
               if (loggedchatmessage$player.canReport(loggedchatmessage$player.profile().getId())) {
                  return loggedchatmessage$player.profile();
               }
            }

            return null;
         }).filter(Objects::nonNull).distinct().toList();
      }

      public Stream<ChatLog.Entry<LoggedChatEvent>> entries() {
         return this.ids().mapToObj(this.log::lookupEntry).filter(Objects::nonNull);
      }
   }
}
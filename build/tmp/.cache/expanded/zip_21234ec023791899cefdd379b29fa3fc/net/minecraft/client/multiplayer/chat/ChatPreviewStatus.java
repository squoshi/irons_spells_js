package net.minecraft.client.multiplayer.chat;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatPreviewStatus implements OptionEnum {
   OFF(0, "options.off"),
   LIVE(1, "options.chatPreview.live"),
   CONFIRM(2, "options.chatPreview.confirm");

   private static final ChatPreviewStatus[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ChatPreviewStatus::getId)).toArray((p_242169_) -> {
      return new ChatPreviewStatus[p_242169_];
   });
   private final int id;
   private final String key;

   private ChatPreviewStatus(int pId, String pKey) {
      this.id = pId;
      this.key = pKey;
   }

   public String getKey() {
      return this.key;
   }

   public int getId() {
      return this.id;
   }

   public static ChatPreviewStatus byId(int pId) {
      return BY_ID[Mth.positiveModulo(pId, BY_ID.length)];
   }
}
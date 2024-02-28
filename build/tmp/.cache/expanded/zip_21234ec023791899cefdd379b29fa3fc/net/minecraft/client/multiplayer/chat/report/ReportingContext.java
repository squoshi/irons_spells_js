package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.RollingMemoryChatLog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ReportingContext(AbuseReportSender sender, ReportEnvironment environment, ChatLog chatLog) {
   private static final int LOG_CAPACITY = 1024;

   public static ReportingContext create(ReportEnvironment pEnvironment, UserApiService pUserApiService) {
      RollingMemoryChatLog rollingmemorychatlog = new RollingMemoryChatLog(1024);
      AbuseReportSender abusereportsender = AbuseReportSender.create(pEnvironment, pUserApiService);
      return new ReportingContext(abusereportsender, pEnvironment, rollingmemorychatlog);
   }

   public boolean matches(ReportEnvironment pEnvironment) {
      return Objects.equals(this.environment, pEnvironment);
   }
}
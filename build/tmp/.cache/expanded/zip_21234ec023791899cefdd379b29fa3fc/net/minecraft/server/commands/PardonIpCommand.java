package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.regex.Matcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.IpBanList;

public class PardonIpCommand {
   private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.invalid"));
   private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("pardon-ip").requires((p_138116_) -> {
         return p_138116_.hasPermission(3);
      }).then(Commands.argument("target", StringArgumentType.word()).suggests((p_138113_, p_138114_) -> {
         return SharedSuggestionProvider.suggest(p_138113_.getSource().getServer().getPlayerList().getIpBans().getUserList(), p_138114_);
      }).executes((p_138111_) -> {
         return unban(p_138111_.getSource(), StringArgumentType.getString(p_138111_, "target"));
      })));
   }

   private static int unban(CommandSourceStack pSource, String pIpAddress) throws CommandSyntaxException {
      Matcher matcher = BanIpCommands.IP_ADDRESS_PATTERN.matcher(pIpAddress);
      if (!matcher.matches()) {
         throw ERROR_INVALID.create();
      } else {
         IpBanList ipbanlist = pSource.getServer().getPlayerList().getIpBans();
         if (!ipbanlist.isBanned(pIpAddress)) {
            throw ERROR_NOT_BANNED.create();
         } else {
            ipbanlist.remove(pIpAddress);
            pSource.sendSuccess(Component.translatable("commands.pardonip.success", pIpAddress), true);
            return 1;
         }
      }
   }
}
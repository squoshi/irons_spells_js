package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StopCommand {
   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("stop").requires((p_138790_) -> {
         return p_138790_.hasPermission(4);
      }).executes((p_138788_) -> {
         p_138788_.getSource().sendSuccess(Component.translatable("commands.stop.stopping"), true);
         p_138788_.getSource().getServer().halt(false);
         return 1;
      }));
   }
}
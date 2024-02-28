package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class SaveOffCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_OFF = new SimpleCommandExceptionType(Component.translatable("commands.save.alreadyOff"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("save-off").requires((p_138289_) -> {
         return p_138289_.hasPermission(4);
      }).executes((p_138287_) -> {
         CommandSourceStack commandsourcestack = p_138287_.getSource();
         boolean flag = false;

         for(ServerLevel serverlevel : commandsourcestack.getServer().getAllLevels()) {
            if (serverlevel != null && !serverlevel.noSave) {
               serverlevel.noSave = true;
               flag = true;
            }
         }

         if (!flag) {
            throw ERROR_ALREADY_OFF.create();
         } else {
            commandsourcestack.sendSuccess(Component.translatable("commands.save.disabled"), true);
            return 1;
         }
      }));
   }
}
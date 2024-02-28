package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;

public class ServerFunctionManager {
   private static final Component NO_RECURSIVE_TRACES = Component.translatable("commands.debug.function.noRecursion");
   private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
   private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
   final MinecraftServer server;
   @Nullable
   private ServerFunctionManager.ExecutionContext context;
   private List<CommandFunction> ticking = ImmutableList.of();
   private boolean postReload;
   private ServerFunctionLibrary library;

   public ServerFunctionManager(MinecraftServer pServer, ServerFunctionLibrary pLibrary) {
      this.server = pServer;
      this.library = pLibrary;
      this.postReload(pLibrary);
   }

   public int getCommandLimit() {
      return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
   }

   public CommandDispatcher<CommandSourceStack> getDispatcher() {
      return this.server.getCommands().getDispatcher();
   }

   public void tick() {
      this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
      if (this.postReload) {
         this.postReload = false;
         Collection<CommandFunction> collection = this.library.getTag(LOAD_FUNCTION_TAG);
         this.executeTagFunctions(collection, LOAD_FUNCTION_TAG);
      }

   }

   private void executeTagFunctions(Collection<CommandFunction> pFunctionObjects, ResourceLocation pIdentifier) {
      this.server.getProfiler().push(pIdentifier::toString);

      for(CommandFunction commandfunction : pFunctionObjects) {
         this.execute(commandfunction, this.getGameLoopSender());
      }

      this.server.getProfiler().pop();
   }

   public int execute(CommandFunction pFunctionObject, CommandSourceStack pSource) {
      return this.execute(pFunctionObject, pSource, (ServerFunctionManager.TraceCallbacks)null);
   }

   public int execute(CommandFunction pFunctionObject, CommandSourceStack pSource, @Nullable ServerFunctionManager.TraceCallbacks pTracer) {
      if (this.context != null) {
         if (pTracer != null) {
            this.context.reportError(NO_RECURSIVE_TRACES.getString());
            return 0;
         } else {
            this.context.delayFunctionCall(pFunctionObject, pSource);
            return 0;
         }
      } else {
         int i;
         try {
            this.context = new ServerFunctionManager.ExecutionContext(pTracer);
            i = this.context.runTopCommand(pFunctionObject, pSource);
         } finally {
            this.context = null;
         }

         return i;
      }
   }

   public void replaceLibrary(ServerFunctionLibrary pReloader) {
      this.library = pReloader;
      this.postReload(pReloader);
   }

   private void postReload(ServerFunctionLibrary pReloader) {
      this.ticking = ImmutableList.copyOf(pReloader.getTag(TICK_FUNCTION_TAG));
      this.postReload = true;
   }

   public CommandSourceStack getGameLoopSender() {
      return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
   }

   public Optional<CommandFunction> get(ResourceLocation pFunctionIdentifier) {
      return this.library.getFunction(pFunctionIdentifier);
   }

   public Collection<CommandFunction> getTag(ResourceLocation pFunctionTagIdentifier) {
      return this.library.getTag(pFunctionTagIdentifier);
   }

   public Iterable<ResourceLocation> getFunctionNames() {
      return this.library.getFunctions().keySet();
   }

   public Iterable<ResourceLocation> getTagNames() {
      return this.library.getAvailableTags();
   }

   class ExecutionContext {
      private int depth;
      @Nullable
      private final ServerFunctionManager.TraceCallbacks tracer;
      private final Deque<ServerFunctionManager.QueuedCommand> commandQueue = Queues.newArrayDeque();
      private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.newArrayList();

      ExecutionContext(ServerFunctionManager.TraceCallbacks pTracer) {
         this.tracer = pTracer;
      }

      void delayFunctionCall(CommandFunction pFunction, CommandSourceStack pSender) {
         int i = ServerFunctionManager.this.getCommandLimit();
         if (this.commandQueue.size() + this.nestedCalls.size() < i) {
            this.nestedCalls.add(new ServerFunctionManager.QueuedCommand(pSender, this.depth, new CommandFunction.FunctionEntry(pFunction)));
         }

      }

      int runTopCommand(CommandFunction pFunction, CommandSourceStack pSource) {
         int i = ServerFunctionManager.this.getCommandLimit();
         int j = 0;
         CommandFunction.Entry[] acommandfunction$entry = pFunction.getEntries();

         for(int k = acommandfunction$entry.length - 1; k >= 0; --k) {
            this.commandQueue.push(new ServerFunctionManager.QueuedCommand(pSource, 0, acommandfunction$entry[k]));
         }

         while(!this.commandQueue.isEmpty()) {
            try {
               ServerFunctionManager.QueuedCommand serverfunctionmanager$queuedcommand = this.commandQueue.removeFirst();
               ServerFunctionManager.this.server.getProfiler().push(serverfunctionmanager$queuedcommand::toString);
               this.depth = serverfunctionmanager$queuedcommand.depth;
               serverfunctionmanager$queuedcommand.execute(ServerFunctionManager.this, this.commandQueue, i, this.tracer);
               if (!this.nestedCalls.isEmpty()) {
                  Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                  this.nestedCalls.clear();
               }
            } finally {
               ServerFunctionManager.this.server.getProfiler().pop();
            }

            ++j;
            if (j >= i) {
               return j;
            }
         }

         return j;
      }

      public void reportError(String pError) {
         if (this.tracer != null) {
            this.tracer.onError(this.depth, pError);
         }

      }
   }

   public static class QueuedCommand {
      private final CommandSourceStack sender;
      final int depth;
      private final CommandFunction.Entry entry;

      public QueuedCommand(CommandSourceStack pSender, int pDepth, CommandFunction.Entry pEntry) {
         this.sender = pSender;
         this.depth = pDepth;
         this.entry = pEntry;
      }

      public void execute(ServerFunctionManager p_179986_, Deque<ServerFunctionManager.QueuedCommand> p_179987_, int p_179988_, @Nullable ServerFunctionManager.TraceCallbacks p_179989_) {
         try {
            this.entry.execute(p_179986_, this.sender, p_179987_, p_179988_, this.depth, p_179989_);
         } catch (CommandSyntaxException commandsyntaxexception) {
            if (p_179989_ != null) {
               p_179989_.onError(this.depth, commandsyntaxexception.getRawMessage().getString());
            }
         } catch (Exception exception) {
            if (p_179989_ != null) {
               p_179989_.onError(this.depth, exception.getMessage());
            }
         }

      }

      public String toString() {
         return this.entry.toString();
      }
   }

   public interface TraceCallbacks {
      void onCommand(int p_179990_, String p_179991_);

      void onReturn(int p_179992_, String p_179993_, int p_179994_);

      void onError(int p_179998_, String p_179999_);

      void onCall(int p_179995_, ResourceLocation p_179996_, int p_179997_);
   }
}
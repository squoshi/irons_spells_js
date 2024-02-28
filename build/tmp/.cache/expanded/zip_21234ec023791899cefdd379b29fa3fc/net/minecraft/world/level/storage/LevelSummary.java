package net.minecraft.world.level.storage;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary implements Comparable<LevelSummary> {
   private final LevelSettings settings;
   private final LevelVersion levelVersion;
   private final String levelId;
   private final boolean requiresManualConversion;
   private final boolean locked;
   private final Path icon;
   @Nullable
   private Component info;

   public LevelSummary(LevelSettings pSettings, LevelVersion pLevelVersion, String pLevelId, boolean pRequiresManualConversion, boolean pLocked, Path pIcon) {
      this.settings = pSettings;
      this.levelVersion = pLevelVersion;
      this.levelId = pLevelId;
      this.locked = pLocked;
      this.icon = pIcon;
      this.requiresManualConversion = pRequiresManualConversion;
   }

   /**
    * Returns the file name.
    */
   public String getLevelId() {
      return this.levelId;
   }

   /**
    * Return the display name of the save.
    */
   public String getLevelName() {
      return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
   }

   public Path getIcon() {
      return this.icon;
   }

   public boolean requiresManualConversion() {
      return this.requiresManualConversion;
   }

   public long getLastPlayed() {
      return this.levelVersion.lastPlayed();
   }

   public int compareTo(LevelSummary p_78360_) {
      if (this.levelVersion.lastPlayed() < p_78360_.levelVersion.lastPlayed()) {
         return 1;
      } else {
         return this.levelVersion.lastPlayed() > p_78360_.levelVersion.lastPlayed() ? -1 : this.levelId.compareTo(p_78360_.levelId);
      }
   }

   public LevelSettings getSettings() {
      return this.settings;
   }

   /**
    * Gets the EnumGameType.
    */
   public GameType getGameMode() {
      return this.settings.gameType();
   }

   public boolean isHardcore() {
      return this.settings.hardcore();
   }

   /**
    * @return {@code true} if cheats are enabled for this world
    */
   public boolean hasCheats() {
      return this.settings.allowCommands();
   }

   public MutableComponent getWorldVersionName() {
      return StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName()) ? Component.translatable("selectWorld.versionUnknown") : Component.literal(this.levelVersion.minecraftVersionName());
   }

   public LevelVersion levelVersion() {
      return this.levelVersion;
   }

   public boolean markVersionInList() {
      return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot() || this.backupStatus().shouldBackup();
   }

   public boolean askToOpenWorld() {
      return this.levelVersion.minecraftVersion().getVersion() > SharedConstants.getCurrentVersion().getDataVersion().getVersion();
   }

   public LevelSummary.BackupStatus backupStatus() {
      WorldVersion worldversion = SharedConstants.getCurrentVersion();
      int i = worldversion.getDataVersion().getVersion();
      int j = this.levelVersion.minecraftVersion().getVersion();
      if (!worldversion.isStable() && j < i) {
         return LevelSummary.BackupStatus.UPGRADE_TO_SNAPSHOT;
      } else {
         return j > i ? LevelSummary.BackupStatus.DOWNGRADE : LevelSummary.BackupStatus.NONE;
      }
   }

   public boolean isLocked() {
      return this.locked;
   }

   public boolean isDisabled() {
      if (!this.isLocked() && !this.requiresManualConversion()) {
         return !this.isCompatible();
      } else {
         return true;
      }
   }

   public boolean isCompatible() {
      return SharedConstants.getCurrentVersion().getDataVersion().isCompatible(this.levelVersion.minecraftVersion());
   }

   public Component getInfo() {
      if (this.info == null) {
         this.info = this.createInfo();
      }

      return this.info;
   }

   private Component createInfo() {
      if (this.isLocked()) {
         return Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
      } else if (this.requiresManualConversion()) {
         return Component.translatable("selectWorld.conversion").withStyle(ChatFormatting.RED);
      } else if (!this.isCompatible()) {
         return Component.translatable("selectWorld.incompatible_series").withStyle(ChatFormatting.RED);
      } else {
         MutableComponent mutablecomponent = this.isHardcore() ? Component.empty().append(Component.translatable("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED)) : Component.translatable("gameMode." + this.getGameMode().getName());
         if (this.hasCheats()) {
            mutablecomponent.append(", ").append(Component.translatable("selectWorld.cheats"));
         }

         MutableComponent mutablecomponent1 = this.getWorldVersionName();
         MutableComponent mutablecomponent2 = Component.literal(", ").append(Component.translatable("selectWorld.version")).append(" ");
         if (this.markVersionInList()) {
            mutablecomponent2.append(mutablecomponent1.withStyle(this.askToOpenWorld() ? ChatFormatting.RED : ChatFormatting.ITALIC));
         } else {
            mutablecomponent2.append(mutablecomponent1);
         }

         mutablecomponent.append(mutablecomponent2);
         return mutablecomponent;
      }
   }

   public static enum BackupStatus {
      NONE(false, false, ""),
      DOWNGRADE(true, true, "downgrade"),
      UPGRADE_TO_SNAPSHOT(true, false, "snapshot");

      private final boolean shouldBackup;
      private final boolean severe;
      private final String translationKey;

      private BackupStatus(boolean pShouldBackup, boolean pSevere, String pTranslationKey) {
         this.shouldBackup = pShouldBackup;
         this.severe = pSevere;
         this.translationKey = pTranslationKey;
      }

      public boolean shouldBackup() {
         return this.shouldBackup;
      }

      public boolean isSevere() {
         return this.severe;
      }

      public String getTranslationKey() {
         return this.translationKey;
      }
   }
   public boolean isExperimental() {
      return this.settings.getLifecycle().equals(com.mojang.serialization.Lifecycle.experimental());
   }
}

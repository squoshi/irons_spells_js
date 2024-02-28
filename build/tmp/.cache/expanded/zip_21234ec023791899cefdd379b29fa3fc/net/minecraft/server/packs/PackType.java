package net.minecraft.server.packs;

import com.mojang.bridge.game.GameVersion;

public enum PackType {
   CLIENT_RESOURCES("assets", com.mojang.bridge.game.PackType.RESOURCE),
   SERVER_DATA("data", com.mojang.bridge.game.PackType.DATA);

   private final String directory;
   public final com.mojang.bridge.game.PackType bridgeType;

   private PackType(String pDirectory, com.mojang.bridge.game.PackType pBridgeType) {
      this.directory = pDirectory;
      this.bridgeType = pBridgeType;
   }

   public String getDirectory() {
      return this.directory;
   }

   public int getVersion(GameVersion pGameVersion) {
      return pGameVersion.getPackVersion(this.bridgeType);
   }
}
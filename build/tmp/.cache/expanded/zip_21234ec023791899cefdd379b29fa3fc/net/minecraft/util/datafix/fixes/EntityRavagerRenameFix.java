package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;

public class EntityRavagerRenameFix extends SimplestEntityRenameFix {
   public static final Map<String, String> RENAMED_IDS = ImmutableMap.<String, String>builder().put("minecraft:illager_beast_spawn_egg", "minecraft:ravager_spawn_egg").build();

   public EntityRavagerRenameFix(Schema pOutputSchema, boolean pChangesType) {
      super("EntityRavagerRenameFix", pOutputSchema, pChangesType);
   }

   protected String rename(String pName) {
      return Objects.equals("minecraft:illager_beast", pName) ? "minecraft:ravager" : pName;
   }
}
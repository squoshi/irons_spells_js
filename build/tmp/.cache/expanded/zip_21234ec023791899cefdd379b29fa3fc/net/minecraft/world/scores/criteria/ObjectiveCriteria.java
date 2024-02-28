package net.minecraft.world.scores.criteria;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;

public class ObjectiveCriteria {
   private static final Map<String, ObjectiveCriteria> CUSTOM_CRITERIA = Maps.newHashMap();
   private static final Map<String, ObjectiveCriteria> CRITERIA_CACHE = Maps.newHashMap();
   public static final ObjectiveCriteria DUMMY = registerCustom("dummy");
   public static final ObjectiveCriteria TRIGGER = registerCustom("trigger");
   public static final ObjectiveCriteria DEATH_COUNT = registerCustom("deathCount");
   public static final ObjectiveCriteria KILL_COUNT_PLAYERS = registerCustom("playerKillCount");
   public static final ObjectiveCriteria KILL_COUNT_ALL = registerCustom("totalKillCount");
   public static final ObjectiveCriteria HEALTH = registerCustom("health", true, ObjectiveCriteria.RenderType.HEARTS);
   public static final ObjectiveCriteria FOOD = registerCustom("food", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria AIR = registerCustom("air", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria ARMOR = registerCustom("armor", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria EXPERIENCE = registerCustom("xp", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria LEVEL = registerCustom("level", true, ObjectiveCriteria.RenderType.INTEGER);
   public static final ObjectiveCriteria[] TEAM_KILL = new ObjectiveCriteria[]{registerCustom("teamkill." + ChatFormatting.BLACK.getName()), registerCustom("teamkill." + ChatFormatting.DARK_BLUE.getName()), registerCustom("teamkill." + ChatFormatting.DARK_GREEN.getName()), registerCustom("teamkill." + ChatFormatting.DARK_AQUA.getName()), registerCustom("teamkill." + ChatFormatting.DARK_RED.getName()), registerCustom("teamkill." + ChatFormatting.DARK_PURPLE.getName()), registerCustom("teamkill." + ChatFormatting.GOLD.getName()), registerCustom("teamkill." + ChatFormatting.GRAY.getName()), registerCustom("teamkill." + ChatFormatting.DARK_GRAY.getName()), registerCustom("teamkill." + ChatFormatting.BLUE.getName()), registerCustom("teamkill." + ChatFormatting.GREEN.getName()), registerCustom("teamkill." + ChatFormatting.AQUA.getName()), registerCustom("teamkill." + ChatFormatting.RED.getName()), registerCustom("teamkill." + ChatFormatting.LIGHT_PURPLE.getName()), registerCustom("teamkill." + ChatFormatting.YELLOW.getName()), registerCustom("teamkill." + ChatFormatting.WHITE.getName())};
   public static final ObjectiveCriteria[] KILLED_BY_TEAM = new ObjectiveCriteria[]{registerCustom("killedByTeam." + ChatFormatting.BLACK.getName()), registerCustom("killedByTeam." + ChatFormatting.DARK_BLUE.getName()), registerCustom("killedByTeam." + ChatFormatting.DARK_GREEN.getName()), registerCustom("killedByTeam." + ChatFormatting.DARK_AQUA.getName()), registerCustom("killedByTeam." + ChatFormatting.DARK_RED.getName()), registerCustom("killedByTeam." + ChatFormatting.DARK_PURPLE.getName()), registerCustom("killedByTeam." + ChatFormatting.GOLD.getName()), registerCustom("killedByTeam." + ChatFormatting.GRAY.getName()), registerCustom("killedByTeam." + ChatFormatting.DARK_GRAY.getName()), registerCustom("killedByTeam." + ChatFormatting.BLUE.getName()), registerCustom("killedByTeam." + ChatFormatting.GREEN.getName()), registerCustom("killedByTeam." + ChatFormatting.AQUA.getName()), registerCustom("killedByTeam." + ChatFormatting.RED.getName()), registerCustom("killedByTeam." + ChatFormatting.LIGHT_PURPLE.getName()), registerCustom("killedByTeam." + ChatFormatting.YELLOW.getName()), registerCustom("killedByTeam." + ChatFormatting.WHITE.getName())};
   private final String name;
   private final boolean readOnly;
   private final ObjectiveCriteria.RenderType renderType;

   private static ObjectiveCriteria registerCustom(String pName, boolean pReadOnly, ObjectiveCriteria.RenderType pRenderType) {
      ObjectiveCriteria objectivecriteria = new ObjectiveCriteria(pName, pReadOnly, pRenderType);
      CUSTOM_CRITERIA.put(pName, objectivecriteria);
      return objectivecriteria;
   }

   private static ObjectiveCriteria registerCustom(String pName) {
      return registerCustom(pName, false, ObjectiveCriteria.RenderType.INTEGER);
   }

   protected ObjectiveCriteria(String pName) {
      this(pName, false, ObjectiveCriteria.RenderType.INTEGER);
   }

   protected ObjectiveCriteria(String pName, boolean pReadOnly, ObjectiveCriteria.RenderType pRenderType) {
      this.name = pName;
      this.readOnly = pReadOnly;
      this.renderType = pRenderType;
      CRITERIA_CACHE.put(pName, this);
   }

   public static Set<String> getCustomCriteriaNames() {
      return ImmutableSet.copyOf(CUSTOM_CRITERIA.keySet());
   }

   public static Optional<ObjectiveCriteria> byName(String pName) {
      ObjectiveCriteria objectivecriteria = CRITERIA_CACHE.get(pName);
      if (objectivecriteria != null) {
         return Optional.of(objectivecriteria);
      } else {
         int i = pName.indexOf(58);
         return i < 0 ? Optional.empty() : Registry.STAT_TYPE.getOptional(ResourceLocation.of(pName.substring(0, i), '.')).flatMap((p_83619_) -> {
            return getStat(p_83619_, ResourceLocation.of(pName.substring(i + 1), '.'));
         });
      }
   }

   private static <T> Optional<ObjectiveCriteria> getStat(StatType<T> pStatType, ResourceLocation pResourceLocation) {
      return pStatType.getRegistry().getOptional(pResourceLocation).map(pStatType::get);
   }

   public String getName() {
      return this.name;
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public ObjectiveCriteria.RenderType getDefaultRenderType() {
      return this.renderType;
   }

   public static enum RenderType {
      INTEGER("integer"),
      HEARTS("hearts");

      private final String id;
      private static final Map<String, ObjectiveCriteria.RenderType> BY_ID;

      private RenderType(String pId) {
         this.id = pId;
      }

      public String getId() {
         return this.id;
      }

      public static ObjectiveCriteria.RenderType byId(String pRenderType) {
         return BY_ID.getOrDefault(pRenderType, INTEGER);
      }

      static {
         ImmutableMap.Builder<String, ObjectiveCriteria.RenderType> builder = ImmutableMap.builder();

         for(ObjectiveCriteria.RenderType objectivecriteria$rendertype : values()) {
            builder.put(objectivecriteria$rendertype.id, objectivecriteria$rendertype);
         }

         BY_ID = builder.build();
      }
   }
}
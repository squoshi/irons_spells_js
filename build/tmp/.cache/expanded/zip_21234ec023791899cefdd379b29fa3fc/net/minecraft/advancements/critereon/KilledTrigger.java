package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
   final ResourceLocation id;

   public KilledTrigger(ResourceLocation pId) {
      this.id = pId;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public KilledTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser) {
      return new KilledTrigger.TriggerInstance(this.id, pEntityPredicate, EntityPredicate.Composite.fromJson(pJson, "entity", pConditionsParser), DamageSourcePredicate.fromJson(pJson.get("killing_blow")));
   }

   public void trigger(ServerPlayer pPlayer, Entity pEntity, DamageSource pSource) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_48112_) -> {
         return p_48112_.matches(pPlayer, lootcontext, pSource);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final EntityPredicate.Composite entityPredicate;
      private final DamageSourcePredicate killingBlow;

      public TriggerInstance(ResourceLocation pCriterion, EntityPredicate.Composite pPlayer, EntityPredicate.Composite pEntityPredicate, DamageSourcePredicate pKillingBlow) {
         super(pCriterion, pPlayer);
         this.entityPredicate = pEntityPredicate;
         this.killingBlow = pKillingBlow;
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate pEntityPredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicate), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder pEntityPredicateBuilder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicateBuilder.build()), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity() {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate pEntityPredicate, DamageSourcePredicate pKillingBlow) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicate), pKillingBlow);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder pEntityPredicateBuilder, DamageSourcePredicate pKillingBlow) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicateBuilder.build()), pKillingBlow);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate pEntityPredicate, DamageSourcePredicate.Builder pKillingBlowBuilder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicate), pKillingBlowBuilder.build());
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder pEntityPredicateBuilder, DamageSourcePredicate.Builder pKillingBlowBuilder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicateBuilder.build()), pKillingBlowBuilder.build());
      }

      public static KilledTrigger.TriggerInstance playerKilledEntityNearSculkCatalyst() {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate pEntityPredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicate), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder pEntityPredicateBuilder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicateBuilder.build()), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer() {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate pEntityPredicate, DamageSourcePredicate pKillingBlow) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicate), pKillingBlow);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder pEntityPredicateBuilder, DamageSourcePredicate pKillingBlow) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicateBuilder.build()), pKillingBlow);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate pEntityPredicate, DamageSourcePredicate.Builder pKillingBlowBuilder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicate), pKillingBlowBuilder.build());
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder pEntityPredicateBuilder, DamageSourcePredicate.Builder pKillingBlowBuilder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityPredicateBuilder.build()), pKillingBlowBuilder.build());
      }

      public boolean matches(ServerPlayer pPlayer, LootContext pContext, DamageSource pSource) {
         return !this.killingBlow.matches(pPlayer, pSource) ? false : this.entityPredicate.matches(pContext);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("entity", this.entityPredicate.toJson(pConditions));
         jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
         return jsonobject;
      }
   }
}
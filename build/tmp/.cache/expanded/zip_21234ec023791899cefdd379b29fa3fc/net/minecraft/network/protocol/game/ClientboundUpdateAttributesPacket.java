package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

   public ClientboundUpdateAttributesPacket(int pEntityId, Collection<AttributeInstance> pAttributes) {
      this.entityId = pEntityId;
      this.attributes = Lists.newArrayList();

      for(AttributeInstance attributeinstance : pAttributes) {
         this.attributes.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(attributeinstance.getAttribute(), attributeinstance.getBaseValue(), attributeinstance.getModifiers()));
      }

   }

   public ClientboundUpdateAttributesPacket(FriendlyByteBuf pBuffer) {
      this.entityId = pBuffer.readVarInt();
      this.attributes = pBuffer.readList((p_179455_) -> {
         ResourceLocation resourcelocation = p_179455_.readResourceLocation();
         Attribute attribute = Registry.ATTRIBUTE.get(resourcelocation);
         double d0 = p_179455_.readDouble();
         List<AttributeModifier> list = p_179455_.readList((p_179457_) -> {
            return new AttributeModifier(p_179457_.readUUID(), "Unknown synced attribute modifier", p_179457_.readDouble(), AttributeModifier.Operation.fromValue(p_179457_.readByte()));
         });
         return new ClientboundUpdateAttributesPacket.AttributeSnapshot(attribute, d0, list);
      });
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeCollection(this.attributes, (p_179452_, p_179453_) -> {
         p_179452_.writeResourceLocation(Registry.ATTRIBUTE.getKey(p_179453_.getAttribute()));
         p_179452_.writeDouble(p_179453_.getBase());
         p_179452_.writeCollection(p_179453_.getModifiers(), (p_179449_, p_179450_) -> {
            p_179449_.writeUUID(p_179450_.getId());
            p_179449_.writeDouble(p_179450_.getAmount());
            p_179449_.writeByte(p_179450_.getOperation().toValue());
         });
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleUpdateAttributes(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public List<ClientboundUpdateAttributesPacket.AttributeSnapshot> getValues() {
      return this.attributes;
   }

   public static class AttributeSnapshot {
      private final Attribute attribute;
      private final double base;
      private final Collection<AttributeModifier> modifiers;

      public AttributeSnapshot(Attribute pAttribute, double pBase, Collection<AttributeModifier> pModifiers) {
         this.attribute = pAttribute;
         this.base = pBase;
         this.modifiers = pModifiers;
      }

      public Attribute getAttribute() {
         return this.attribute;
      }

      public double getBase() {
         return this.base;
      }

      public Collection<AttributeModifier> getModifiers() {
         return this.modifiers;
      }
   }
}
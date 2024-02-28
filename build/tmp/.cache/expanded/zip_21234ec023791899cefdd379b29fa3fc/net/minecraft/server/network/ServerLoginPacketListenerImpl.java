package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl implements TickablePacketListener, ServerLoginPacketListener {
   private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TICKS_BEFORE_LOGIN = 600;
   private static final RandomSource RANDOM = RandomSource.create();
   private final byte[] nonce;
   final MinecraftServer server;
   public final Connection connection;
   ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
   /** How long has player been trying to login into the server. */
   private int tick;
   @Nullable
   public GameProfile gameProfile;
   private final String serverId = "";
   @Nullable
   private ServerPlayer delayedAcceptPlayer;
   @Nullable
   private ProfilePublicKey.Data profilePublicKeyData;

   public ServerLoginPacketListenerImpl(MinecraftServer pServer, Connection pConnection) {
      this.server = pServer;
      this.connection = pConnection;
      this.nonce = Ints.toByteArray(RANDOM.nextInt());
   }

   public void tick() {
      if (this.state == State.NEGOTIATING) {
         // We force the state into "NEGOTIATING" which is otherwise unused. Once we're completed we move the negotiation onto "READY_TO_ACCEPT"
         // Might want to promote player object creation to here as well..
         boolean negotiationComplete = net.minecraftforge.network.NetworkHooks.tickNegotiation(this, this.connection, this.delayedAcceptPlayer);
         if (negotiationComplete)
            this.state = State.READY_TO_ACCEPT;
      } else if (this.state == ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT) {
         this.handleAcceptedLogin();
      } else if (this.state == ServerLoginPacketListenerImpl.State.DELAY_ACCEPT) {
         ServerPlayer serverplayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId());
         if (serverplayer == null) {
            this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
            this.placeNewPlayer(this.delayedAcceptPlayer);
            this.delayedAcceptPlayer = null;
         }
      }

      if (this.tick++ == 600) {
         this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
      }

   }

   /**
    * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
    */
   public Connection getConnection() {
      return this.connection;
   }

   public void disconnect(Component pReason) {
      try {
         LOGGER.info("Disconnecting {}: {}", this.getUserName(), pReason.getString());
         this.connection.send(new ClientboundLoginDisconnectPacket(pReason));
         this.connection.disconnect(pReason);
      } catch (Exception exception) {
         LOGGER.error("Error whilst disconnecting player", (Throwable)exception);
      }

   }

   public void handleAcceptedLogin() {
      ProfilePublicKey profilepublickey = null;
      if (!this.gameProfile.isComplete()) {
         this.gameProfile = this.createFakeProfile(this.gameProfile);
      } else {
         try {
            SignatureValidator signaturevalidator = this.server.getServiceSignatureValidator();
            profilepublickey = validatePublicKey(this.profilePublicKeyData, this.gameProfile.getId(), signaturevalidator, this.server.enforceSecureProfile());
         } catch (ProfilePublicKey.ValidationException profilepublickey$validationexception) {
            LOGGER.error("Failed to validate profile key: {}", (Object)profilepublickey$validationexception.getMessage());
            if (!this.connection.isMemoryConnection()) {
               this.disconnect(profilepublickey$validationexception.getComponent());
               return;
            }
         }
      }

      Component component1 = this.server.getPlayerList().canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
      if (component1 != null) {
         this.disconnect(component1);
      } else {
         this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
         if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
            this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> {
               this.connection.setupCompression(this.server.getCompressionThreshold(), true);
            }));
         }

         this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
         ServerPlayer serverplayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId());

         try {
            ServerPlayer serverplayer1 = this.server.getPlayerList().getPlayerForLogin(this.gameProfile, profilepublickey);
            if (serverplayer != null) {
               this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
               this.delayedAcceptPlayer = serverplayer1;
            } else {
               this.placeNewPlayer(serverplayer1);
            }
         } catch (Exception exception) {
            LOGGER.error("Couldn't place player in world", (Throwable)exception);
            Component component = Component.translatable("multiplayer.disconnect.invalid_player_data");
            this.connection.send(new ClientboundDisconnectPacket(component));
            this.connection.disconnect(component);
         }
      }

   }

   private void placeNewPlayer(ServerPlayer p_143700_) {
      this.server.getPlayerList().placeNewPlayer(this.connection, p_143700_);
   }

   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   public void onDisconnect(Component pReason) {
      LOGGER.info("{} lost connection: {}", this.getUserName(), pReason.getString());
   }

   public String getUserName() {
      return this.gameProfile != null ? this.gameProfile + " (" + this.connection.getRemoteAddress() + ")" : String.valueOf((Object)this.connection.getRemoteAddress());
   }

   @Nullable
   private static ProfilePublicKey validatePublicKey(@Nullable ProfilePublicKey.Data pProfilePublicKeyData, UUID pProfileId, SignatureValidator pSignatureValidator, boolean pEnforceSecureProfile) throws ProfilePublicKey.ValidationException {
      if (pProfilePublicKeyData == null) {
         if (pEnforceSecureProfile) {
            throw new ProfilePublicKey.ValidationException(ProfilePublicKey.MISSING_PROFILE_PUBLIC_KEY);
         } else {
            return null;
         }
      } else {
         return ProfilePublicKey.createValidated(pSignatureValidator, pProfileId, pProfilePublicKeyData, Duration.ZERO);
      }
   }

   public void handleHello(ServerboundHelloPacket pPacket) {
      Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
      Validate.validState(isValidUsername(pPacket.name()), "Invalid characters in username");
      this.profilePublicKeyData = pPacket.publicKey().orElse((ProfilePublicKey.Data)null);
      GameProfile gameprofile = this.server.getSingleplayerProfile();
      if (gameprofile != null && pPacket.name().equalsIgnoreCase(gameprofile.getName())) {
         this.gameProfile = gameprofile;
         this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
      } else {
         this.gameProfile = new GameProfile((UUID)null, pPacket.name());
         if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
            this.state = ServerLoginPacketListenerImpl.State.KEY;
            this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.nonce));
         } else {
            this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
         }

      }
   }

   public static boolean isValidUsername(String pUsername) {
      return pUsername.chars().filter((p_203791_) -> {
         return p_203791_ <= 32 || p_203791_ >= 127;
      }).findAny().isEmpty();
   }

   public void handleKey(ServerboundKeyPacket pPacket) {
      Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");

      final String s;
      try {
         PrivateKey privatekey = this.server.getKeyPair().getPrivate();
         if (this.profilePublicKeyData != null) {
            ProfilePublicKey profilepublickey = new ProfilePublicKey(this.profilePublicKeyData);
            if (!pPacket.isChallengeSignatureValid(this.nonce, profilepublickey)) {
               throw new IllegalStateException("Protocol error");
            }
         } else if (!pPacket.isNonceValid(this.nonce, privatekey)) {
            throw new IllegalStateException("Protocol error");
         }

         SecretKey secretkey = pPacket.getSecretKey(privatekey);
         Cipher cipher = Crypt.getCipher(2, secretkey);
         Cipher cipher1 = Crypt.getCipher(1, secretkey);
         s = (new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretkey))).toString(16);
         this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
         this.connection.setEncryptionKey(cipher, cipher1);
      } catch (CryptException cryptexception) {
         throw new IllegalStateException("Protocol error", cryptexception);
      }

      Thread thread = new Thread(net.minecraftforge.fml.util.thread.SidedThreadGroups.SERVER, "User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
         public void run() {
            GameProfile gameprofile = ServerLoginPacketListenerImpl.this.gameProfile;

            try {
               ServerLoginPacketListenerImpl.this.gameProfile = ServerLoginPacketListenerImpl.this.server.getSessionService().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.getAddress());
               if (ServerLoginPacketListenerImpl.this.gameProfile != null) {
                  ServerLoginPacketListenerImpl.LOGGER.info("UUID of player {} is {}", ServerLoginPacketListenerImpl.this.gameProfile.getName(), ServerLoginPacketListenerImpl.this.gameProfile.getId());
                  ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
               } else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                  ServerLoginPacketListenerImpl.LOGGER.warn("Failed to verify username but will let them in anyway!");
                  ServerLoginPacketListenerImpl.this.gameProfile = gameprofile;
                  ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
               } else {
                  ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                  ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", (Object)gameprofile.getName());
               }
            } catch (AuthenticationUnavailableException authenticationunavailableexception) {
               if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                  ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                  ServerLoginPacketListenerImpl.this.gameProfile = gameprofile;
                  ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
               } else {
                  ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                  ServerLoginPacketListenerImpl.LOGGER.error("Couldn't verify username because servers are unavailable");
               }
            }

         }

         @Nullable
         private InetAddress getAddress() {
            SocketAddress socketaddress = ServerLoginPacketListenerImpl.this.connection.getRemoteAddress();
            return ServerLoginPacketListenerImpl.this.server.getPreventProxyConnections() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress)socketaddress).getAddress() : null;
         }
      };
      thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public void handleCustomQueryPacket(ServerboundCustomQueryPacket pPacket) {
      if (!net.minecraftforge.network.NetworkHooks.onCustomPayload(pPacket, this.connection))
      this.disconnect(Component.translatable("multiplayer.disconnect.unexpected_query_response"));
   }

   protected GameProfile createFakeProfile(GameProfile pOriginal) {
      UUID uuid = UUIDUtil.createOfflinePlayerUUID(pOriginal.getName());
      return new GameProfile(uuid, pOriginal.getName());
   }

   static enum State {
      HELLO,
      KEY,
      AUTHENTICATING,
      NEGOTIATING,
      READY_TO_ACCEPT,
      DELAY_ACCEPT,
      ACCEPTED;
   }
}

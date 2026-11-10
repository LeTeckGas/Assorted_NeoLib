package com.grim3212.assorted.lib.platform;

import com.grim3212.assorted.lib.LibConstants;
import com.grim3212.assorted.lib.platform.services.INetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * NeoForge 1.21.1 implementation of {@link INetworkHelper}.
 * <p>
 * The public API is unchanged from 1.20.1: mods still call {@code Services.NETWORK.register(new MessageHandler<>(...))}
 * with an encoder/decoder. Internally every message is wrapped in a {@link LibPayload} and registered as a
 * NeoForge {@link CustomPacketPayload}.
 * <p>
 * NeoForge only accepts payload registrations during {@link RegisterPayloadHandlersEvent}, so mods must call
 * {@code register} from their mod constructor (or anything else that runs before that event).
 */
public class ForgeNetworkHelper implements INetworkHelper {

    private static final String PROTOCOL = "1";

    private static final Map<Class<?>, Registered<?>> byMessageType = new ConcurrentHashMap<>();
    private static final List<Registered<?>> pending = new CopyOnWriteArrayList<>();
    private static volatile boolean payloadsRegistered = false;

    /**
     * Wraps any message object so it can be sent through NeoForge's payload system.
     */
    public record LibPayload<MSG>(CustomPacketPayload.Type<LibPayload<MSG>> type, MSG message) implements CustomPacketPayload {
    }

    private record Registered<MSG>(MessageHandler<MSG> handler, CustomPacketPayload.Type<LibPayload<MSG>> type) {
        LibPayload<MSG> wrap(MSG message) {
            return new LibPayload<>(type, message);
        }

        StreamCodec<RegistryFriendlyByteBuf, LibPayload<MSG>> codec() {
            return StreamCodec.of(
                    (buf, payload) -> handler.encoder().accept(payload.message(), buf),
                    buf -> wrap(handler.decoder().apply(buf)));
        }

        void handle(LibPayload<MSG> payload, IPayloadContext context) {
            // Same behaviour as the 1.20.1 version: the player is only passed for messages sent to the server.
            // NeoForge already runs this on the main thread.
            Player player = handler.side() == MessageBoundSide.SERVER ? context.player() : null;
            handler.messageConsumer().accept(payload.message(), player);
        }
    }

    @Override
    public <MSG> void register(MessageHandler<MSG> handler) {
        if (payloadsRegistered) {
            throw new IllegalStateException("Tried to register message " + handler.id() + " after RegisterPayloadHandlersEvent. Register messages in your mod constructor.");
        }

        Registered<MSG> registered = new Registered<>(handler, new CustomPacketPayload.Type<>(handler.id()));
        byMessageType.put(handler.messageType(), registered);
        pending.add(registered);
    }

    /**
     * Called from the mod event bus by {@link com.grim3212.assorted.lib.AssortedLibForge}.
     */
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        // One registrar per namespace so every mod keeps its own protocol version
        Map<String, PayloadRegistrar> registrars = new ConcurrentHashMap<>();
        for (Registered<?> registered : pending) {
            PayloadRegistrar registrar = registrars.computeIfAbsent(registered.handler().id().getNamespace(), ns -> event.registrar(PROTOCOL));
            registerOne(registrar, registered);
        }
        LibConstants.LOG.debug("Registered {} network messages", pending.size());
        pending.clear();
        payloadsRegistered = true;
    }

    private static <MSG> void registerOne(PayloadRegistrar registrar, Registered<MSG> registered) {
        if (registered.handler().side() == MessageBoundSide.CLIENT) {
            registrar.playToClient(registered.type(), registered.codec(), registered::handle);
        } else {
            registrar.playToServer(registered.type(), registered.codec(), registered::handle);
        }
    }

    @SuppressWarnings("unchecked")
    private static <MSG> LibPayload<MSG> wrap(MSG message) {
        Registered<MSG> registered = (Registered<MSG>) byMessageType.get(message.getClass());
        if (registered == null) {
            throw new IllegalArgumentException("Message " + message.getClass().getName() + " was never registered");
        }
        return registered.wrap(message);
    }

    @Override
    public <MSG> void sendToNearby(Level world, BlockPos pos, MSG toSend) {
        if (world instanceof ServerLevel serverLevel) {
            LibPayload<MSG> payload = wrap(toSend);
            serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).stream()
                    .filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64)
                    .forEach(p -> PacketDistributor.sendToPlayer(p, payload));
        }
    }

    @Override
    public <MSG> void sendToNearby(Level world, Entity entity, MSG toSend) {
        sendToNearby(world, entity.blockPosition(), toSend);
    }

    @Override
    public <MSG> void sendTo(Player player, MSG toSend) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, wrap(toSend));
    }

    @Override
    public <MSG> void sendToServer(MSG toSend) {
        PacketDistributor.sendToServer(wrap(toSend));
    }
}

package me.cometkaizo.curtain.network;

import me.cometkaizo.curtain.Main;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Packets {

    public static SimpleChannel CHANNEL;

    private static final AtomicInteger packetCount = new AtomicInteger(0);
    private static int nextId() {
        return packetCount.getAndIncrement();
    }

    public static void init() {

        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Main.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        // register all messages, or they will crash the game when trying to send
    }

    public static <T extends Packet> void register(Class<T> messageType, Function<FriendlyByteBuf, T> decoder, NetworkDirection direction) {
        CHANNEL.messageBuilder(messageType, nextId(), direction)
                .encoder(Packet::encode)
                .decoder(decoder)
                .consumerMainThread(Packet::handle)
                .add();
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

}

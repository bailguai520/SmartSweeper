package com.qiyu.smartsweeper;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenGuiPacket(
    long totalItemsCleared,
    long totalEntitiesCleared,
    int clearCount,
    int timeUntilClear,
    boolean isEnabled,
    int clearInterval,
    int whitelistSize
) implements CustomPacketPayload {

    public static final Type<OpenGuiPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SmartSweeper.MODID, "open_gui"));
    
    public static final StreamCodec<ByteBuf, OpenGuiPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenGuiPacket decode(ByteBuf buf) {
            long totalItems = buf.readLong();
            long totalEntities = buf.readLong();
            int count = buf.readInt();
            int timeUntil = buf.readInt();
            boolean enabled = buf.readBoolean();
            int interval = buf.readInt();
            int whitelistCount = buf.readInt();
            return new OpenGuiPacket(totalItems, totalEntities, count, timeUntil, enabled, interval, whitelistCount);
        }

        @Override
        public void encode(ByteBuf buf, OpenGuiPacket packet) {
            buf.writeLong(packet.totalItemsCleared);
            buf.writeLong(packet.totalEntitiesCleared);
            buf.writeInt(packet.clearCount);
            buf.writeInt(packet.timeUntilClear);
            buf.writeBoolean(packet.isEnabled);
            buf.writeInt(packet.clearInterval);
            buf.writeInt(packet.whitelistSize);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenGuiPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new ItemClearGuiScreen(
                null,
                packet.totalItemsCleared,
                packet.totalEntitiesCleared,
                packet.clearCount,
                packet.timeUntilClear,
                packet.isEnabled,
                packet.clearInterval,
                packet.whitelistSize
            ));
        });
    }
}



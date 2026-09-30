package org.bukkit.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.*;

class PlayerActionBarTest {
    private RecordingConnection connection;
    private ServerPlayer handle;
    private CraftPlayer player;

    @BeforeAll
    static void initializeMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void createPlayerWithoutStartingAWorld() throws ReflectiveOperationException {
        // Skip world/socket constructors; execute the real CraftPlayer method,
        // serializers and packet. Only the network send boundary is replaced.
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        connection = (RecordingConnection) unsafe.allocateInstance(RecordingConnection.class);
        connection.packets = new ArrayList<>();
        handle = (ServerPlayer) unsafe.allocateInstance(ServerPlayer.class);
        handle.connection = connection;
        player = (CraftPlayer) unsafe.allocateInstance(CraftPlayer.class);
        // Bind the base handle only; action bars require no player inventory.
        player.setHandle((Entity) handle);
    }

    @Test
    void componentReminderReachesTheActionBarWithLegacyColours() throws ReflectiveOperationException {
        Component message = LegacyComponentSerializer.legacySection()
                .deserialize("§c你还有 §e30 §c秒完成绑定邮箱");

        send(Component.class, message);

        Component delivered = deliveredMessage();
        assertEquals(message, delivered);
        assertEquals("你还有 30 秒完成绑定邮箱", deliveredPacket().getText().getString());
    }

    @Test
    void rgbFormattingAndTranslatableComponentsArePreserved() throws ReflectiveOperationException {
        Component message = Component.text("RGB ", TextColor.color(0x12abef))
                .decorate(TextDecoration.BOLD)
                .append(Component.translatable("chat.type.text")
                        .args(Component.text("Steve"), Component.text("你好", NamedTextColor.YELLOW)));

        send(Component.class, message);

        assertEquals(message, deliveredMessage());
    }

    @Test
    void componentLikeUsesTheSameActionBarPath() throws ReflectiveOperationException {
        ComponentLike message = () -> Component.text("登录成功", NamedTextColor.GREEN);

        send(ComponentLike.class, message);

        assertEquals(Component.text("登录成功", NamedTextColor.GREEN), deliveredMessage());
    }

    @Test
    void nbtComponentsAreSupported() throws ReflectiveOperationException {
        Component message = Component.entityNBT("CustomName", "@s");

        send(Component.class, message);

        assertEquals(message, deliveredMessage());
    }

    @Test
    void translationFallbackIsPreserved() throws ReflectiveOperationException {
        Component message = Component.translatable("plugin.custom", "中文提示");

        send(Component.class, message);

        assertEquals(message, deliveredMessage());
    }

    @Test
    void selectorSeparatorIsPreserved() throws ReflectiveOperationException {
        Component message = Component.selector("@a", Component.text(", "));

        send(Component.class, message);

        assertEquals(message, deliveredMessage());
    }

    @Test
    void emptyComponentClearsTheActionBar() throws ReflectiveOperationException {
        send(Component.class, Component.empty());

        assertEquals("", deliveredPacket().getText().getString());
    }

    @Test
    void disconnectedPlayerDoesNotSendPackets() throws ReflectiveOperationException {
        handle.connection = null;

        send(Component.class, Component.text("登录成功"));

        assertTrue(connection.packets.isEmpty());
    }

    private void send(Class<?> parameterType, Object message) throws ReflectiveOperationException {
        // Look up the exact Paper binary signature without requiring it to
        // exist at test compile time, so a missing API is a regression failure.
        Method method = Player.class.getMethod("sendActionBar", parameterType);
        assertEquals(void.class, method.getReturnType());
        method.invoke(player, message);
    }

    private Component deliveredMessage() {
        return GsonComponentSerializer.gson().deserialize(
                net.minecraft.network.chat.Component.Serializer.toJson(deliveredPacket().getText()));
    }

    private ClientboundSetActionBarTextPacket deliveredPacket() {
        assertEquals(1, connection.packets.size());
        return assertInstanceOf(ClientboundSetActionBarTextPacket.class, connection.packets.get(0));
    }

    private static class RecordingConnection extends ServerGamePacketListenerImpl {
        private List<Packet<?>> packets;

        private RecordingConnection() {
            super(null, null, null); // Not invoked: allocated without a live server.
        }

        @Override
        public void send(Packet<?> packet) {
            packets.add(packet);
        }
    }
}

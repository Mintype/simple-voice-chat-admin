package com.github.mintype;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.minecraft.server.level.ServerPlayer;
//import de.maxhenkel.voicechat.api.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoicechatAdminPlugin implements VoicechatPlugin {

    // Store the API instance and our global state
    public static VoicechatServerApi serverApi;
    public static boolean isGlobalMuted = false;
    public static Group globalGroup = null;

    // Maps a player's UUID to their previous Group UUID
    public static final Map<UUID, UUID> previousGroups = new HashMap<>();

    @Override
    public String getPluginId() {
        return "voicechat_admin";
    }

    @Override
    public void initialize(VoicechatApi api) {
        // Initialization if needed
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();

        // Create the massive group
        globalGroup = VoicechatAdminPlugin.serverApi.groupBuilder()
                .setName("Global Meeting")
                .setPersistent(true)
                .setType(Group.Type.OPEN)
                .build();
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        // If global mute is active, block non-operator audio
        if (isGlobalMuted && event.getSenderConnection() != null) {

            de.maxhenkel.voicechat.api.ServerPlayer vcPlayer = event.getSenderConnection().getPlayer();
            ServerPlayer player = (ServerPlayer) vcPlayer.getPlayer();

            boolean isOperator = player.level().getServer().getPlayerList().isOp(player.nameAndId());

            // Check if the player is NOT an operator
            if (!isOperator) {
                event.cancel(); // Drops the audio packet

            }
        }
    }
}
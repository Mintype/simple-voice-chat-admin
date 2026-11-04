package com.github.mintype;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.commands.Commands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SimpleVoiceChatAdmin implements ModInitializer {
	public static final String MOD_ID = "simple-voice-chat-admin";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("vcadmin")
                    .requires(source -> {
                        var player = source.getPlayer();

                        // If player is null, the command is being run by the server console/RCON
                        if (player == null) {
                            return true;
                        }

                        // Check if the executing player is on the server's OP list
                        return player.level().getServer().getPlayerList().isOp(player.nameAndId());
                    })

                    // MUTE ALL COMMAND
                    .then(Commands.literal("muteall").executes(context -> {
                        if (VoicechatAdminPlugin.serverApi == null) return 0;

                        if (VoicechatAdminPlugin.isGlobalMuted) return 0;

                        VoicechatAdminPlugin.isGlobalMuted = true;
                        VoicechatAdminPlugin.previousGroups.clear();

//                        // Create the massive group
//                        VoicechatAdminPlugin.globalGroup = VoicechatAdminPlugin.serverApi.groupBuilder()
//                                .setName("Global Meeting")
//                                .setPersistent(true)
//                                .setType(Group.Type.OPEN)
//                                .build();

                        // Move everyone into it
                        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                            VoicechatConnection conn = VoicechatAdminPlugin.serverApi.getConnectionOf(player.getUUID());
                            if (conn != null) {
                                // Save their current group
                                if (conn.getGroup() != null) {
                                    VoicechatAdminPlugin.previousGroups.put(player.getUUID(), conn.getGroup().getId());
                                }
                                // Move them to the global group
                                conn.setGroup(VoicechatAdminPlugin.globalGroup);
                            }
                        }

                        context.getSource().sendSystemMessage(Component.literal("§cAll non-OPs globally muted and moved to Global Meeting."));
                        return 1;
                    }))

                    // UNMUTE ALL COMMAND
                    .then(Commands.literal("unmuteall").executes(context -> {
                        if (VoicechatAdminPlugin.serverApi == null) return 0;

                        if (!VoicechatAdminPlugin.isGlobalMuted) return 0;

                        VoicechatAdminPlugin.isGlobalMuted = false;

                        // Restore everyone to their old groups
                        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                            VoicechatConnection conn = VoicechatAdminPlugin.serverApi.getConnectionOf(player.getUUID());
                            if (conn != null) {
                                UUID oldGroupId = VoicechatAdminPlugin.previousGroups.get(player.getUUID());
                                if (oldGroupId != null) {
                                    Group oldGroup = VoicechatAdminPlugin.serverApi.getGroup(oldGroupId);
                                    conn.setGroup(oldGroup); // Restore group
                                } else {
                                    conn.setGroup(null); // Remove from group
                                }
                            }
                        }

                        VoicechatAdminPlugin.previousGroups.clear();
//                        VoicechatAdminPlugin.globalGroup = null;

                        context.getSource().sendSystemMessage(Component.literal("§aAll players unmuted and restored to previous groups."));
                        return 1;
                    }))
            );
        });

		LOGGER.info("Simple Voice Chat Admin Initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

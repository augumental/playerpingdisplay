package com.badeye.playerpingdisplay;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;

public final class PlayerPingDisplayClient implements ClientModInitializer {
	public static final String MOD_ID = "playerpingdisplay";
	private static final int DISPLAY_TICKS = 100;

	private static KeyMapping configKey;
	private static TrackedPlayer trackedPlayer;

	@Override
	public void onInitializeClient() {
		PlayerPingDisplayConfig.load();

		configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.playerpingdisplay.open_config",
				InputConstants.UNKNOWN.getValue(),
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "controls"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(PlayerPingDisplayClient::onEndTick);
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide() && entity instanceof Player targetPlayer) {
				trackedPlayer = new TrackedPlayer(targetPlayer, DISPLAY_TICKS);
			}

			return InteractionResult.PASS;
		});
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "ping"),
				(graphics, deltaTracker) -> PlayerPingHud.render(graphics, trackedPlayer));
	}

	public static void showConfigScreen(Minecraft client) {
		MinecraftCompat.setScreen(client, new PlayerPingDisplayConfigScreen(MinecraftCompat.currentScreen(client)));
	}

	static int getCurrentPing(Player player) {
		Minecraft client = Minecraft.getInstance();
		if (client.getConnection() == null) {
			return -1;
		}

		return switch (PlayerPingDisplayConfig.pingMode) {
			case UUID -> getPingByUuidThenName(player);
			case TAB_LIST_PROFILE_DISPLAY -> getPingByNameThenUuid(player);
		};
	}

	private static int getPingByUuidThenName(Player player) {
		Minecraft client = Minecraft.getInstance();
		if (client.getConnection() == null) {
			return -1;
		}

		var entry = client.getConnection().getPlayerInfo(player.getUUID());
		return entry == null ? getPingByName(player) : entry.getLatency();
	}

	private static int getPingByNameThenUuid(Player player) {
		int nameMatchedPing = getPingByName(player);
		if (nameMatchedPing >= 0) {
			return nameMatchedPing;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.getConnection() == null) {
			return -1;
		}

		var entry = client.getConnection().getPlayerInfo(player.getUUID());
		return entry == null ? -1 : entry.getLatency();
	}

	private static int getPingByName(Player player) {
		Minecraft client = Minecraft.getInstance();
		if (client.getConnection() == null) {
			return -1;
		}

		String profileName = player.getGameProfile().name();
		String visibleName = player.getName().getString();
		for (PlayerInfo playerListEntry : client.getConnection().getOnlinePlayers()) {
			if (playerListEntry.getProfile().name().equalsIgnoreCase(profileName)) {
				return playerListEntry.getLatency();
			}

			if (playerListEntry.getTabListDisplayName() != null && playerListEntry.getTabListDisplayName().getString().equalsIgnoreCase(visibleName)) {
				return playerListEntry.getLatency();
			}
		}

		return -1;
	}

	private static void onEndTick(Minecraft client) {
		while (configKey.consumeClick()) {
			showConfigScreen(client);
		}

		if (trackedPlayer != null && !trackedPlayer.tick()) {
			trackedPlayer = null;
		}
	}

	static final class TrackedPlayer {
		private final Player player;
		private int ticksRemaining;
		private int ticksUntilPingRefresh;
		private int cachedPing;

		private TrackedPlayer(Player player, int ticksRemaining) {
			this.player = player;
			this.ticksRemaining = ticksRemaining;
			refreshPing();
		}

		boolean tick() {
			if (PlayerPingDisplayConfig.dynamicResolverEnabled && ticksUntilPingRefresh-- <= 0) {
				refreshPing();
			}

			return player.isAlive() && ticksRemaining-- > 0;
		}

		String playerName() {
			return player.getName().getString();
		}

		String pingText() {
			return cachedPing >= 0 ? cachedPing + " ms" : "unknown";
		}

		int pingColor() {
			if (cachedPing < 0) {
				return 0xAAAAAA;
			}

			if (cachedPing < 50) {
				return 0x55FF55;
			}

			if (cachedPing < 100) {
				return 0xFFFF55;
			}

			if (cachedPing < 200) {
				return 0xFF5555;
			}

			return 0xAA0000;
		}

		float opacity() {
			return Math.min(1.0F, ticksRemaining / 20.0F);
		}

		private void refreshPing() {
			int resolvedPing = getCurrentPing(player);
			if (resolvedPing > 0 || cachedPing <= 0) {
				cachedPing = resolvedPing;
			}
			ticksUntilPingRefresh = PlayerPingDisplayConfig.refreshTicks;
		}
	}
}

package com.badeye.playerpingdisplay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class PlayerPingDisplayClient implements ClientModInitializer {
	public static final String MOD_ID = "playerpingdisplay";
	private static final int DISPLAY_TICKS = 100;

	private static KeyBinding configKey;
	private static TrackedPlayer trackedPlayer;

	@Override
	public void onInitializeClient() {
		PlayerPingDisplayConfig.load();

		configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.playerpingdisplay.open_config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				KeyBinding.Category.create(Identifier.of(MOD_ID, "controls"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(PlayerPingDisplayClient::onEndTick);
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient() && entity instanceof PlayerEntity targetPlayer) {
				trackedPlayer = new TrackedPlayer(targetPlayer, DISPLAY_TICKS);
			}

			return ActionResult.PASS;
		});
		HudRenderCallback.EVENT.register((drawContext, tickCounter) -> PlayerPingHud.render(drawContext, trackedPlayer));
	}

	public static void showConfigScreen(MinecraftClient client) {
		client.setScreen(new PlayerPingDisplayConfigScreen(client.currentScreen));
	}

	static int getCurrentPing(PlayerEntity player) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getNetworkHandler() == null) {
			return -1;
		}

		return switch (PlayerPingDisplayConfig.pingMode) {
			case UUID -> getPingByUuidThenName(player);
			case TAB_LIST_PROFILE_DISPLAY -> getPingByNameThenUuid(player);
		};
	}

	private static int getPingByUuidThenName(PlayerEntity player) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getNetworkHandler() == null) {
			return -1;
		}

		var entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
		return entry == null ? getPingByName(player) : entry.getLatency();
	}

	private static int getPingByNameThenUuid(PlayerEntity player) {
		int nameMatchedPing = getPingByName(player);
		if (nameMatchedPing >= 0) {
			return nameMatchedPing;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getNetworkHandler() == null) {
			return -1;
		}

		var entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
		return entry == null ? -1 : entry.getLatency();
	}

	private static int getPingByName(PlayerEntity player) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getNetworkHandler() == null) {
			return -1;
		}

		String profileName = player.getGameProfile().name();
		String visibleName = player.getName().getString();
		for (PlayerListEntry playerListEntry : client.getNetworkHandler().getPlayerList()) {
			if (playerListEntry.getProfile().name().equalsIgnoreCase(profileName)) {
				return playerListEntry.getLatency();
			}

			if (playerListEntry.getDisplayName() != null && playerListEntry.getDisplayName().getString().equalsIgnoreCase(visibleName)) {
				return playerListEntry.getLatency();
			}
		}

		return -1;
	}

	private static void onEndTick(MinecraftClient client) {
		while (configKey.wasPressed()) {
			showConfigScreen(client);
		}

		if (trackedPlayer != null && !trackedPlayer.tick()) {
			trackedPlayer = null;
		}
	}

	static final class TrackedPlayer {
		private final PlayerEntity player;
		private int ticksRemaining;
		private int ticksUntilPingRefresh;
		private int cachedPing;

		private TrackedPlayer(PlayerEntity player, int ticksRemaining) {
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

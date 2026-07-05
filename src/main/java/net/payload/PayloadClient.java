package net.payload;

import java.util.ArrayList;
import java.util.List;

import com.mojang.logging.LogUtils;

import net.payload.altmanager.AltManager;
import net.payload.api.IAddon;
import net.payload.cmd.CommandManager;
import net.payload.cmd.GlobalChat;
import net.payload.cmd.commands.*;
import net.payload.combatmanager.CombatManager;
import net.payload.event.EventManager;
import net.payload.gui.GuiManager;
import net.payload.gui.colors.GradientColor;
import net.payload.gui.font.FontManager;
import net.payload.macros.MacroManager;
import net.payload.mixin.interfaces.IMinecraftClient;
import net.payload.module.ModuleManager;
import net.payload.proxymanager.ProxyManager;
import net.payload.settings.SettingManager;
import net.payload.settings.friends.FriendsList;
import net.payload.utils.EntityManager;
import net.payload.utils.anticheat.AntiCheatManager;
import net.payload.utils.block.BlockIterator;
import net.payload.utils.block.BreakManager;
import net.payload.utils.discord.RPCManager;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.player.PlayerManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;
import net.payload.utils.rotation.RotationManager;
public class PayloadClient {
	public static final String NAME = "Payload";
	public static final String VERSION = "1.21.3";
	public static final String WEBSITE = "https://payload.technology/";
	public static final String PAYLOAD_VERSION = "1.4.4";
	public static MinecraftClient MC;
	public static IMinecraftClient IMC;
	public RotationManager rotationManager;
	public ModuleManager moduleManager;
	public CommandManager commandManager;
	public AltManager altManager;
	public ProxyManager proxyManager;
	public GuiManager guiManager;
	public EntityManager entityManager;
	public FontManager fontManager;
	public CombatManager combatManager;
	public RPCManager rpcManager;
	public SettingManager settingManager;
	public BreakManager breakManager;
	public FriendsList friendsList;
	public GlobalChat globalChat;
	public BlockIterator blockIterator;
	public PlayerManager playerManager;
	public InventoryUtil inventoryUtil;
	public AntiCheatManager antiCheatManager;
	public EventManager eventManager;
	public MacroManager macroManager;
	public static List<IAddon> addons = new ArrayList<>();

	public void Initialize() {
		try {
			MC = MinecraftClient.getInstance();
			IMC = (IMinecraftClient) MC;

		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}


	public void loadAssets() {
		eventManager = new EventManager();
		for (EntrypointContainer<IAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("payload", IAddon.class)) {
			IAddon addon = entrypoint.getEntrypoint();
			try {
			} catch (Throwable e) {
			}

			addons.add(addon);
		}
		entityManager = new EntityManager();
		settingManager = new SettingManager();
		friendsList = new FriendsList();
		moduleManager = new ModuleManager(addons);
		commandManager = new CommandManager(addons);
		fontManager = new FontManager();
		fontManager.Initialize();
		combatManager = new CombatManager();
		macroManager = new MacroManager();
		guiManager = new GuiManager();
		guiManager.Initialize();
		altManager = new AltManager();
		proxyManager = new ProxyManager();
		blockIterator = new BlockIterator();
		playerManager = new PlayerManager();
		inventoryUtil = new InventoryUtil();
		antiCheatManager = new AntiCheatManager();
		breakManager = new BreakManager();
		rotationManager = new RotationManager();
		SettingManager.loadGlobalSettings();
		SettingManager.loadSettings();
		globalChat = new GlobalChat();
		globalChat.StartListener();
	}

	public void endClient() {
		try {
			SettingManager.saveSettings();
			altManager.saveAlts();
			friendsList.save();
			macroManager.save();
			moduleManager.modules.forEach(s -> s.onDisable());
		} catch (Exception e) {
			LogUtils.getLogger().error(e.getMessage());
		}
	}


}

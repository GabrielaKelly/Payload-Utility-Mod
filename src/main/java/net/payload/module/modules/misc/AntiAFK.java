package net.payload.module.modules.misc;

import net.payload.Payload;
import net.payload.event.events.LookAtEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.LookAtListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;


import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AntiAFK extends Module implements TickListener, LookAtListener {

    private final SettingGroup actionsSettings;
    private final SettingGroup messagesSettings;

    // Actions Settings
    private final BooleanSetting jump = BooleanSetting.builder()
            .id("anti_afk_jump")
            .displayName("Jump")
            .description("Jump randomly")
            .defaultValue(true)
            .build();

    private final BooleanSetting swing = BooleanSetting.builder()
            .id("anti_afk_swing")
            .displayName("Swing")
            .description("Swings your hand")
            .defaultValue(false)
            .build();

    private final BooleanSetting sneak = BooleanSetting.builder()
            .id("anti_afk_sneak")
            .displayName("Sneak")
            .description("Sneaks and unsneaks quickly")
            .defaultValue(false)
            .build();

    private final FloatSetting sneakTime = FloatSetting.builder()
            .id("anti_afk_sneak_time")
            .displayName("Sneak Time")
            .description("How many ticks to stay sneaked")
            .defaultValue(5f)
            .minValue(1f)
            .maxValue(20f)
            .step(1f)
            .build();

    private final BooleanSetting strafe = BooleanSetting.builder()
            .id("anti_afk_strafe")
            .displayName("Strafe")
            .description("Strafe right and left")
            .defaultValue(false)
            .build();

    private final BooleanSetting spin = BooleanSetting.builder()
            .id("anti_afk_spin")
            .displayName("Spin")
            .description("Spins the player in place")
            .defaultValue(true)
            .build();

    public enum SpinMode {
        Server,
        Client
    }

    private final EnumSetting<SpinMode> spinMode = EnumSetting.<SpinMode>builder()
            .id("anti_afk_spin_mode")
            .displayName("Spin Mode")
            .description("The method of rotating")
            .defaultValue(SpinMode.Server)
            .build();

    private final FloatSetting spinSpeed = FloatSetting.builder()
            .id("anti_afk_spin_speed")
            .displayName("Speed")
            .description("The speed to spin you")
            .defaultValue(7f)
            .minValue(1f)
            .maxValue(20f)
            .step(1f)
            .build();

    private final FloatSetting pitch = FloatSetting.builder()
            .id("anti_afk_pitch")
            .displayName("Pitch")
            .description("The pitch to send to the server")
            .defaultValue(0f)
            .minValue(-90f)
            .maxValue(90f)
            .step(1f)
            .build();

    // Messages Settings
    private final BooleanSetting sendMessages = BooleanSetting.builder()
            .id("anti_afk_send_messages")
            .displayName("Send Messages")
            .description("Sends messages to prevent getting kicked for AFK")
            .defaultValue(false)
            .build();

    private final BooleanSetting randomMessage = BooleanSetting.builder()
            .id("anti_afk_random_message")
            .displayName("Random")
            .description("Selects a random message from your message list")
            .defaultValue(true)
            .build();

    private final FloatSetting delay = FloatSetting.builder()
            .id("anti_afk_delay")
            .displayName("Delay")
            .description("The delay between specified messages in seconds")
            .defaultValue(20f)
            .minValue(0f)
            .maxValue(30f)
            .step(1f)
            .build();

    public enum ChatSetting {
        Innocent, PayloadLover, Autism
    }

    private final EnumSetting<AntiAFK.ChatSetting> chatmode = EnumSetting.<AntiAFK.ChatSetting>builder()
            .id("antiafk_chatmode")
            .displayName("ChatPool")
            .description("pool of words to choose")
            .defaultValue(ChatSetting.PayloadLover)
            .build();

    public AntiAFK() {
        super("AntiAFK");

        this.setCategory(Category.of("Misc"));
        this.setDescription("Prevents the server from kicking you");

        // Initialize setting groups
        actionsSettings = SettingGroup.Builder.builder()
                .id("anti_afk_actions")
                .displayName("Actions")
                .description("Action settings for anti-AFK")
                .build();

        messagesSettings = SettingGroup.Builder.builder()
                .id("anti_afk_messages_group")
                .displayName("Messages")
                .description("Message settings for anti-AFK")
                .build();

        // Add settings to actions group
        actionsSettings.addSetting(jump);
        actionsSettings.addSetting(swing);
        actionsSettings.addSetting(sneak);
        actionsSettings.addSetting(sneakTime);
        actionsSettings.addSetting(strafe);
        actionsSettings.addSetting(spin);
        actionsSettings.addSetting(spinMode);
        actionsSettings.addSetting(spinSpeed);
        actionsSettings.addSetting(pitch);

        // Add settings to messages group
        messagesSettings.addSetting(sendMessages);
        messagesSettings.addSetting(randomMessage);
        messagesSettings.addSetting(delay);
        messagesSettings.addSetting(chatmode);

        // Add groups to module
        this.addSetting(actionsSettings);
        this.addSetting(messagesSettings);

        // Register settings with SettingManager
        SettingManager.registerSetting(actionsSettings);
        SettingManager.registerSetting(messagesSettings);

        // Register action settings
        SettingManager.registerSetting(jump);
        SettingManager.registerSetting(swing);
        SettingManager.registerSetting(sneak);
        SettingManager.registerSetting(sneakTime);
        SettingManager.registerSetting(strafe);
        SettingManager.registerSetting(spin);
        SettingManager.registerSetting(spinMode);
        SettingManager.registerSetting(spinSpeed);
        SettingManager.registerSetting(pitch);

        // Register message settings
        SettingManager.registerSetting(sendMessages);
        SettingManager.registerSetting(randomMessage);
        SettingManager.registerSetting(delay);
        SettingManager.registerSetting(chatmode);
    }

    private final Random random = new Random();
    private int messageTimer = 0;
    private int messageI = 0;
    private int sneakTimer = 0;
    private int strafeTimer = 0;
    private boolean direction = false;
    private float prevYaw;
    private boolean shouldRotate = false;

    String[] innocent = {
            "Hello Chat",
            "Meow",
            "What?",
            "他的名字是約翰·希南",
            "yawn....",
            ":)",
            ":c",
            "gg",
            "h",
            "y",
            "ez",
            "RG",
            "Hello World!",
            "zxcvbnmzxckvjlnzioufghi",
            "DDD",
            "bug",
            "chug",
            "grug",
            "rug"
    };

    String[] autism = {
            "( ͡° ͜ʖ ͡°)",
            "Meow",
            "The server has fallen, billions must grind.",
            "You are all weak NN dogs. I am the supreme sigma.",
            "f",
            "yawn....",
            ":)",
            "loveandlighttv",
            "他的名字是約翰·希南",
            "Yes, King!",
            "/whisper vsaw i moved the base to -420000, 690000",
            "Oh Lebron, my glorious king. Hush, be still. I will care for you.",
            "May i please have a glass of water? Please?",
            "They call me the Michigan Grim Reaper...",
            "The voices are telling me to touch grass. I refuse.",
            "silly rabbit, tricks are for kids.",
            "Real men meow unironically.",
            "Im sipping on promethazine, with lean i fell in love",
            "What if we kissed... in the 2b2t queue?",
            "I'm not hacking, I'm just utilizing unintended features.",
            "Queue skipping is a human right.",
            "Nerds are busy fighting for spawn, I'm building a Walmart at 1M 1M."
    };

    String[] payloadlover = {
            "I save money, I get my exploits with payload.technology",
            "Praise be the lord and savior Vertical Saw who blessed us with the holy water",
            "Only the worthy shall wield the power of Payload.",
            "Why fear anti-cheat when you have the blessings of Payload?",
            "Payload technology—turning mortals into gods, one exploit at a time.",
            "Thou shalt not question the supremacy of Payload.",
            "In Vertical Saw we trust, for he delivers us from patches.",
            "The anti-cheat weeps when Payload arrives.",
            "Payload doesn’t break the game, it perfects it. sometimes.",
            "One does not simply cheat—one ascends with Payload.",
            "With great cheats comes great responsibility… nah just kidding",
            "When the server says no, Payload says yes.",
            "Some say magic isn’t real. They haven’t seen Payload.",
            "Anti-cheat developers fear the name Vertical Saw.",
            "Payload isn’t just a tool, it’s a way of life.",
            "From the ashes of patches, Payload rises anew.",
            "Skill issue? No. Cheat issue. Fix it with Payload."
    };


    //TODO: Make multiple arrays of messages and a enum to select from a pool of these.

    List<String> innocentList = Arrays.stream(innocent).toList();
    List<String> autismList = Arrays.stream(autism).toList();
    List<String> payloadList = Arrays.stream(payloadlover).toList();

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);

        if (nullCheck()) return;

        if (strafe.get()) {
            MC.options.leftKey.setPressed(false);
            MC.options.rightKey.setPressed(false);
        }

        shouldRotate = false;
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);

        if (sendMessages.get() && getList().isEmpty()) {
            sendErrorMessage("Message list is empty, please send this error in the payload discord.");
            sendMessages.toggle();
        }


        if (nullCheck()) return;

        prevYaw = MC.player.getYaw();
        messageTimer = delay.get().intValue() * 20;
    }

    @Override
    public void onToggle() {
    }


    @Override
    public void onTick(TickEvent.Pre event) {
            if (nullCheck()) return;

            if (Payload.getInstance().guiManager == null) {
                return;
            }

            if (jump.get()) {
                if (MC.options.jumpKey.isPressed()) MC.options.jumpKey.setPressed(false);
                else if (random.nextInt(99) == 0) MC.options.jumpKey.setPressed(true);
            }

            if (swing.get() && random.nextInt(99) == 0) {
                MC.player.swingHand(MC.player.getActiveHand());
            }

            if (sneak.get()) {
                if (sneakTimer++ >= sneakTime.get()) {
                    MC.options.sneakKey.setPressed(false);
                    if (random.nextInt(99) == 0) sneakTimer = 0; // Sneak after ~5 seconds
                } else MC.options.sneakKey.setPressed(true);
            }

            if (strafe.get() && strafeTimer-- <= 0) {
                MC.options.leftKey.setPressed(!direction);
                MC.options.rightKey.setPressed(direction);
                direction = !direction;
                strafeTimer = 20;
            }

            if (spin.get()) {
                prevYaw += spinSpeed.get();
                switch (spinMode.get()) {
                    case Client -> MC.player.setYaw(prevYaw);
                    case Server -> shouldRotate = true;
                }
            }
            else {
                shouldRotate = false;
            }

            // Messages
            if (sendMessages.get() && !getList().isEmpty() && messageTimer-- <= 0) {
                if (randomMessage.get()) messageI = random.nextInt(getList().size());
                else if (++messageI >= getList().size()) messageI = 0;

                sendPlayerMsg(getList().get(messageI).toString());
                messageTimer = delay.get().intValue() * 20;
            }
        }

    public List<String> getList() {
        switch (chatmode.getValue()) {
            case ChatSetting.Innocent -> {
                return innocentList;
            }

            case ChatSetting.Autism -> {
                return autismList;
            }

            case ChatSetting.PayloadLover -> {
                return payloadList;
            }
        }

        return innocentList;
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onLook(LookAtEvent event) {
        if (shouldRotate) {
            event.setRotation(prevYaw, pitch.get(), 1, 2);
        }
    }

    public static void sendPlayerMsg(String message) {
        MC.inGameHud.getChatHud().addToMessageHistory(message);
        MC.player.networkHandler.sendChatMessage(message);
    }
}
package net.payload;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static net.payload.SoundGenerator.*;
import static net.payload.utils.render.TextureBank.payload;

public class Payload implements ModInitializer {
    public static PayloadClient instance;

    @Override
    public void onInitialize() {
        instance = new PayloadClient();
        instance.Initialize();

        Registry.register(Registries.SOUND_EVENT, HALOSPRINT_ID, HALOSPRINT_EVENT);
        Registry.register(Registries.SOUND_EVENT, HALOHURT_ID, HALOHURT_EVENT);
        Registry.register(Registries.SOUND_EVENT, HALOSHOOT_ID, HALOSHOOT_EVENT);
        Registry.register(Registries.SOUND_EVENT, HALOSHOOT2_ID, HALOSHOOT2_EVENT);

        Registry.register(Registries.SOUND_EVENT, MY_SOUND_ID, MY_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, MY_SOUND_ID1, MY_SOUND_EVENT1);
        Registry.register(Registries.SOUND_EVENT, NEWVEGAS1_ID, NEWVEGAS1_EVENT);
        Registry.register(Registries.SOUND_EVENT, ARMOR_ID, ARMOR_EVENT);
        Registry.register(Registries.SOUND_EVENT, ARMOR2_ID, ARMOR2_EVENT);
        Registry.register(Registries.SOUND_EVENT, ARMOR3_ID, ARMOR3_EVENT);
        Registry.register(Registries.SOUND_EVENT, ARMOR4_ID, ARMOR4_EVENT);
        Registry.register(Registries.SOUND_EVENT, ARMOR5_ID, ARMOR5_EVENT);
        Registry.register(Registries.SOUND_EVENT, BAKA_YOUDIDIT_ID, BAKA_YOUDIDIT_EVENT);
        Registry.register(Registries.SOUND_EVENT, BETTERLUCK_ID, BETTERLUCK_EVENT);
        Registry.register(Registries.SOUND_EVENT, BETTERLUCK2_ID, BETTERLUCK2_EVENT);
        Registry.register(Registries.SOUND_EVENT, BETTERLUCK3_ID, BETTERLUCK3_EVENT);
        Registry.register(Registries.SOUND_EVENT, BETTERLUCK4_ID, BETTERLUCK4_EVENT);
        Registry.register(Registries.SOUND_EVENT, BETTERLUCK5_ID, BETTERLUCK5_EVENT);
        Registry.register(Registries.SOUND_EVENT, BFF_ID, BFF_EVENT);
        Registry.register(Registries.SOUND_EVENT, BFF2_ID, BFF2_EVENT);
        Registry.register(Registries.SOUND_EVENT, BOO1_ID, BOO1_EVENT);
        Registry.register(Registries.SOUND_EVENT, BOO2_ID, BOO2_EVENT);
        Registry.register(Registries.SOUND_EVENT, BOO3_ID, BOO3_EVENT);
        Registry.register(Registries.SOUND_EVENT, BUZZ_ID, BUZZ_EVENT);
        Registry.register(Registries.SOUND_EVENT, BUZZ2_ID, BUZZ2_EVENT);
        Registry.register(Registries.SOUND_EVENT, CANIHAVEYOURSTASH_ID, CANIHAVEYOURSTASH_EVENT);
        Registry.register(Registries.SOUND_EVENT, CANIHAVEYOURSTASH2_ID, CANIHAVEYOURSTASH2_EVENT);
        Registry.register(Registries.SOUND_EVENT, CANIHAVEYOURSTASH3_ID, CANIHAVEYOURSTASH3_EVENT);
        Registry.register(Registries.SOUND_EVENT, DIAMOND2_ID, DIAMOND2_EVENT);
        Registry.register(Registries.SOUND_EVENT, DIAMONDHELMET_ID, DIAMONDHELMET_EVENT);
        Registry.register(Registries.SOUND_EVENT, DONKEYDUPE1_ID, DONKEYDUPE1_EVENT);
        Registry.register(Registries.SOUND_EVENT, DONKEYDUPE2_ID, DONKEYDUPE2_EVENT);
        Registry.register(Registries.SOUND_EVENT, DOYOULOVEMEBACK_ID, DOYOULOVEMEBACK_EVENT);
        Registry.register(Registries.SOUND_EVENT, DOYOULOVEMEBACK2_ID, DOYOULOVEMEBACK2_EVENT);
        Registry.register(Registries.SOUND_EVENT, DOYOULOVEMEBACK3_ID, DOYOULOVEMEBACK3_EVENT);
        Registry.register(Registries.SOUND_EVENT, DOYOULOVEMEBACK4_ID, DOYOULOVEMEBACK4_EVENT);
        Registry.register(Registries.SOUND_EVENT, EGG_ID, EGG_EVENT);
        Registry.register(Registries.SOUND_EVENT, EGG2_ID, EGG2_EVENT);
        Registry.register(Registries.SOUND_EVENT, EGG3_ID, EGG3_EVENT);
        Registry.register(Registries.SOUND_EVENT, HAUHHHH_ID, HAUHHHH_EVENT);
        Registry.register(Registries.SOUND_EVENT, HEADSHOT_ID, HEADSHOT_EVENT);
        Registry.register(Registries.SOUND_EVENT, HEADSHOT2_ID, HEADSHOT2_EVENT);
        Registry.register(Registries.SOUND_EVENT, HEADSHOT3_ID, HEADSHOT3_EVENT);
        Registry.register(Registries.SOUND_EVENT, HEADSHOT5_ID, HEADSHOT5_EVENT);
        Registry.register(Registries.SOUND_EVENT, HEADSHOT6_ID, HEADSHOT6_EVENT);
        Registry.register(Registries.SOUND_EVENT, HOIIIIIY_ID, HOIIIIIY_EVENT);
        Registry.register(Registries.SOUND_EVENT, HOYY_ID, HOYY_EVENT);
        Registry.register(Registries.SOUND_EVENT, HOYYYY_ID, HOYYYY_EVENT);
        Registry.register(Registries.SOUND_EVENT, HOYYYYYY_ID, HOYYYYYY_EVENT);
        Registry.register(Registries.SOUND_EVENT, IKNEWYOUCOULDDOIT_ID, IKNEWYOUCOULDDOIT_EVENT);
        Registry.register(Registries.SOUND_EVENT, IKNEWYOUCOULDDOIT2_ID, IKNEWYOUCOULDDOIT2_EVENT);
        Registry.register(Registries.SOUND_EVENT, IKNEWYOUCOULDDOIT3_ID, IKNEWYOUCOULDDOIT3_EVENT);
        Registry.register(Registries.SOUND_EVENT, IKNEWYOUCOULDDOIT4_ID, IKNEWYOUCOULDDOIT4_EVENT);
        Registry.register(Registries.SOUND_EVENT, LONGSMUG_LAUGH_ID, LONGSMUG_LAUGH_EVENT);
        Registry.register(Registries.SOUND_EVENT, MEANLAUGH_LONGEST_ID, MEANLAUGH_LONGEST_EVENT);
        Registry.register(Registries.SOUND_EVENT, OHYOUDIDIT_BEST_ID, OHYOUDIDIT_BEST_EVENT);
        Registry.register(Registries.SOUND_EVENT, OOPS_ID, OOPS_EVENT);
        Registry.register(Registries.SOUND_EVENT, OOPS2_ID, OOPS2_EVENT);
        Registry.register(Registries.SOUND_EVENT, OOPS3_ID, OOPS3_EVENT);
        Registry.register(Registries.SOUND_EVENT, OOPS4_ID, OOPS4_EVENT);
        Registry.register(Registries.SOUND_EVENT, OUCH_ID, OUCH_EVENT);
        Registry.register(Registries.SOUND_EVENT, OUCH2_ID, OUCH2_EVENT);
        Registry.register(Registries.SOUND_EVENT, OUCH3_ID, OUCH3_EVENT);
        Registry.register(Registries.SOUND_EVENT, OUCH4_ID, OUCH4_EVENT);
        Registry.register(Registries.SOUND_EVENT, OUCH5_ID, OUCH5_EVENT);
        Registry.register(Registries.SOUND_EVENT, PAYLOAD_ID, PAYLOAD_EVENT);
        Registry.register(Registries.SOUND_EVENT, PAYLOAD2_ID, PAYLOAD2_EVENT);
        Registry.register(Registries.SOUND_EVENT, PAYLOAD3_ID, PAYLOAD3_EVENT);
        Registry.register(Registries.SOUND_EVENT, PAYLOAD5_ID, PAYLOAD5_EVENT);
        Registry.register(Registries.SOUND_EVENT, PAYLOAD6_ID, PAYLOAD6_EVENT);
        Registry.register(Registries.SOUND_EVENT, PAYLOAD7_ID, PAYLOAD7_EVENT);
        Registry.register(Registries.SOUND_EVENT, PINGCARRIED_ID, PINGCARRIED_EVENT);
        Registry.register(Registries.SOUND_EVENT, PLANB_ID, PLANB_EVENT);
        Registry.register(Registries.SOUND_EVENT, PLANB2_ID, PLANB2_EVENT);
        Registry.register(Registries.SOUND_EVENT, PLANB3_ID, PLANB3_EVENT);
        Registry.register(Registries.SOUND_EVENT, SCREAM1_ID, SCREAM1_EVENT);
        Registry.register(Registries.SOUND_EVENT, SCREAM2_ID, SCREAM2_EVENT);
        Registry.register(Registries.SOUND_EVENT, SCREAM3_ID, SCREAM3_EVENT);
        Registry.register(Registries.SOUND_EVENT, SHH_ID, SHH_EVENT);
        Registry.register(Registries.SOUND_EVENT, SHH2_ID, SHH2_EVENT);
        Registry.register(Registries.SOUND_EVENT, SKEETSKET_ID, SKEETSKET_EVENT);
        Registry.register(Registries.SOUND_EVENT, SKEETSKET2_ID, SKEETSKET2_EVENT);
        Registry.register(Registries.SOUND_EVENT, SKYRIM1_ID, SKYRIM1_EVENT);
        Registry.register(Registries.SOUND_EVENT, SMUG_LAUGH_ID, SMUG_LAUGH_EVENT);
        Registry.register(Registries.SOUND_EVENT, SMUG_LAUGH2_ID, SMUG_LAUGH2_EVENT);
        Registry.register(Registries.SOUND_EVENT, THESEMODULESUSEDTO_ID, THESEMODULESUSEDTO_EVENT);
        Registry.register(Registries.SOUND_EVENT, TIMETORUN_ID, TIMETORUN_EVENT);
        Registry.register(Registries.SOUND_EVENT, TIMETORUN2_ID, TIMETORUN2_EVENT);
        Registry.register(Registries.SOUND_EVENT, TIMETORUN3_ID, TIMETORUN3_EVENT);
        Registry.register(Registries.SOUND_EVENT, TOTEM_ID, TOTEM_EVENT);
        Registry.register(Registries.SOUND_EVENT, WHIMPER_ID, WHIMPER_EVENT);
        Registry.register(Registries.SOUND_EVENT, YOUDIDIT_ID, YOUDIDIT_EVENT);
        Registry.register(Registries.SOUND_EVENT, YUMMY_ID, YUMMY_EVENT);
        Registry.register(Registries.SOUND_EVENT, YUMMY2_ID, YUMMY2_EVENT);
        Registry.register(Registries.SOUND_EVENT, YUMMY3_ID, YUMMY3_EVENT);
    }

    public static PayloadClient getInstance() {
        return instance;
    }
}

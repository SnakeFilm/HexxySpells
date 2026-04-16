package ru.snake_film.hexxyspells;

import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import ru.snake_film.hexxyspells.iota.StringIota;
import ru.snake_film.hexxyspells.patterns.IronPatterns;
import thedarkcolour.kotlinforforge.KotlinModLoadingContext;

@Mod(HexxySpells.MODID)
public class HexxySpells {
    public static final String MODID = "hexxyspells";

    public HexxySpells() {
        IEventBus modBus = KotlinModLoadingContext.Companion.get().getKEventBus();


        modBus.addListener(this::onRegister);

    }

    public void onRegister(RegisterEvent event) {

        if (event.getRegistryKey().equals(HexRegistries.ACTION)) {
            IronPatterns.INSTANCE.registerPatterns((entry, id) ->
                    event.register(HexRegistries.ACTION, id, () -> entry)
            );
        }


        if (event.getRegistryKey().equals(HexRegistries.IOTA_TYPE)) {
            event.register(HexRegistries.IOTA_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MODID, "string_iota"),
                    () -> StringIota.TYPE
            );
            }
        }}

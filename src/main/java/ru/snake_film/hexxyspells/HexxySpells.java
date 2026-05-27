package ru.snake_film.hexxyspells;


import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import ru.snake_film.hexxyspells.elements.ElementalPatternIota;
import ru.snake_film.hexxyspells.hud.EntityFlyingPattern;
import ru.snake_film.hexxyspells.hud.RenderFlyingPattern;
import ru.snake_film.hexxyspells.iota.StringIota;
import ru.snake_film.hexxyspells.patterns.IronPatterns;
import thedarkcolour.kotlinforforge.KotlinModLoadingContext;
import ru.snake_film.hexxyspells.ModPackets;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.List;

@Mod(HexxySpells.MODID)
public class HexxySpells {
    public static final String MODID = "hexxyspells";

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final KeyMapping CAST_KEY = new KeyMapping(
            "key.hexxyspells.cast",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.hexxyspells"
    );

    // 2. Объявляем саму сущность
    public static final RegistryObject<EntityType<EntityFlyingPattern>> FLYING_PATTERN =
            ENTITIES.register("flying_pattern", () -> EntityType.Builder.<EntityFlyingPattern>of(EntityFlyingPattern::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build("flying_pattern"));
    public static final RegistryObject<EntityType<EntityFlyingPattern>> CONTINUOUS_ENTITY =
            ENTITIES.register("continuous_entity", () -> EntityType.Builder.<EntityFlyingPattern>of(EntityFlyingPattern::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(64)
                    .build("continuous_entity"));

    public HexxySpells() {


        ModPackets.INSTANCE.register();

        IEventBus modBus = KotlinModLoadingContext.Companion.get().getKEventBus();


        // 3. Регистрация сущностей
        ENTITIES.register(modBus);
        ModParticles.PARTICLE_TYPES.register(modBus);

        // 4. Слушатели для регистраций (одного раза достаточно)
        modBus.addListener(this::onRegister);

        // 5. Рендеринг (Клиентская часть)
        modBus.addListener(this::registerEntityRenderers);
        modBus.addListener(this::registerKeys);

        // 6. Команды (на общей шине)
        MinecraftForge.EVENT_BUS.addListener(this::onCommandsRegister);




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
        if (event.getRegistryKey().equals(HexRegistries.IOTA_TYPE)) {
            event.register(HexRegistries.IOTA_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MODID, "elemental_pattern"),
                    () -> ElementalPatternIota.Companion.getTYPE()
            );
        }
        }




    private void onCommandsRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Вот здесь мы говорим игре:
        // "Для сущности FLYING_PATTERN используй класс RenderFlyingPattern"
        event.registerEntityRenderer(FLYING_PATTERN.get(), RenderFlyingPattern::new);
    }
    private void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CAST_KEY);
    }


    }

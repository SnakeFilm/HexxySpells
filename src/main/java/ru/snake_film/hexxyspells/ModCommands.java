package ru.snake_film.hexxyspells;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import ru.snake_film.hexxyspells.HexxySpells;
import ru.snake_film.hexxyspells.hud.EntityFlyingPattern;
import java.util.List;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hexhud")
                .then(Commands.literal("test")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ServerLevel world = player.serverLevel();


                            EntityFlyingPattern patternEntity = new EntityFlyingPattern(HexxySpells.FLYING_PATTERN.get(), world);
                            patternEntity.setOwner(player.getUUID()); // ИСПОЛЬЗУЙ ЭТО
                            patternEntity.setPos(player.getX(), player.getY() + 1.5, player.getZ());

                            HexPattern pattern = HexPattern.fromAngles("aqadwe", HexDir.NORTH_EAST);
                            patternEntity.setPatterns(List.of(new PatternIota(pattern)));

                            world.addFreshEntity(patternEntity);
                            patternEntity.setPos(player.getX(), player.getY(), player.getZ());
                            world.addFreshEntity(patternEntity);

                            context.getSource().sendSuccess(() -> Component.literal("Магия запущена!"), false);
                            return 1;
                        })
                )
        );
    }
}
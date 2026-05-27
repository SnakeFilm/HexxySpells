package ru.snake_film.hexxyspells.elements.elemental_mixins;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.snake_film.hexxyspells.elements.ElementalPatternIota;
import ru.snake_film.hexxyspells.elements.ElementalCastingManager;

@Mixin(CastingVM.class)
public class MixinPatternCapture {

    @Shadow
    @Final
    private CastingEnvironment env; // Достаем окружение каста

    @ModifyVariable(method = "queueExecuteAndWrapIota", at = @At("HEAD"), argsOnly = true, remap = false)
    private Iota wrapSingleIota(Iota iota) {
        // Проверяем, есть ли игрок в текущем окружении
        LivingEntity caster = env.getCaster();
        if (caster instanceof Player player) {
            String currentElement = player.getPersistentData().getString("hexxyspells:current_element");

            if (currentElement != null && !currentElement.isEmpty() && !currentElement.equals("spirit")) {
                if (iota instanceof PatternIota patIota && !(patIota instanceof ElementalPatternIota)) {
                    return new ElementalPatternIota(patIota.getPattern(), currentElement);
                }
            }
        }
        return iota;
    }
}
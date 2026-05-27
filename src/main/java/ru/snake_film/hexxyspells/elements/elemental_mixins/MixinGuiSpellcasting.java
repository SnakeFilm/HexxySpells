package ru.snake_film.hexxyspells.elements.elemental_mixins;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.snake_film.hexxyspells.elements.ElementalCastingManager;
import ru.snake_film.hexxyspells.ModPackets;
import ru.snake_film.hexxyspells.ElementSelectPacket;

@Mixin(GuiSpellcasting.class)
public abstract class MixinGuiSpellcasting extends Screen {

    protected MixinGuiSpellcasting(Component title) { super(title); }

    @Inject(method = "init", at = @At("RETURN"))
    private void addElementalButtons(CallbackInfo ci) {
        String[] elements = {"Spirit", "Fire", "Water", "Earth", "Air", "Dark"};
        int startY = 20;

        for (int i = 0; i < elements.length; i++) {
            final String element = elements[i].toLowerCase();
            int y = startY + (i * 25);


            this.addRenderableWidget(Button.builder(Component.literal(elements[i]), (btn) -> {
                // 1. Клиентское обновление (для цвета и яркости)
                ElementalCastingManager.INSTANCE.setElement(element);

                // 2. Отправка пакета на сервер (для сохранения в игрока и впрыска в список)
                ModPackets.CHANNEL.sendToServer(new ElementSelectPacket(element));

            }).bounds(this.width - 60, y, 20, 10).build());
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void drawPanelLabel(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        graphics.drawString(this.font, "Elements:", this.width - 60, 5, 0xFFFFFF);

        // Тут можно будет добавить логику отрисовки рамки вокруг активной кнопки,
        // основываясь на ElementalCastingManager.INSTANCE.getElement()
    }
}
package ru.snake_film.hexxyspells.iota;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringIota extends Iota {
    // Тип нашей иоты (нужно будет зарегистрировать)
    public static final IotaType<StringIota> TYPE = new Type();

    public StringIota(@NotNull String payload) {
        super(TYPE, payload);
    }

    public String getString() {
        return (String) this.payload;
    }

    @Override
    public boolean isTruthy() {
        return !this.getString().isEmpty();
    }

    @Override
    public boolean toleratesOther(@NotNull Iota that) {
        return that instanceof StringIota other && other.getString().equals(this.getString());
    }

    @Override
    public @NotNull Tag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("string_value", this.getString());
        return tag;
    }
    public static int getSchoolColor(String school) {
        return switch (school.toLowerCase()) {
            case "fire" -> 0xff_e86500;    // Красный
            case "ice" -> 0xff_55ffff;     // Голубой
            case "blood" -> 0xff_aa0000;   // Темно-красный
            case "ender" -> 0xff_aa00ff;   // Фиолетовый
            case "holy" -> 0xff_ffff55;    // Желтый
            case "lightning" -> 0xff_5555ff; // Синий
            case "nature" -> 0xff_55ff55;  // Зеленый
            case "evocation" -> 0xff_5500aa; // Пурпурный
            default -> 0xff_ffffff;        // Белый
        };
    }

    // Класс-десериализатор (как превратить NBT обратно в объект)
    private static class Type extends IotaType<StringIota> {
        @Nullable
        @Override
        public StringIota deserialize(Tag tag, ServerLevel world) {
            if (tag instanceof CompoundTag ct) {
                return new StringIota(ct.getString("string_value"));
            }
            return null;
        }

        @Override
        public Component display(Tag tag) {
            if (tag instanceof CompoundTag ct) {
                String value = ct.getString("string_value");
                int color = getSchoolColor(value); // Получаем цвет по строке

                // Создаем текст и красим его в нужный цвет
                return Component.literal("\"" + value + "\"")
                        .withStyle(style -> style.withColor(color));
            }
            return Component.literal("\"\"");
        }

        @Override
        public int color() {
            // Цвет самого типа иоты (например, в сетке паттернов)
            return 0xff_55ff55;
        }

        // Вспомогательный метод для выбора цвета

    }}
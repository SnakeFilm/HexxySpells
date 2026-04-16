package ru.snake_film.hexxyspells.patterns

import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions
import net.minecraft.resources.ResourceLocation
import ru.snake_film.hexxyspells.spells.projectile
import java.util.function.BiConsumer
import ru.snake_film.hexxyspells.iota.StringIota
import kotlin.collections.listOf

object IronPatterns {
    fun registerPatterns(r: BiConsumer<ActionRegistryEntry, ResourceLocation>) {
        // 1. Создаем "запись" для реестра
        val projectilePattern = ActionRegistryEntry(
            HexPattern.fromAngles("eaqa", HexDir.NORTH_EAST),
            projectile
        )

        // 2. Скармливаем её регистратору под уникальным ID
        r.accept(projectilePattern, ResourceLocation.fromNamespaceAndPath("hexxyspells", "projectile"))

        //SCHOOL PATTERNS
        // FIRE
        val schoolFireEntry = ActionRegistryEntry(
            HexPattern.fromAngles("wawa", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("fire")))
        r.accept(schoolFireEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolfire"))

// ICE
        val schoolIceEntry = ActionRegistryEntry(
            HexPattern.fromAngles("dwdw", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("ice")))
        r.accept(schoolIceEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolice")) // Исправлено

// BLOOD
        val schoolBloodEntry = ActionRegistryEntry(
            HexPattern.fromAngles("dwdwde", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("blood")))
        r.accept(schoolBloodEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolblood")) // Исправлено

// HOLY
        val schoolHolyEntry = ActionRegistryEntry(
            HexPattern.fromAngles("qawawa", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("holy")))
        r.accept(schoolHolyEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolholy")) // Исправлено

// ENDER
        val schoolEnderEntry = ActionRegistryEntry(
            HexPattern.fromAngles("qaqqqqqwqqq", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("ender")))
        r.accept(schoolEnderEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolender")) // Исправлено

// LIGHTNING
        val schoolLightningEntry = ActionRegistryEntry(
            HexPattern.fromAngles("wdea", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("lightning")))
        r.accept(schoolLightningEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoollightning")) // Исправлено

// NATURE
        val schoolNatureEntry = ActionRegistryEntry(
            HexPattern.fromAngles("deedqaa", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("nature")))
        r.accept(schoolNatureEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolnature")) // Исправлено

// EVOCATION
        val schoolEvocationEntry = ActionRegistryEntry(
            HexPattern.fromAngles("qaqqqqqwqqqeaeaeaeadaeaeaea", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("evocation")))
        r.accept(schoolEvocationEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolevocation")) // Исправлено
    }

    }







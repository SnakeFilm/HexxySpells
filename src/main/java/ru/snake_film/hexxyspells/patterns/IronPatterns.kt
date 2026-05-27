package ru.snake_film.hexxyspells.patterns

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.resources.ResourceLocation
import ru.snake_film.hexxyspells.elements.ElementalAction

import java.util.function.BiConsumer
import ru.snake_film.hexxyspells.iota.StringIota
import ru.snake_film.hexxyspells.spells.actions.pointAction
import ru.snake_film.hexxyspells.spells.actions.ActionSpawnFlyingPattern
import ru.snake_film.hexxyspells.spells.actions.ContinuousAction
import ru.snake_film.hexxyspells.spells.actions.InstantAction
import ru.snake_film.hexxyspells.spells.actions.projectile

object IronPatterns {
    fun registerPatterns(r: BiConsumer<ActionRegistryEntry, ResourceLocation>) {
        // 1. Создаем "запись" для реестра
        val projectilePattern = ActionRegistryEntry(
            HexPattern.fromAngles("eaqa", HexDir.EAST),
            projectile
        )

        // 2. Скармливаем её регистратору под уникальным ID
        r.accept(projectilePattern, ResourceLocation.fromNamespaceAndPath("hexxyspells", "projectile"))

        val PointActionPattern = ActionRegistryEntry(
            HexPattern.fromAngles("qded", HexDir.EAST),
            pointAction
        )


        r.accept(PointActionPattern, ResourceLocation.fromNamespaceAndPath("hexxyspells", "pointaction"))

        val ContinuousActionPattern = ActionRegistryEntry(
            HexPattern.fromAngles("aqa", HexDir.NORTH_EAST),
            ContinuousAction
        )


        r.accept(ContinuousActionPattern, ResourceLocation.fromNamespaceAndPath("hexxyspells", "continuousaction"))

        val InstantActionPattern = ActionRegistryEntry(
            HexPattern.fromAngles("ded", HexDir.SOUTH_EAST),
            InstantAction
        )


        r.accept(InstantActionPattern, ResourceLocation.fromNamespaceAndPath("hexxyspells", "entityaction"))


        //SCHOOL PATTERNS
        // FIRE
        val OldschoolFireEntry = ActionRegistryEntry(
            HexPattern.fromAngles("wawa", HexDir.SOUTH_WEST),
            Action.makeConstantOp(StringIota("oldfire")))
        r.accept(OldschoolFireEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "oldschoolfire"))

// ICE
        val schoolIceEntry = ActionRegistryEntry(
            HexPattern.fromAngles("dwdw", HexDir.SOUTH_EAST),
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
            HexPattern.fromAngles("qaqqqqqwqqqeaeaeaeadaeaeae", HexDir.NORTH_EAST),
            Action.makeConstantOp(StringIota("evocation")))
        r.accept(schoolEvocationEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolevocation")) // Исправлено


        val spawnPatternEntry = ActionRegistryEntry(
            HexPattern.fromAngles("deaqqq", HexDir.SOUTH_EAST), // Твой паттерн
            ActionSpawnFlyingPattern
        )
        r.accept(spawnPatternEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "spawn_flying_pattern"))


    //ELEMENTS!!!
    // SPIRIT
    val schoolSpiritEntry = ActionRegistryEntry(
        HexPattern.fromAngles("waqqqqqwaeaeaeaeaea", HexDir.SOUTH_EAST),
        ElementalAction("spirit")
    )
    r.accept(spawnPatternEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolspirit"))

    // FIRE
    val schoolFireEntry = ActionRegistryEntry(
        HexPattern.fromAngles("wqqqqa", HexDir.SOUTH_EAST),
        ElementalAction("fire")
    )
    r.accept(schoolFireEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolfire"))

    // WATER
    val schoolWaterEntry = ActionRegistryEntry(
        HexPattern.fromAngles("wqqqqw", HexDir.NORTH_WEST),
        ElementalAction("water")
    )
    r.accept(schoolWaterEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolwater"))

    // EARTH
    val schoolEarthEntry = ActionRegistryEntry(
        HexPattern.fromAngles("aqawwawwaw", HexDir.SOUTH_WEST),
        ElementalAction("earth")
    )
    r.accept(schoolEarthEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolearth"))

    // AIR
    val schoolAirEntry = ActionRegistryEntry(
        HexPattern.fromAngles("aqqqqwqq", HexDir.NORTH_EAST),
        ElementalAction("air")
    )
    r.accept(schoolAirEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schoolair"))

    // DARK
    val schoolDarkEntry = ActionRegistryEntry(
        HexPattern.fromAngles("qaqqqqqwqqqeaeaeaeadaeaeaea", HexDir.EAST),
        ElementalAction("dark")
    )
    r.accept(schoolDarkEntry, ResourceLocation.fromNamespaceAndPath("hexxyspells", "schooldark"))

}}







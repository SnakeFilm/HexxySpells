package ru.snake_film.hexxyspells.spells

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import io.redspace.ironsspellbooks.api.spells.SchoolType
import net.minecraft.world.entity.projectile.Projectile
import ru.snake_film.hexxyspells.spells.utils_and_entities.BeamUtils
import kotlin.math.pow

interface IElementalSchool {

    // Снаряды (Projectile)
    fun createProjectile(index: Int, world: Level, caster: LivingEntity, damage: Double): Projectile?

    // Точечные эффекты (Point)
    fun createPointEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, damage: Double)

    // Потоковые/длительные эффекты (Continuous)
    fun createContinuousEffect(index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double, duration: Int)
    // Призыв существ (Entity)
    fun createInstantEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, power: Double)

    fun performBeamHit(world: Level, caster: LivingEntity, target: Entity, power: Double)

    // Возвращает школу из Iron's для расчета бонусов экипировки
    fun getIronSchool(): SchoolType
    fun getManaCost(power: Double, index: Int): Float
    fun getMediaCost(power: Double, index: Int): Long {
        return (power.pow(2).toLong())*0.5.toLong()
    }
    fun getBehavior(index: Int): BeamUtils.BeamType
}
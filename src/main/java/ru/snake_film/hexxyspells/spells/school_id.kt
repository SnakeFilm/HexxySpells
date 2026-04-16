package ru.snake_film.hexxyspells.spells

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile
import io.redspace.ironsspellbooks.entity.spells.electrocute.ElectrocuteProjectile
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb
import io.redspace.ironsspellbooks.entity.spells.small_magic_arrow.SmallMagicArrow
import io.redspace.ironsspellbooks.registries.EntityRegistry
import net.minecraft.world.entity.*
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.Level

object school_id {
    fun createProjectile(schoolId: Int, spellIndex: Int, world: Level, caster: LivingEntity, damage: Double): Projectile? {
        val dmg = damage.toFloat()

        // Функция быстрой настройки для стандартных кастов
        fun setup(p: Entity?): Projectile? {
            if (p is AbstractMagicProjectile) {
                p.owner = caster
                p.damage = dmg
            }
            return p as? Projectile
        }

        return when (schoolId) {
            1 -> when (spellIndex) { // FIRE
                1 -> setup(EntityRegistry.FIREBOLT_PROJECTILE.get().create(world))
                2 -> MagicFireball(world, caster).apply {
                    this.damage = dmg
                    this.explosionRadius = (dmg / 3f).coerceAtLeast(1f)
                }
                3 -> FireBomb(world, caster).apply {
                    this.damage = dmg
                    this.explosionRadius = (dmg / 4f).coerceAtLeast(1f)
                }
                4 -> setup(EntityRegistry.FIRE_ARROW_PROJECTILE.get().create(world))

                else -> null
            }

            2 -> when (spellIndex) { // ICE
                1 -> setup(EntityRegistry.ICICLE_PROJECTILE.get().create(world))
                2 -> setup(EntityRegistry.SNOWBALL.get().create(world))
                3 -> IceBlockProjectile(EntityRegistry.ICE_BLOCK_PROJECTILE.get(), world).apply {
                    owner = caster // Устанавливаем владельца отдельно, так как его нет в конструкторе
                }
                4 -> setup(EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get().create(world))

                else -> null
            }

            3 -> when (spellIndex) { // BLOOD
                1 -> setup(EntityRegistry.BLOOD_NEEDLE.get().create(world))
                2 -> setup(EntityRegistry.WITHER_SKULL_PROJECTILE.get().create(world))
                3 -> setup(EntityRegistry.BLOOD_SLASH_PROJECTILE.get().create(world))

                else -> null
            }

            4 -> when (spellIndex) { // ENDER
                1 -> setup(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get().create(world))
                2 -> setup(EntityRegistry.MAGIC_ARROW_PROJECTILE.get().create(world))
                3 -> setup(EntityRegistry.SMALL_MAGIC_ARROW.get().create(world))
                else -> null
            }

            5 -> when (spellIndex) { // HOLY
                1 -> setup(EntityRegistry.GUIDING_BOLT.get().create(world))
                2 -> {
                    val wisp = EntityRegistry.WISP.get().create(world)
                    if (wisp is io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity) {
                        wisp.setOwner(caster)
                        wisp.level()
                    }
                    wisp as? Projectile
                }
                else -> null
            }

            6 -> when (spellIndex) { // LIGHTNING

                1 -> setup(EntityRegistry.LIGHTNING_LANCE_PROJECTILE.get().create(world))
               // DO NOT WORK 3 -> setup(EntityRegistry.LIGHTNING_STRIKE.get().create(world))
                2 -> setup(EntityRegistry.BALL_LIGHTNING.get().create(world))
                //CRASING MC 5 -> setup(EntityRegistry.CHAIN_LIGHTNING.get().create(world))


                else -> null
            }

            7 -> when (spellIndex) { // NATURE
                1 -> setup(EntityRegistry.ACID_ORB.get().create(world))
                2 -> setup(EntityRegistry.POISON_ARROW.get().create(world))
                else -> null
            }

            8 -> when (spellIndex) { // EVOCATION
                1 -> setup(EntityRegistry.CREEPER_HEAD_PROJECTILE.get().create(world))
                else -> null
            }

            else -> null
        }
    }
}
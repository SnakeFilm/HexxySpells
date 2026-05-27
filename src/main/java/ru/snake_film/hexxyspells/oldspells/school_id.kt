package ru.snake_film.hexxyspells.oldspells

import io.redspace.ironsspellbooks.api.magic.MagicData
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb
import io.redspace.ironsspellbooks.registries.EntityRegistry
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

object school_id {

        private const val MEDIA_TO_MANA_RATIO = 1000.0

        // Централизованный метод траты маны
        fun consumeMana(player: ServerPlayer, amount: Float): Boolean {
            val magicData = MagicData.getPlayerMagicData(player)
            if (magicData.mana < amount) return false
            magicData.mana -= amount
            return true
        }

        // Хелпер для установки владельца (решает проблему с засчитыванием фрагов)[cite: 34]
        fun applyOwner(entity: Entity?, caster: LivingEntity) {
            if (entity == null) return
            // Используем явные методы вместо присваивания через точку, чтобы избежать ошибок val
            if (entity is Projectile) entity.owner = caster
            if (entity is OwnableEntity) {
                // В некоторых версиях интерфейса может не быть сеттера, пробуем общие методы
                if (entity is Mob) entity.target = null
            }
        }

        private fun <T : Entity> createAndPos(type: EntityType<T>, world: Level, pos: Vec3): T? {
            val entity = type.create(world)
            entity?.moveTo(pos.x, pos.y, pos.z)
            return entity
        }


        fun createProjectile(
            schoolId: Int,
            spellIndex: Int,
            world: Level,
            caster: LivingEntity,
            damage: Double
        ): Projectile? {
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
                    5 -> setup(EntityRegistry.FIERY_DAGGER_PROJECTILE.get().create(world))

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


        //POINT ACTIONS
        fun createPointEffect(
            schoolId: Int,
            spellIndex: Int,
            world: Level,
            caster: LivingEntity,
            pos: Vec3,
            damage: Double
        ) {
            val dmg = damage.toFloat()
            when (schoolId) {
                1 -> when (spellIndex) { // FIRE
                    1 -> world.explode(caster, pos.x, pos.y, pos.z, dmg / 2f, Level.ExplosionInteraction.MOB)
                    2 -> createAndPos(
                        EntityRegistry.FIRE_ERUPTION_AOE.get(),
                        world,
                        pos
                    )?.let { world.addFreshEntity(it) }

                    3 -> createAndPos(EntityRegistry.WALL_OF_FIRE_ENTITY.get(), world, pos)?.let {
                        world.addFreshEntity(
                            it
                        )
                    }
                }

                2 -> when (spellIndex) { // ICE
                    1 -> createAndPos(EntityRegistry.ICE_SPIKE.get(), world, pos)?.let { world.addFreshEntity(it) }
                    2 -> createAndPos(EntityRegistry.FROST_FIELD.get(), world, pos)?.let { world.addFreshEntity(it) }
                }

                5 -> when (spellIndex) { // HOLY
                    1 -> createAndPos(EntityRegistry.SUNBEAM.get(), world, pos)?.let { world.addFreshEntity(it) }
                    2 -> createAndPos(EntityRegistry.HEALING_AOE.get(), world, pos)?.let { world.addFreshEntity(it) }
                }

                6 -> if (spellIndex == 1) { // LIGHTNING
                    createAndPos(EntityRegistry.LIGHTNING_STRIKE.get(), world, pos)?.let {
                        it.damage = dmg
                        world.addFreshEntity(it)
                    }
                }

                7 -> if (spellIndex == 1) { // NATURE
                    createAndPos(EntityRegistry.EARTHQUAKE_AOE.get(), world, pos)?.let { world.addFreshEntity(it) }
                }
            }
        }

        fun createEntityEffect(school: String, index: Int, world: Level, caster: LivingEntity, pos: Vec3, power: Double) {
            val entity = when (school) {
                "evocation" -> if (index == 1) createAndPos(EntityRegistry.SUMMONED_VEX.get(), world, pos) else null
                "ender" -> if (index == 1) createAndPos(EntityRegistry.SUMMONED_SWORD.get(), world, pos) else null
                else -> null
            }
            entity?.let {
                applyOwner(it, caster)
                if (it is LivingEntity) {
                    // ПРИМЕР: Мощь увеличивает здоровье и урон призванного существа
                    it.getAttribute(Attributes.MAX_HEALTH)?.baseValue = 4.0 * power
                    it.health = it.maxHealth
                    it.getAttribute(Attributes.ATTACK_DAMAGE)?.baseValue = 1.0 * power
                }
                world.addFreshEntity(it)
            }
        }

        // CONTINUOUS: Исправляем невидимость и урон (Землетрясение и др.)
        fun createContinuousEffect(school: String, index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double) {
            val pos = caster.eyePosition.add(caster.lookAngle.scale(0.5))

            // Для AOE (Землетрясение) нужно создавать его на земле под точкой
            val entity = when (school) {
                "fire" -> createAndPos(EntityRegistry.FIRE_BREATH_PROJECTILE.get(), world, pos)
                "nature" ->  { // Землетрясение
                    createAndPos(EntityRegistry.EARTHQUAKE_AOE.get(), world, pos).apply {
                        this?.radius = (2f * power).toFloat()
                        this?.damage = (power).toFloat()
                        this?.duration = 10 // Даем 10 тиков жизни, чтобы он успел сработать[cite: 30]
                    }
                }
                else -> null
            }

            entity?.let {
                applyOwner(it, caster)
                if (it is Projectile) it.shoot(direction.x, direction.y, direction.z, 1.0f, 0f)
                if (it is AbstractMagicProjectile) it.damage = power.toFloat()

                world.addFreshEntity(it)
            }
        }
    }


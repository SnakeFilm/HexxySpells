package ru.snake_film.hexxyspells.spells.utils_and_entities

import io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.ElementalRegistry
import ru.snake_film.hexxyspells.spells.ManaMediaCost

class ChannelingEffectEntity(
    type: EntityType<*>,
    world: Level
) : Entity(type, world) {

    constructor(
        world: Level,
        caster: LivingEntity,
        elementId: String,
        power: Double,
        duration: Int,
        spellIndex: Int,
        behavior: BeamUtils.BeamType
    ) : this(EntityType.MARKER, world) {
        this.caster = caster
        this.elementId = elementId
        this.power = power
        this.duration = duration
        this.spellIndex = spellIndex
        this.behavior = behavior
    }

    var caster: LivingEntity? = null
    var elementId: String = "fire"
    var power: Double = 1.0
    var duration: Int = 100
    var spellIndex: Int = 1
    var behavior: BeamUtils.BeamType = BeamUtils.BeamType.BEAM

    var visualEntity: Entity? = null
    var initialSlot: Int = -1

    companion object {
        val VISUAL_ENTITY_STR: EntityDataAccessor<String> = SynchedEntityData.defineId(
            ChannelingEffectEntity::class.java, EntityDataSerializers.STRING
        )
        val PARTICLE_TYPE_STR: EntityDataAccessor<String> = SynchedEntityData.defineId(
            ChannelingEffectEntity::class.java, EntityDataSerializers.STRING
        )
        val SOUND_EVENT_STR: EntityDataAccessor<String> = SynchedEntityData.defineId(
            ChannelingEffectEntity::class.java, EntityDataSerializers.STRING
        )
        val SLOWDOWN_UUID: java.util.UUID = java.util.UUID.fromString("7a4b8c9d-1234-5678-9abc-def012345678")
    }

    override fun defineSynchedData() {
        this.entityData.define(VISUAL_ENTITY_STR, "")
        this.entityData.define(PARTICLE_TYPE_STR, "")
        this.entityData.define(SOUND_EVENT_STR, "")
    }

    fun setVisualEntityType(type: EntityType<*>?) {
        if (type != null) {
            val registryName = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()
            this.entityData.set(VISUAL_ENTITY_STR, registryName)
        }
    }

    fun setParticleType(type: ParticleOptions?) {
        if (type != null) {
            val registryName = BuiltInRegistries.PARTICLE_TYPE.getKey(type.type).toString()
            this.entityData.set(PARTICLE_TYPE_STR, registryName)
        }
    }

    fun getVisualEntityType(): EntityType<*>? {
        val name = this.entityData.get(VISUAL_ENTITY_STR)
        if (name.isEmpty()) return null
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation(name))
    }

    fun getParticleType(): ParticleOptions? {
        val name = this.entityData.get(PARTICLE_TYPE_STR)
        if (name.isEmpty()) return null
        val particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation(name))
        return particleType as? ParticleOptions
    }

    fun setSoundEvent(sound: net.minecraft.sounds.SoundEvent?) {
        if (sound != null) {
            val registryName = BuiltInRegistries.SOUND_EVENT.getKey(sound).toString()
            this.entityData.set(SOUND_EVENT_STR, registryName)
        }
    }

    fun getSoundEvent(): net.minecraft.sounds.SoundEvent? {
        val name = this.entityData.get(SOUND_EVENT_STR)
        if (name.isEmpty()) return null
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation(name))
    }

    override fun tick() {
        super.tick()

        val currentCaster = caster ?: return

        // 1. БАЗОВЫЕ ПРЕРЫВАНИЯ (Работают везде)
        if (!currentCaster.isAlive || duration <= 0) {
            removeVisualAndDiscard()
            return
        }

        this.setPos(currentCaster.x, currentCaster.y, currentCaster.z)
        val school = ElementalRegistry.get(elementId)

        // 2. СЕРВЕРНАЯ КОРРЕКТИРОВКА СОСТОЯНИЙ И МАНЫ
        if (!level().isClientSide) {
            if (currentCaster is ServerPlayer) {
                if (this.initialSlot == -1) {
                    this.initialSlot = currentCaster.inventory.selected
                } else if (currentCaster.inventory.selected != this.initialSlot) {
                    removeVisualAndDiscard()
                    return
                }

                if (currentCaster.containerMenu != currentCaster.inventoryMenu) {
                    removeVisualAndDiscard()
                    return
                }

                if (!ManaMediaCost.checkAndConsume(currentCaster, ManaMediaCost.TYPE_CONTINUOUS, power, spellIndex)) {
                    removeVisualAndDiscard()
                    return
                }
            }

            // Накладываем замедление на первом тике
            if (tickCount == 1) {
                val speedAttribute =
                    currentCaster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                if (speedAttribute != null && speedAttribute.getModifier(SLOWDOWN_UUID) == null) {
                    val modifier = net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        SLOWDOWN_UUID,
                        "Spell channeling slowdown",
                        -0.5,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL
                    )
                    speedAttribute.addTransientModifier(modifier)
                }
            }

            // Наносим фактический урон/эффекты магии
            BeamUtils.cast(level(), currentCaster, currentCaster.lookAngle, power, school, behavior, spellIndex)

            // Расчет начальных векторов и точек для визуализации
            val dir = currentCaster.lookAngle
            val yaw = Math.toRadians(currentCaster.yRot.toDouble())
            val rightHandVector = Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw)).normalize()

            val handOffset = rightHandVector.scale(0.35).add(dir.scale(0.4))
            val start = currentCaster.eyePosition.subtract(0.0, 0.4, 0.0).add(handOffset)
            val maxRange = if (behavior == BeamUtils.BeamType.BEAM) 16.0 else 4.0
            val end = start.add(dir.scale(maxRange))

            // Определение базовой частицы
            val particle = getParticleType() ?: if (spellIndex == 2) {
                ParticleTypes.SOUL_FIRE_FLAME
            } else {
                ParticleTypes.FLAME
            }

            val serverLevel = level() as ServerLevel

            // =======================================================
            // ВЕТВЛЕНИЕ ЧАСТИЦ (СТРОГО ОДИН БЛОК НА ЗАКЛИНАНИЕ)
            // =======================================================
            if (elementId == "spirit") {
                // Предварительно вычисляем цель на земле один раз или используем изначальный вектор,
                // чтобы эффекты не "плавали" за головой игрока, если он отвернулся.
                val rayTraceRange = 32.0
                val hitResult = level().clip(
                    net.minecraft.world.level.ClipContext(
                        currentCaster.eyePosition,
                        currentCaster.eyePosition.add(dir.scale(rayTraceRange)), // Используем dir из Hex Casting вместо lookAngle!
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        currentCaster
                    )
                )
                val groundTarget = hitResult.location

                when (spellIndex) {
                    1 -> { // ==========================================
                        // ДРАКОНЬЕ ДЫХАНИЕ (Визуал + Нанесение урона)
                        // ==========================================
                        val steps = 10
                        val up = dir.cross(rightHandVector).normalize()
                        val maxDistance = 6.5

                        // Рендер частиц (твой код)
                        for (i in 1..steps) {
                            val progress = i / steps.toDouble()
                            val distance = progress * maxDistance
                            val linePos = start.add(dir.scale(distance))
                            val radius = progress * 1.8
                            val angle = random.nextDouble() * Math.PI * 2
                            val offset = rightHandVector.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius))
                            val particlePos = linePos.add(offset)
                            val velocity = dir.scale(0.15).add(offset.normalize().scale(0.05))

                            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, particlePos.x, particlePos.y, particlePos.z, 1, velocity.x, velocity.y, velocity.z, 0.02)
                        }

                        // ЧЕСТНЫЙ УРОН: Каждые 2 тика проверяем врагов в конусе дыхания
                        if (tickCount % 2 == 0) {
                            val targets = serverLevel.getEntitiesOfClass(LivingEntity::class.java, currentCaster.boundingBox.inflate(maxDistance))
                            for (target in targets) {
                                if (target == currentCaster || target.isAlliedTo(currentCaster)) continue

                                val toTarget = target.position().subtract(start)
                                // Проверяем расстояние и угол конуса (дот-продукт > 0.75 означает конус ~45 градусов)
                                if (toTarget.length() <= maxDistance && toTarget.normalize().dot(dir.normalize()) > 0.75) {
                                    target.hurt(serverLevel.damageSources().dragonBreath(), (2.0 * power).toFloat())
                                }
                            }
                        }
                    }

                    2 -> { // SUNBEAM (Частицы у земли)
                        val rayTraceRange = 32.0
                        val hitResult = level().clip(
                            net.minecraft.world.level.ClipContext(
                                currentCaster.eyePosition,
                                currentCaster.eyePosition.add(currentCaster.lookAngle.scale(rayTraceRange)),
                                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                                net.minecraft.world.level.ClipContext.Fluid.NONE,
                                currentCaster
                            )
                        )
                        val ground = hitResult.location
                        serverLevel.sendParticles(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            ground.x,
                            ground.y + 0.2,
                            ground.z,
                            2,
                            0.3,
                            0.1,
                            0.3,
                            0.03
                        )
                    }

                    3 -> { // ==========================================
                        // ЗВЕЗДОПАД (Визуал + Настоящие Кометы Ирона)
                        // ==========================================
                        // Кольцо индикации на земле (твой код, но завязан на вектор dir)
                        val ringParticles = 6
                        for (p in 0 until ringParticles) {
                            val angle = (p * (Math.PI * 2) / ringParticles) + (tickCount * 0.1)
                            val ringX = groundTarget.x + Math.cos(angle) * 6.0
                            val ringZ = groundTarget.z + Math.sin(angle) * 6.0
                            serverLevel.sendParticles(ParticleTypes.PORTAL, ringX, groundTarget.y + 0.1, ringZ, 1, 0.0, 0.0, 0.0, 0.0)
                        }

                        // СПАВН НАСТОЯЩИХ КОМЕТ ИРОНА
                        // Каждые 3 тика сбрасываем с неба комету в зону маркера
                        if (tickCount % 3 == 0) {
                            val areaRadius = 6.0 // Радиус падения из описания Starfall
                            val offsetX = (random.nextDouble() - 0.5) * areaRadius * 2
                            val offsetZ = (random.nextDouble() - 0.5) * areaRadius * 2
                            val impactPoint = Vec3(groundTarget.x + offsetX, groundTarget.y, groundTarget.z + offsetZ)

                            // Точка появления кометы высоко в небе
                            val skySpawnPos = impactPoint.add(-2.0, 15.0, -2.0)

                            // Создаем оригинальную сущность кометы Ирона
                            val cometType = io.redspace.ironsspellbooks.registries.EntityRegistry.COMET.get()
                            val cometEntity = cometType.create(serverLevel)

                            if (cometEntity != null) {
                                cometEntity.setOwner(currentCaster)
                                cometEntity.moveTo(skySpawnPos.x, skySpawnPos.y, skySpawnPos.z, 0f, 0f)

                                // Задаем комете вектор движения строго в точку падения на земле
                                val travelVec = impactPoint.subtract(skySpawnPos).normalize().scale(1.2) // Скорость полета кометы
                                cometEntity.deltaMovement = travelVec

                                // Настраиваем урон кометы на основе силы Hex-каста

                                    cometEntity.damage = (1.0 * power).toFloat()


                                serverLevel.addFreshEntity(cometEntity)
                            }

                            val cometSteps = 6
                            for (s in 0..cometSteps) {
                                val t = s / cometSteps.toDouble()
                                val particlePos = skySpawnPos.lerp(impactPoint, t)
                                serverLevel.sendParticles(ParticleTypes.ENCHANT, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.1)
                            }
                        }
                    }
                }
            }
            else if (elementId == "air" && spellIndex == 1) {
                val steps = 12
                val random = level().random
                val up = dir.cross(rightHandVector).normalize()

                val blockPosUnder = BlockPos.containing(currentCaster.x, currentCaster.y - 0.5, currentCaster.z)
                val groundState = level().getBlockState(blockPosUnder)
                val fluidState = level().getFluidState(blockPosUnder)

                var mainEnvironmentParticle: BlockParticleOption? = null
                var useLeafParticle = false
                var useWaterParticle = false

                if (!fluidState.isEmpty && fluidState.isSource) {
                    useWaterParticle = true
                } else if (!groundState.isAir) {
                    val isShovelBlock = groundState.`is`(BlockTags.MINEABLE_WITH_SHOVEL)
                    val isHoeBlock = groundState.`is`(BlockTags.MINEABLE_WITH_HOE)

                    if (isShovelBlock || isHoeBlock) {
                        mainEnvironmentParticle = BlockParticleOption(ParticleTypes.BLOCK, groundState)

                        val blockName = groundState.block.descriptionId.lowercase()
                        if (blockName.contains("leaves") || blockName.contains("grass") || blockName.contains("moss")) {
                            useLeafParticle = true
                        }
                    }
                }

                val leafParticle = ru.snake_film.hexxyspells.ModParticles.GREEN_LEAF.get()

                for (i in 1..steps) {
                    val progress = i / steps.toDouble()
                    val distance = progress * maxRange

                    val linePos = start.add(dir.scale(distance))
                    val radius = if (progress > 0.5) (progress - 0.5) * 3.5 else progress * 0.5
                    val countInRing = if (progress > 0.5) 3 else 1

                    for (j in 0 until countInRing) {
                        val angle = random.nextDouble() * Math.PI * 2
                        val offset =
                            rightHandVector.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius))
                        val particlePos = linePos.add(offset)

                        var velocity = dir.scale(0.2)
                        if (progress > 0.6) {
                            val pushOut = offset.normalize().scale(0.15)
                            val pullBack = dir.scale(-0.25)
                            velocity = velocity.add(pushOut).add(pullBack)
                        }

                        // Слой 1: Базовый каркас ветра (всегда)
                        serverLevel.sendParticles(
                            ParticleTypes.CLOUD,
                            particlePos.x,
                            particlePos.y,
                            particlePos.z,
                            0,
                            velocity.x,
                            velocity.y,
                            velocity.z,
                            1.0
                        )

                        // Слой 2: Водный вихрь
                        if (useWaterParticle) {
                            if (random.nextFloat() > 0.3f) {
                                serverLevel.sendParticles(
                                    ParticleTypes.RAIN,
                                    particlePos.x,
                                    particlePos.y,
                                    particlePos.z,
                                    0,
                                    velocity.x,
                                    velocity.y,
                                    velocity.z,
                                    1.0
                                )
                            }
                            if (random.nextFloat() > 0.7f) {
                                serverLevel.sendParticles(
                                    ParticleTypes.BUBBLE,
                                    particlePos.x,
                                    particlePos.y,
                                    particlePos.z,
                                    0,
                                    velocity.x,
                                    velocity.y,
                                    velocity.z,
                                    0.5
                                )
                            }
                        }
                        // Слой 3: МНОГО частиц блоков земли/песка (если блок прошел фильтр)
                        else if (mainEnvironmentParticle != null) {
                            // Вместо count = 0 (символизирующего скорость) ставим count = 3.
                            // Это заставит Майнкрафт спавнить по 3 частицы за раз.
                            // Параметр 0.15 — это небольшой случайный разброс (X, Y, Z), чтобы они не летели строго в одну точку, а создавали объем.
                            serverLevel.sendParticles(
                                mainEnvironmentParticle,
                                particlePos.x, particlePos.y, particlePos.z,
                                3,          // Количество частиц в одной точке за тик
                                0.15, 0.15, 0.15, // Разброс в стороны (дельта)
                                0.05        // Собственная скорость разлета частиц блока
                            )
                        }

                        // Слой 4: Зеленые Листья
                        if (useLeafParticle && random.nextFloat() > 0.2f) {
                            serverLevel.sendParticles(
                                leafParticle,
                                particlePos.x,
                                particlePos.y,
                                particlePos.z,
                                0,
                                velocity.x,
                                velocity.y,
                                velocity.z,
                                0.4
                            )
                        }
                    }
                }
            } else if (elementId == "air" && spellIndex == 3) {
                // ВОЗДУХ 3: ТВОЙ ЦИКЛОН ЗАСАСЫВАНИЯ
                val centerOfCyclone = start.add(dir.scale(4.0))
                val particleCount = 4

                for (p in 0 until particleCount) {
                    val randomOffset = Vec3(
                        (level().random.nextDouble() - 0.5) * 7.0,
                        (level().random.nextDouble() - 0.5) * 3.0,
                        (level().random.nextDouble() - 0.5) * 7.0
                    )
                    val spawnPos = centerOfCyclone.add(randomOffset)
                    val speedVec = centerOfCyclone.subtract(spawnPos).scale(0.15)

                    serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        spawnPos.x,
                        spawnPos.y,
                        spawnPos.z,
                        0,
                        speedVec.x,
                        speedVec.y,
                        speedVec.z,
                        1.0
                    )
                }
            }

            else {
                // ДЛЯ ВСЕХ ОСТАЛЬНЫХ МАГИЙ: СТАНДАРТНЫЙ ПРОСЧЕТ ЛУЧЕЙ (Огонь, Лед и т.д.)
                val steps = 10
                for (i in 1..steps) {
                    val pos = start.lerp(end, i / steps.toDouble())
                    var delta = if (behavior == BeamUtils.BeamType.CONE) 0.4 else 0.0
                    var count = 1

                    if (elementId == "water" && spellIndex == 3) {
                        delta = 0.8
                        count = 4
                    }

                    serverLevel.sendParticles(particle, pos.x, pos.y, pos.z, count, delta, delta, delta, 0.0)
                }
            }

            // ЗВУКОВОЕ СОПРОВОЖДЕНИЕ (Вынесено из условий частиц, поет всегда!)
            val spellSound = getSoundEvent()
            if (spellSound != null && this.tickCount % 30 == 1) {
                level().playSound(
                    null,
                    this.x,
                    this.y,
                    this.z,
                    spellSound,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0f,
                    1.0f
                )
            }

            // СПАВН И АКТУАЛИЗАЦИЯ СУЩНОСТЕЙ ДЛЯ ВСЕХ МОДЕЛЕЙ (Вынесено из условий!)
            val visualType = getVisualEntityType()
            if (visualType != null) {
                val isSunbeam = visualType.toString().contains("sunbeam")

                // Вычисляем позицию спавна
                val visualSpawnPos = if (isSunbeam) {
                    // Трассируем луч до блока для Sunbeam
                    val rayTraceRange = 32.0
                    val hitResult = currentCaster.level().clip(
                        net.minecraft.world.level.ClipContext(
                            currentCaster.eyePosition,
                            currentCaster.eyePosition.add(currentCaster.lookAngle.scale(rayTraceRange)),
                            net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE,
                            currentCaster
                        )
                    )
                    hitResult.location
                } else {
                    // Обычные лучи привязаны к руке
                    start.subtract(0.0, 0.5, 0.0)
                }

                val needNewEntity = visualEntity == null ||
                        visualEntity!!.isRemoved ||
                        (visualEntity is RayOfFrostVisualEntity && visualEntity!!.tickCount >= 12)

                if (needNewEntity) {
                    visualEntity?.discard()
                    visualEntity = visualType.create(level())

                    visualEntity?.let { entity ->
                        if (!isSunbeam && behavior == BeamUtils.BeamType.BEAM && entity is RayOfFrostVisualEntity) {
                            entity.distance = maxRange.toFloat()
                        }

                        entity.moveTo(visualSpawnPos.x, visualSpawnPos.y, visualSpawnPos.z, currentCaster.yRot, currentCaster.xRot)

                        // Фикс урона Sunbeam: выставляем через правильное поле и расширяем радиус AOE, чтобы точно задевало
                        if (isSunbeam && entity is io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity) {
                            entity.setRadius(1.5f)
                            // Используем встроенный унаследованный сеттер урона из AoeEntity/AbstractMagicProjectile
                            entity.damage = (power * 0.5).toFloat()
                        }

                        level().addFreshEntity(entity)
                    }
                } else {
                    visualEntity?.let { entity ->
                        if (!isSunbeam && behavior == BeamUtils.BeamType.BEAM && entity is RayOfFrostVisualEntity) {
                            entity.distance = maxRange.toFloat()
                        }

                        entity.moveTo(visualSpawnPos.x, visualSpawnPos.y, visualSpawnPos.z, currentCaster.yRot, currentCaster.xRot)

                        if (isSunbeam && entity is io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity) {
                            // На всякий случай обновляем урон на лету, если power динамический
                            entity.damage = (power * 0.5).toFloat()

                            // ХАК ВРЕМЕНИ: Удерживаем луч активным
                            if (entity.tickCount >= 15) {
                                entity.tickCount = 14
                            }
                        }
                    }
                }
            }
        }

        duration--
    }

    private fun removeVisualAndDiscard() {
        visualEntity?.discard()

        if (!level().isClientSide) {
            getSoundEvent()?.let { sound ->
                val stopPacket = net.minecraft.network.protocol.game.ClientboundStopSoundPacket(sound.location, net.minecraft.sounds.SoundSource.PLAYERS)
                (level() as ServerLevel).chunkSource.chunkMap.getPlayers(net.minecraft.world.level.ChunkPos(this.blockPosition()), false).forEach { player ->
                    player.connection.send(stopPacket)
                }
            }

            if (caster != null) {
                caster!!.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                    ?.removeModifier(SLOWDOWN_UUID)
            }
        }
        this.discard()
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {}
    override fun addAdditionalSaveData(tag: CompoundTag) {}
}
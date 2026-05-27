package ru.snake_film.hexxyspells.hud

import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import io.redspace.ironsspellbooks.api.magic.MagicData
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.NetworkHooks
import ru.snake_film.hexxyspells.elements.ElementalPatternIota
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class FlyingCastEnv(player: ServerPlayer, hand: InteractionHand) : PlayerBasedCastEnv(player, hand) {
    override fun extractMediaEnvironment(cost: Long, allowOvercast: Boolean): Long = this.extractMediaFromInventory(cost, allowOvercast, false)
    override fun getCastingHand(): InteractionHand = this.castingHand
    override fun getPigment(): FrozenPigment = IXplatAbstractions.INSTANCE.getPigment(this.caster)
}

class EntityFlyingPattern(type: EntityType<*>, world: Level) : Entity(type, world) {

    var patterns: MutableList<PatternIota> = mutableListOf()

    var maxCastTime: Int
        get() = this.entityData.get(MAX_CAST_TIME)
        set(value) = this.entityData.set(MAX_CAST_TIME, value)

    var manaCost: Float = 0f

    // Переменные для левитации и офсета
    private var bobbingTick: Int = 0
    private var fixedRelativeOffset: Vec3? = null
    private var wasSelected: Boolean = false

    companion object {

        val IS_SELECTED = SynchedEntityData.defineId(EntityFlyingPattern::class.java, EntityDataSerializers.BOOLEAN)
        val OWNER_UUID = SynchedEntityData.defineId(EntityFlyingPattern::class.java, EntityDataSerializers.OPTIONAL_UUID)
        val CAST_PROGRESS = SynchedEntityData.defineId(EntityFlyingPattern::class.java, EntityDataSerializers.INT)
        private val ELEMENT_ID: EntityDataAccessor<String> = SynchedEntityData.defineId(EntityFlyingPattern::class.java, EntityDataSerializers.STRING)
        // Также нам нужно синхронизировать список паттернов для клиента
        val SYNCED_PATTERNS: EntityDataAccessor<String> = SynchedEntityData.defineId(EntityFlyingPattern::class.java, EntityDataSerializers.STRING)
        val MAX_CAST_TIME = SynchedEntityData.defineId(EntityFlyingPattern::class.java, EntityDataSerializers.INT)
    }
        override fun defineSynchedData() {
            this.entityData.define(MAX_CAST_TIME, 20)
            this.entityData.define(IS_SELECTED, false)
            this.entityData.define(OWNER_UUID, Optional.empty())
            this.entityData.define(CAST_PROGRESS, 0)
            this.entityData.define(SYNCED_PATTERNS, "")
            this.entityData.define(ELEMENT_ID, "spirit")


    }
    var element: String
        get() = this.entityData.get(ELEMENT_ID)
        set(value) = this.entityData.set(ELEMENT_ID, value)
    fun updateSyncedData() {
        if (level().isClientSide) return

        // Превращаем список паттернов в строку
        val encoded = patterns.joinToString(";") { "${it.pattern.anglesSignature()}|${it.pattern.startDir.name}" }
        this.entityData.set(SYNCED_PATTERNS, encoded)
    }

    // ВАЖНО: Мы используем anglesSignature() чтобы получить чистую строку "qwe", а не массив "[Q, W, E]"
    fun syncPatterns() {
        if (level().isClientSide) return
        val encoded = patterns.joinToString(";") { "${it.pattern.anglesSignature()}|${it.pattern.startDir.name}" }
        this.entityData.set(SYNCED_PATTERNS, encoded)

    }
    override fun getAddEntityPacket(): Packet<ClientGamePacketListener> {
        // Это заставит сервер отправить все данные (включая NBT из addAdditionalSaveData) клиенту при появлении
        return NetworkHooks.getEntitySpawningPacket(this)
    }

    var isSelected: Boolean
        get() = this.entityData.get(IS_SELECTED)
        set(value) = this.entityData.set(IS_SELECTED, value)

    var castProgress: Int
        get() = this.entityData.get(CAST_PROGRESS)
        set(value) = this.entityData.set(CAST_PROGRESS, value)
    fun addTimeToMaxCast(time: Int) {
        if (level().isClientSide) return
        this.maxCastTime += time
        // Синхронизируем, чтобы клиент увидел обновление полоски
        this.updateSyncedData()
    }

    fun setOwner(uuid: UUID) = this.entityData.set(OWNER_UUID, Optional.of(uuid))
    fun getOwnerUUID(): UUID? = this.entityData.get(OWNER_UUID).orElse(null)

    override fun isPickable(): Boolean = true
    override fun isAttackable(): Boolean = true
    override fun makeBoundingBox(): AABB = AABB.ofSize(this.position(), 0.5, 0.5, 0.5) // Хитбокс 0.5

    override fun refreshDimensions() {
        super.refreshDimensions()
        this.setBoundingBox(this.makeBoundingBox())
    }

    override fun tick() {
        super.tick()
        this.refreshDimensions()
        bobbingTick++

        val ownerUuid = getOwnerUUID() ?: return
        val owner = level().getPlayerByUUID(ownerUuid) as? ServerPlayer ?: return

        if (isSelected) {
            wasSelected = true

            // Летит перед глазами
            val targetPos = owner.getEyePosition(1.0f).add(owner.lookAngle.scale(1.8))
            val currentPos = this.position()
            val lerpPos = currentPos.add(targetPos.subtract(currentPos).scale(0.25))
            this.setPos(lerpPos.x, lerpPos.y, lerpPos.z)

            // Логика нажатия C (через Persistent Data, которую шлет обновленный пакет)
            if (!level().isClientSide && isSelected) {
                val cPressed = owner.persistentData.getBoolean("is_c_pressed")
                if (cPressed) {
                    castProgress++
                    if (castProgress >= maxCastTime) {
                        performCast(owner)
                        castProgress = 0
                        isSelected = false
                    }
                } else {
                    castProgress = 0
                }
            }
        } else {
            // Если только что отвязали - ЗАПОМИНАЕМ точный вектор от игрока до руны
            if (wasSelected || fixedRelativeOffset == null) {
                fixedRelativeOffset = this.position().subtract(owner.position())
                wasSelected = false
            }

            // Плавное покачивание
            val floatY = sin(bobbingTick * 0.05) * 0.15
            val floatX = cos(bobbingTick * 0.03) * 0.1

            // Итоговая позиция = позиция игрока + сохраненный вектор отрыва + покачивание
            val relativeTarget = fixedRelativeOffset!!.add(floatX, floatY, 0.0)
            val targetPos = owner.position().add(relativeTarget)

            val currentPos = this.position()
            val distSq = targetPos.distanceToSqr(currentPos)

            // Если игрок ушел дальше 32 блоков, притягиваем руну, иначе она просто висит на своем смещении
            if (distSq > 1024.0) {
                this.setPos(targetPos.x, targetPos.y, targetPos.z)
            } else {
                val lerpPos = currentPos.add(targetPos.subtract(currentPos).scale(0.1))
                this.setPos(lerpPos.x, lerpPos.y, lerpPos.z)
            }
        }
    }


    override fun interact(player: Player, hand: InteractionHand): InteractionResult {
        if (hand == InteractionHand.MAIN_HAND && !level().isClientSide) {
            if (getOwnerUUID() == player.uuid) {

                if (this.isSelected) {
                    return InteractionResult.SUCCESS
                } else {
                    val others = level().getEntitiesOfClass(EntityFlyingPattern::class.java, player.boundingBox.inflate(15.0))
                    others.forEach { it.isSelected = false } // Снимаем выбор с других
                    this.isSelected = true
                    return InteractionResult.SUCCESS
                }
            }
        }
        return InteractionResult.PASS
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val attacker = source.entity
        if (attacker is Player && attacker.uuid == getOwnerUUID() && !level().isClientSide) {
            val isMelee = source.directEntity == attacker
            if (this.isSelected) this.isSelected = false
            else if (isMelee) {
                if (attacker.isCrouching) {
                    this.discard()
                }
                return true
            }
            return false
        }
        return true
    }

        private fun performCast(player: ServerPlayer) {
            val magicData = MagicData.getPlayerMagicData(player)
            if (magicData.mana < manaCost) {
                level().playSound(
                    null,
                    blockPosition(),
                    io.redspace.ironsspellbooks.registries.SoundRegistry.VAULT_FALL.get(),
                    SoundSource.PLAYERS,
                    1f,
                    1f
                )
                castProgress = 0
                return
            }
            magicData.mana -= manaCost

            val sLevel = level() as? ServerLevel ?: return
            val env = FlyingCastEnv(player, InteractionHand.MAIN_HAND)
            val vm = CastingVM.empty(env)
            vm.queueExecuteAndWrapIotas(patterns, sLevel)

            sLevel.playSound(
                null,
                x,
                y,
                z,
                at.petrak.hexcasting.common.lib.HexSounds.CAST_HERMES,
                SoundSource.PLAYERS,
                1f,
                1f
            )

            // Сбросим состояние кнопки C у игрока, чтобы следующий спавн сразу не начал каст
            player.persistentData.putBoolean("is_c_pressed", false)
            this.discard()
        }

    override fun canBeCollidedWith(): Boolean {
        return false
    }
    override fun isPushable(): Boolean {
        return false
    }

    override fun push(entity: Entity) {
        // Оставляем пустым, чтобы не передавать импульс при контакте
    }

    override fun canBeHitByProjectile(): Boolean {
        return false
    }
        override fun addAdditionalSaveData(pCompound: CompoundTag) {
            getOwnerUUID()?.let { pCompound.putUUID("owner", it) }
            pCompound.putBoolean("isSelected", isSelected)
            pCompound.putInt("maxCastTime", this.maxCastTime)
            pCompound.putFloat("manaCost", manaCost)

            val listTag = ListTag()
            for (iota in patterns) {
                listTag.add(IotaType.serialize(iota))
            }
            pCompound.put("stored_patterns", listTag)
        }


        override fun readAdditionalSaveData(nbt: CompoundTag) {
            if (nbt.hasUUID("owner")) this.setOwner(nbt.getUUID("owner"))
            if (nbt.contains("isSelected")) isSelected = nbt.getBoolean("isSelected")
            if (nbt.contains("maxCastTime")) {
                this.maxCastTime = nbt.getInt("maxCastTime")
            if (nbt.contains("manaCost")) manaCost = nbt.getFloat("manaCost")

            if (nbt.contains("stored_patterns")) {
                val list = nbt.getList("stored_patterns", Tag.TAG_COMPOUND.toInt())
                patterns.clear()
                val sLevel = level() as? ServerLevel
                if (sLevel != null) {
                    for (i in 0 until list.size) {
                        val iota = IotaType.deserialize(list.getCompound(i), sLevel)
                        if (iota is PatternIota) patterns.add(iota)
                    }
                    syncPatterns() // Синхронизируем на клиент после загрузки чанка
                }
            }
        }
    }
}
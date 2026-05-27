package ru.snake_film.hexxyspells

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.CherryParticle
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.SimpleParticleType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = HexxySpells.MODID, value = [Dist.CLIENT])
object ClientEvents {

    // Переменная для отслеживания изменения состояния клавиши
    private var wasCPressed = false

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            val player = Minecraft.getInstance().player ?: return

            // Текущее состояние клавиши
            val isCPressed = HexxySpells.CAST_KEY.isDown

            // Если состояние изменилось (нажали ИЛИ отпустили) - отправляем пакет
            if (isCPressed != wasCPressed) {
                wasCPressed = isCPressed
                ModPackets.CHANNEL.sendToServer(MouseUpdatePacket(2, isCPressed))
            }
        }
    }

    class GreenLeafParticle(
        level: ClientLevel,
        x: Double, y: Double, z: Double,
        dx: Double, dy: Double, dz: Double,
        spriteSet: SpriteSet
    ) : CherryParticle(level, x, y, z, spriteSet) {

        init {
            // Задаем листу скорость нашего воздушного вихря, переданную из заклинания!
            this.xd = dx
            this.yd = dy
            this.zd = dz

            // Немного укоротим ему жизнь (например, до 20-40 тиков),
            // чтобы листья не летали по миру вечно после каста
            this.lifetime = 20 + this.random.nextInt(20)
        }
    }

    @SubscribeEvent
    fun registerParticles(event: RegisterParticleProvidersEvent) {
        event.registerSpriteSet(ModParticles.GREEN_LEAF.get()) { spriteSet ->
            object : ParticleProvider<SimpleParticleType> {
                override fun createParticle(
                    type: SimpleParticleType,
                    level: ClientLevel,
                    x: Double, y: Double, z: Double,
                    dx: Double, dy: Double, dz: Double
                ): Particle {
                    // Теперь вызываем НАШ класс, куда честно передаем и координаты, и скорость вихря
                    return GreenLeafParticle(level, x, y, z, dx, dy, dz, spriteSet)
                }
            }
        }
    }
}
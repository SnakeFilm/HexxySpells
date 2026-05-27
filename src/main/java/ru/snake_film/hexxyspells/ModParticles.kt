package ru.snake_film.hexxyspells

import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.Registries
import net.minecraft.core.particles.SimpleParticleType
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegistryObject

object ModParticles {
    @JvmField
    val PARTICLE_TYPES: DeferredRegister<ParticleType<*>> = DeferredRegister.create(Registries.PARTICLE_TYPE, "your_mod_id")


    val GREEN_LEAF: RegistryObject<SimpleParticleType> = PARTICLE_TYPES.register("green_leaf") {
        SimpleParticleType(false) // false = отрисовка на любой дистанции
    }
}
package ru.snake_film.hexxyspells.hud

import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.client.render.PatternColors
import at.petrak.hexcasting.client.render.PatternRenderer
import at.petrak.hexcasting.client.render.PatternRenderer.WorldlyBits
import at.petrak.hexcasting.client.render.PatternSettings
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import com.mojang.math.Axis
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.iota.StringIota
import ru.snake_film.hexxyspells.elements.*
import ru.snake_film.hexxyspells.ElementSelectPacket
class RenderFlyingPattern(ctx: EntityRendererProvider.Context) : EntityRenderer<EntityFlyingPattern>(ctx) {

    // private val BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "textures/gui/icons.png")
    private var testStroke = PatternSettings.StrokeSettings.fromStroke(0.085)
    private val testPos = PatternSettings.PositionSettings.paddedSquare(0.0, 1.0, 1.0)
    private val testSettings = PatternSettings("flying", testPos, testStroke, PatternSettings.ZappySettings.STATIC)
    private val testColor = 0xFF_B18FEF.toInt()
    private val testColorOut = 0xFF_B18FEF.toInt()

    override fun render(entity: EntityFlyingPattern, yaw: Float, partialTicks: Float, ms: PoseStack, buffer: MultiBufferSource, light: Int) {
        ms.pushPose()


        // Высота: 0.25 - это ровно середина хитбокса размером 0.5
        ms.translate(0.0, -0.25, 0.0)

        // Биллбординг (поворот к лицу)
        ms.mulPose(this.entityRenderDispatcher.cameraOrientation())
        ms.mulPose(Axis.YP.rotationDegrees(180f))

        var activeElement = entity.element
        val rawData = entity.entityData.get(EntityFlyingPattern.SYNCED_PATTERNS)

        val patternsToRender = mutableListOf<HexPattern>()
        val firstIota = entity.patterns.firstOrNull()

        if (rawData.isNullOrBlank()) {
            // Добавляем заглушку qqq
            patternsToRender.add(HexPattern.fromAngles("qqq", HexDir.SOUTH_EAST))

        } else {
            val cleanData = rawData.replace("[", "").replace("]", "").trim()
            cleanData.split(";").forEach { pStr ->
                if (pStr.isNotBlank()) {
                    val parts = pStr.split("|")
                    if (parts.size == 2) {
                        try {
                            //patternsToRender.add(HexPattern.fromAngles(parts[0], HexDir.valueOf(parts[1])))

                            val pattern = HexPattern.fromAngles(parts[0], HexDir.valueOf(parts[1]))

                            // 2. Добавляем его в список для рендера
                            patternsToRender.add(pattern)

                            // 3. ПРОВЕРЯЕМ СИГНАТУРУ (parts[0] - это и есть углы)
                            val sig = parts[0]
                            val detected = when (sig) {
                                "wqqqqa" -> "fire"
                                "wqqqqw" -> "water"
                                "aqawwawwaw"  -> "earth"
                                "aqqqqwqq"   -> "air"
                                "qaqqqqqwqqqeaeaeaeadaeaeaea"   -> "dark"
                                else -> null
                            }

                            // Если нашли стихийный паттерн — перезаписываем текущую стихию
                            if (detected != null) {
                                activeElement = detected
                            }
                        } catch (e: Exception) {  }
                    }
                }
            }
        }

        // 2. РЕНДЕР ПАТТЕРНОВ
        entity.element = activeElement
        val glowingBuffer = MultiBufferSource { renderType ->

            buffer.getBuffer(RenderType.lightning())
        }
        val lightFull = 15728880
        val spacing = if (entity.isSelected) 0.14f else 0.04f
        val glowingBits = WorldlyBits(
            glowingBuffer,
            lightFull,
            Vec3.ZERO
        )

            val elementcolor = entity.element
            val colors = when(elementcolor) {
            "fire" -> PatternColors.glowyStroke(0xFF_FF4500.toInt())   // Оранжево-красный
            "water" -> PatternColors.glowyStroke(0xFF_00BFFF.toInt()  )// Голубой
            "earth" -> PatternColors.glowyStroke(0xFF_8B4513.toInt() ) // Коричневый
            "air" -> PatternColors.glowyStroke(0xFF_F0FFFF.toInt() )   // Белый/Воздушный
            "dark" -> PatternColors.glowyStroke(0xFF_3b0209.toInt())   // Темно-фиолетовый
            else -> PatternColors.glowyStroke(testColor.toInt())
        }

        patternsToRender.forEachIndexed { index, pattern ->
            ms.pushPose()
            ms.translate(0.0, (index * spacing).toDouble(), 0.0)
            ms.scale(0.2f, 0.2f, 0.2f)

            at.petrak.hexcasting.client.render.PatternRenderer.renderPattern(
                pattern,
                ms,
                testSettings,
                colors,
                entity.tickCount.toDouble() + partialTicks,
                lightFull
            )
            ms.popPose()

        }
        val maxTime = entity.maxCastTime
        val progress = if (maxTime > 0) {
            (entity.castProgress.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        if (progress > 0f) {
            renderBar(ms, progress)
            println(progress)
            println(maxTime)
        }
        ms.popPose()
        super.render(entity, yaw, partialTicks, ms, buffer, light)

    }


    private fun renderBar(ms: PoseStack, progress: Float) {
        ms.pushPose()

        ms.translate(-0.5, -0.25, 0.01)


        drawRect(ms, 0f, 0f, 1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.7f)


        ms.translate(0.0, 0.0, 0.01)
        drawRect(ms, 0f, 0f, progress, 0.1f, 0.7f, 0.5f, 1f, 1f)

        ms.popPose()
    }

    private fun drawRect(ms: PoseStack, x: Float, y: Float, w: Float, h: Float, r: Float, g: Float, b: Float, a: Float) {
        val matrix = ms.last().pose()
        val tesselator = Tesselator.getInstance()
        val bufferbuilder = tesselator.getBuilder()

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.disableDepthTest()

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)

        bufferbuilder.vertex(matrix, x, y, 0f).color(r, g, b, a).endVertex()
        bufferbuilder.vertex(matrix, x, y + h, 0f).color(r, g, b, a).endVertex()
        bufferbuilder.vertex(matrix, x + w, y + h, 0f).color(r, g, b, a).endVertex()
        bufferbuilder.vertex(matrix, x + w, y, 0f).color(r, g, b, a).endVertex()

        tesselator.end()

        RenderSystem.enableDepthTest()
    }


    override fun getTextureLocation(entity: EntityFlyingPattern): ResourceLocation =
        ResourceLocation("hexcasting", "textures/entity/white.png")
}
package com.knowspends.app.ui.theme

import android.graphics.Matrix
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

/**
 * Material 3 Expressive Algorithmic Shapes catalog powered by [androidx.graphics.shapes].
 * Matches the Google M3 Expressive shapes catalog from https://rxmaterial3e.dpdns.org/
 */
object ExpressivePolygons {
    /** 12-lobed flower / cookie scallop */
    val Scallop = RoundedPolygon.star(
        numVerticesPerRadius = 12,
        radius = 1f,
        innerRadius = 0.82f,
        rounding = CornerRounding(0.35f)
    )

    /** 4-leaf organic rounded clover */
    val Clover = RoundedPolygon.star(
        numVerticesPerRadius = 4,
        radius = 1f,
        innerRadius = 0.65f,
        rounding = CornerRounding(0.40f)
    )

    /** 4-pointed dynamic starburst */
    val Star4 = RoundedPolygon.star(
        numVerticesPerRadius = 4,
        radius = 1f,
        innerRadius = 0.45f,
        rounding = CornerRounding(0.20f)
    )

    /** 8-pointed sunny flower / star */
    val Star8 = RoundedPolygon.star(
        numVerticesPerRadius = 8,
        radius = 1f,
        innerRadius = 0.72f,
        rounding = CornerRounding(0.30f)
    )

    /** 6-pointed playful hexagon bun */
    val Bun = RoundedPolygon.star(
        numVerticesPerRadius = 6,
        radius = 1f,
        innerRadius = 0.85f,
        rounding = CornerRounding(0.30f)
    )

    /** Smooth rounded circle */
    val Circle = RoundedPolygon.circle(numVertices = 12)

    /** Algorithmic morph from Scallop to Circle for FAB open/close */
    val ScallopToCircleMorph = Morph(Scallop, Circle)

    /** Algorithmic morph from Clover to Star for dynamic touch feedback */
    val CloverToStarMorph = Morph(Clover, Star4)
}

/**
 * Converts a [RoundedPolygon] into a responsive Jetpack Compose [Shape] that
 * automatically scales and centers within the composable's bounds.
 */
class RoundedPolygonShape(
    private val polygon: RoundedPolygon
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Rectangle(Rect.Zero)
        }
        val path = polygon.toPath()
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val bw = bounds.width().coerceAtLeast(0.001f)
        val bh = bounds.height().coerceAtLeast(0.001f)
        val scale = minOf(size.width / bw, size.height / bh)

        val matrix = Matrix()
        matrix.postTranslate(-bounds.left, -bounds.top)
        matrix.postScale(scale, scale)
        matrix.postTranslate(
            (size.width - bw * scale) / 2f,
            (size.height - bh * scale) / 2f
        )
        path.transform(matrix)
        return Outline.Generic(path.asComposePath())
    }
}

/**
 * Converts a [Morph] between two [RoundedPolygon] geometries into a dynamic [Shape]
 * based on the animation [progress] (0.0f = start shape, 1.0f = end shape).
 */
class MorphShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Rectangle(Rect.Zero)
        }
        val path = morph.toPath(progress.coerceIn(0f, 1f))
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val bw = bounds.width().coerceAtLeast(0.001f)
        val bh = bounds.height().coerceAtLeast(0.001f)
        val scale = minOf(size.width / bw, size.height / bh)

        val matrix = Matrix()
        matrix.postTranslate(-bounds.left, -bounds.top)
        matrix.postScale(scale, scale)
        matrix.postTranslate(
            (size.width - bw * scale) / 2f,
            (size.height - bh * scale) / 2f
        )
        path.transform(matrix)
        return Outline.Generic(path.asComposePath())
    }
}

/**
 * Pre-allocated Shape singletons for high UI rendering performance.
 */
object ExpressiveShapes {
    val Scallop: Shape = RoundedPolygonShape(ExpressivePolygons.Scallop)
    val Clover: Shape = RoundedPolygonShape(ExpressivePolygons.Clover)
    val Star4: Shape = RoundedPolygonShape(ExpressivePolygons.Star4)
    val Star8: Shape = RoundedPolygonShape(ExpressivePolygons.Star8)
    val Bun: Shape = RoundedPolygonShape(ExpressivePolygons.Bun)
    val Circle: Shape = RoundedPolygonShape(ExpressivePolygons.Circle)
}

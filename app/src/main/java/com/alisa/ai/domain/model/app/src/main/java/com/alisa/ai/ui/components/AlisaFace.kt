package com.alisa.ai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alisa.ai.domain.model.AlisaState
import com.alisa.ai.ui.theme.ErrorGlow
import com.alisa.ai.ui.theme.TealGlowPrimary
import com.alisa.ai.ui.theme.TealGlowSecondary

@Composable
fun AlisaFace(
    state: AlisaState,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AlisaAnim")

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 0 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    val eyeScale by infiniteTransition.animateFloat(
        initialValue = if (state == AlisaState.LISTENING) 0.85f else 1.0f,
        targetValue = if (state == AlisaState.LISTENING) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 0 else 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EyeScale"
    )

    val mouthWave by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 0 else 180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MouthWave"
    )

    val baseColor = when (state) {
        AlisaState.ERROR -> ErrorGlow
        AlisaState.SLEEPING -> TealGlowSecondary.copy(alpha = 0.35f)
        AlisaState.THINKING -> TealGlowSecondary
        else -> TealGlowPrimary
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height

            val glowRadius = (width * 0.48f) * glowPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = if (state == AlisaState.THINKING) 0.35f else 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(width / 2f, height / 2f),
                    radius = glowRadius
                )
            )

            val eyeRadius = (width * 0.10f) * if (state == AlisaState.LISTENING) eyeScale else 1f
            val eyeStrokeWidth = width * 0.026f
            val leftEyeCenter = Offset(width * 0.32f, height * 0.38f)
            val rightEyeCenter = Offset(width * 0.68f, height * 0.38f)

            if (state != AlisaState.SLEEPING) {
                drawXSymbol(
                    center = leftEyeCenter,
                    radius = eyeRadius,
                    strokeWidth = eyeStrokeWidth,
                    color = baseColor
                )
                drawXSymbol(
                    center = rightEyeCenter,
                    radius = eyeRadius,
                    strokeWidth = eyeStrokeWidth,
                    color = baseColor
                )
            } else {
                drawLine(
                    color = baseColor,
                    start = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y),
                    end = Offset(leftEyeCenter.x + eyeRadius, leftEyeCenter.y),
                    strokeWidth = eyeStrokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = baseColor,
                    start = Offset(rightEyeCenter.x - eyeRadius, rightEyeCenter.y),
                    end = Offset(rightEyeCenter.x + eyeRadius, rightEyeCenter.y),
                    strokeWidth = eyeStrokeWidth,
                    cap = StrokeCap.Round
                )
            }

            val mouthY = height * 0.72f
            val mouthLineWidth = width * 0.60f
            val mouthLineStart = Offset((width - mouthLineWidth) / 2f, mouthY)
            val mouthLineEnd = Offset(mouthLineStart.x + mouthLineWidth, mouthY)
            val mouthStrokeWidth = width * 0.016f

            drawLine(
                color = baseColor.copy(alpha = 0.7f),
                start = mouthLineStart,
                end = mouthLineEnd,
                strokeWidth = mouthStrokeWidth,
                cap = StrokeCap.Round
            )

            val mouthXCenters = listOf(
                Offset(width * 0.33f, mouthY + if (state == AlisaState.SPEAKING) mouthWave else 0f),
                Offset(width * 0.50f, mouthY - if (state == AlisaState.SPEAKING) mouthWave else 0f),
                Offset(width * 0.67f, mouthY + if (state == AlisaState.SPEAKING) mouthWave else 0f)
            )

            val mouthXRadius = width * 0.045f
            val mouthXStroke = width * 0.020f

            mouthXCenters.forEach { xCenter ->
                drawXSymbol(
                    center = xCenter,
                    radius = mouthXRadius,
                    strokeWidth = mouthXStroke,
                    color = baseColor
                )
            }
        }
    }
}

private fun DrawScope.drawXSymbol(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    color: Color
) {
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y - radius),
        end = Offset(center.x + radius, center.y + radius),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y + radius),
        end = Offset(center.x + radius, center.y - radius),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

package com.example.omnigo.features.main.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.omnigo.R
import com.example.omnigo.features.main.presentation.viewmodel.SplashViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BackgroundLight
import com.example.omnigo.ui.theme.OnPrimaryColor
import com.example.omnigo.ui.theme.OutlineVariantLight
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SecondaryContainer
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextTertiary
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s28
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onNavigateToDestination: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var targetDestination by remember { mutableStateOf<String?>(null) }

    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val progressWidth = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_orbit")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    LaunchedEffect(Unit) {
        viewModel.decideStartDestination { destination ->
            targetDestination = destination
        }

        // Trigger animations concurrently
        logoAlpha.animateTo(1f, animationSpec = tween(500))
        logoScale.animateTo(
            1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
        )

        textAlpha.animateTo(1f, animationSpec = tween(600))
        progressWidth.animateTo(1f, animationSpec = tween(700))

        delay(400)

        val destination = targetDestination ?: "login"
        onNavigateToDestination(destination)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central Logo with Orbit & Pulse Glow
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Pulse Glow Ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .clip(CircleShape)
                        .background(PrimaryContainer)
                )

                // 3 Orbiting Service Satellites
                OrbitSatellite(
                    angleDegrees = orbitAngle,
                    radiusDp = 80f,
                    iconRes = R.drawable.ic_home,
                    containerColor = PrimaryContainer,
                    iconColor = PrimaryColor
                )

                OrbitSatellite(
                    angleDegrees = orbitAngle + 120f,
                    radiusDp = 80f,
                    iconRes = R.drawable.ic_discount,
                    containerColor = SecondaryContainer,
                    iconColor = SecondaryColor
                )

                OrbitSatellite(
                    angleDegrees = orbitAngle + 240f,
                    radiusDp = 80f,
                    iconRes = R.drawable.ic_activity,
                    containerColor = PrimaryContainer,
                    iconColor = PrimaryColor
                )

                // Main Central Logo
                Image(
                    painter = painterResource(id = R.drawable.omni_logo),
                    contentDescription = stringResource(id = R.string.app_name),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // App Brand Name & Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.s28.bold(),
                    color = PrimaryColor
                )

                Spacer(modifier = Modifier.height(AppSpacing.XS))

                Text(
                    text = stringResource(id = R.string.app_tagline),
                    style = MaterialTheme.typography.s14.normal(),
                    color = TextSecondary
                )
            }
        }

        // Bottom Capsule Progress Indicator & App Version
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Dimen.PaddingXXL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(Dimen.PaddingXS)
                    .clip(RoundedCornerShape(AppShape.PillShape))
                    .background(OutlineVariantLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(AppShape.PillShape))
                        .background(PrimaryColor)
                        .scale(scaleX = progressWidth.value, scaleY = 1f)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.M))

            Text(
                text = stringResource(id = R.string.account_app_version, "1.0.0"),
                style = MaterialTheme.typography.s12.normal(),
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun OrbitSatellite(
    angleDegrees: Float,
    radiusDp: Float,
    iconRes: Int,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val angleRad = Math.toRadians(angleDegrees.toDouble())
    val offsetX = (radiusDp * cos(angleRad)).toInt()
    val offsetY = (radiusDp * sin(angleRad)).toInt()

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.dp.roundToPx(), offsetY.dp.roundToPx()) }
            .size(Dimen.SizeL)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(Dimen.SizeS)
        )
    }
}

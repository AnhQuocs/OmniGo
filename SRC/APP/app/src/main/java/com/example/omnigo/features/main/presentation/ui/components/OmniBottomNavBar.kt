package com.example.omnigo.features.main.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.features.main.presentation.navigation.BottomNavDestination
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.BottomBarBg
import com.example.omnigo.ui.theme.BottomBarBorder
import com.example.omnigo.ui.theme.BottomNavDefault
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s12

@Composable
fun OmniBottomNavBar(
    currentRoute: String?,
    onNavigateToDestination: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    promosCount: Int = 0
) {

    val pillShape = remember { RoundedCornerShape(AppShape.PillShape) }
    val inactiveColor = BottomNavDefault

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimen.PaddingSM, vertical = Dimen.PaddingS)
            .border(width = 1.dp, color = BottomBarBorder, shape = pillShape)
            .clip(pillShape)
            .background(color = BottomBarBg)
            .padding(all = Dimen.PaddingXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavDestination.items.forEach { destination ->
            val isSelected = currentRoute == destination.route
            val color = if (isSelected) PrimaryColor else inactiveColor

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onNavigateToDestination(destination)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(pillShape)
                        .then(
                            if (isSelected) {
                                Modifier.paint(
                                    painter = painterResource(R.drawable.bg_tab_selected),
                                )
                            } else {
                                Modifier
                            },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                ) {
                    Image(
                        painter = painterResource(destination.iconRes),
                        contentDescription = stringResource(destination.titleRes),
                        modifier = Modifier.size(Dimen.SizeS2),
                        colorFilter = ColorFilter.tint(color)
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.XXS))

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(destination.titleRes),
                        overflow = TextOverflow.Clip,
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.s10.copy(fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal),
                        textAlign = TextAlign.Center,
                        color = if (isSelected) PrimaryColor else inactiveColor
                    )
                }

                if (destination.route == BottomNavDestination.Promos.route && promosCount > 0) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 20.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = PrimaryColor,
                                shape = RoundedCornerShape(AppShape.PromosShape)
                            )
                            .padding(3.dp),
                    ) {
                        // numbers of live matches
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = if (promosCount > 99) "99+" else promosCount.toString(),
                            style = MaterialTheme.typography.s10.copy(color = Color.White),
                        )
                    }
                }
            }
        }
    }
}
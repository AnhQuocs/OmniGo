package com.example.omnigo.features.customer.activity.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.omnigo.R
import com.example.omnigo.features.customer.activity.presentation.viewmodel.ActivityTab
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.SecondaryColor
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.s10
import com.example.omnigo.utils.s14

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTabsSection(
    selectedTab: ActivityTab,
    ongoingCount: Int,
    onTabSelected: (ActivityTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = ActivityTab.entries
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)

    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        containerColor = SurfaceLight,
        contentColor = SecondaryColor,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex),
                color = SecondaryColor,
                height = Dimen.PaddingXXS
            )
        }
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            val title = when (tab) {
                ActivityTab.ONGOING -> stringResource(id = R.string.activity_tab_ongoing)
                ActivityTab.HISTORY -> stringResource(id = R.string.activity_tab_history)
            }

            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            style = if (isSelected) {
                                MaterialTheme.typography.s14.bold()
                            } else {
                                MaterialTheme.typography.s14.medium()
                            },
                            color = if (isSelected) SecondaryColor else TextSecondary
                        )

                        if (tab == ActivityTab.ONGOING && ongoingCount > 0) {
                            Spacer(modifier = Modifier.width(AppSpacing.XSPlus))
                            Box(
                                modifier = Modifier
                                    .size(Dimen.SizeTabBadge)
                                    .clip(CircleShape)
                                    .background(SecondaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ongoingCount.toString(),
                                    style = MaterialTheme.typography.s10.bold(),
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

package com.example.omnigo.features.customer.home.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.omnigo.R
import com.example.omnigo.core.region.domain.model.District
import com.example.omnigo.core.region.domain.model.Province
import com.example.omnigo.core.region.domain.model.Ward
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.CardBorderColor
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.PrimaryContainer
import com.example.omnigo.ui.theme.SurfaceLight
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextSecondary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.medium
import com.example.omnigo.utils.normal
import com.example.omnigo.utils.s12
import com.example.omnigo.utils.s13
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s16

enum class AddressPickerStep {
    PROVINCE, DISTRICT, WARD, STREET_DETAIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressPickerBottomSheet(
    provinces: List<Province>,
    districts: List<District>,
    wards: List<Ward>,
    selectedProvince: Province?,
    selectedDistrict: District?,
    selectedWard: Ward?,
    streetDetail: String,
    currentStep: AddressPickerStep,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProvinceSelect: (Province) -> Unit,
    onDistrictSelect: (District) -> Unit,
    onWardSelect: (Ward) -> Unit,
    onStreetDetailChange: (String) -> Unit,
    onStepChange: (AddressPickerStep) -> Unit,
    onConfirmAddress: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = AppShape.ShapeXL, topEnd = AppShape.ShapeXL),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = Dimen.PaddingM)
        ) {
            AddressPickerHeader(
                currentStep = currentStep,
                onBackClick = {
                    when (currentStep) {
                        AddressPickerStep.DISTRICT -> onStepChange(AddressPickerStep.PROVINCE)
                        AddressPickerStep.WARD -> onStepChange(AddressPickerStep.DISTRICT)
                        AddressPickerStep.STREET_DETAIL -> onStepChange(AddressPickerStep.WARD)
                        AddressPickerStep.PROVINCE -> onDismissRequest()
                    }
                },
                onCloseClick = onDismissRequest
            )

            AddressStepBreadcrumb(
                currentStep = currentStep,
                selectedProvince = selectedProvince,
                selectedDistrict = selectedDistrict,
                selectedWard = selectedWard,
                onStepClick = onStepChange
            )

            Spacer(modifier = Modifier.height(AppSpacing.S))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "AddressPickerStepAnimation"
            ) { step ->
                when (step) {
                    AddressPickerStep.PROVINCE -> {
                        RegionListStep(
                            searchHint = stringResource(id = R.string.location_search_province_hint),
                            searchQuery = searchQuery,
                            onSearchChange = onSearchQueryChange,
                            items = provinces.map { it.name },
                            selectedItem = selectedProvince?.name,
                            onItemClick = { index -> onProvinceSelect(provinces[index]) }
                        )
                    }
                    AddressPickerStep.DISTRICT -> {
                        RegionListStep(
                            searchHint = stringResource(id = R.string.location_search_district_hint),
                            searchQuery = searchQuery,
                            onSearchChange = onSearchQueryChange,
                            items = districts.map { it.name },
                            selectedItem = selectedDistrict?.name,
                            onItemClick = { index -> onDistrictSelect(districts[index]) }
                        )
                    }
                    AddressPickerStep.WARD -> {
                        RegionListStep(
                            searchHint = stringResource(id = R.string.location_search_ward_hint),
                            searchQuery = searchQuery,
                            onSearchChange = onSearchQueryChange,
                            items = wards.map { it.name },
                            selectedItem = selectedWard?.name,
                            onItemClick = { index -> onWardSelect(wards[index]) }
                        )
                    }
                    AddressPickerStep.STREET_DETAIL -> {
                        StreetDetailStep(
                            province = selectedProvince?.name ?: "",
                            district = selectedDistrict?.name ?: "",
                            ward = selectedWard?.name ?: "",
                            streetDetail = streetDetail,
                            onStreetDetailChange = onStreetDetailChange,
                            onConfirm = onConfirmAddress
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressPickerHeader(
    currentStep: AddressPickerStep,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep != AddressPickerStep.PROVINCE) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        } else {
            Spacer(modifier = Modifier.size(Dimen.SizeXL))
        }

        Text(
            text = stringResource(id = R.string.location_picker_title),
            style = MaterialTheme.typography.s16.bold(),
            color = TextPrimary
        )

        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun AddressStepBreadcrumb(
    currentStep: AddressPickerStep,
    selectedProvince: Province?,
    selectedDistrict: District?,
    selectedWard: Ward?,
    onStepClick: (AddressPickerStep) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.PaddingXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BreadcrumbChip(
            title = selectedProvince?.name ?: stringResource(id = R.string.location_step_province),
            isActive = currentStep == AddressPickerStep.PROVINCE,
            isDone = selectedProvince != null,
            onClick = { onStepClick(AddressPickerStep.PROVINCE) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(AppSpacing.XXS))

        BreadcrumbChip(
            title = selectedDistrict?.name ?: stringResource(id = R.string.location_step_district),
            isActive = currentStep == AddressPickerStep.DISTRICT,
            isDone = selectedDistrict != null,
            onClick = { if (selectedProvince != null) onStepClick(AddressPickerStep.DISTRICT) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(AppSpacing.XXS))

        BreadcrumbChip(
            title = selectedWard?.name ?: stringResource(id = R.string.location_step_ward),
            isActive = currentStep == AddressPickerStep.WARD,
            isDone = selectedWard != null,
            onClick = { if (selectedDistrict != null) onStepClick(AddressPickerStep.WARD) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BreadcrumbChip(
    title: String,
    isActive: Boolean,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(AppShape.ShapeXL2),
        color = if (isActive) PrimaryContainer else if (isDone) SurfaceLight else SurfaceLight.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) PrimaryColor else CardBorderColor
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = Dimen.PaddingXS),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = if (isActive) MaterialTheme.typography.s12.bold() else MaterialTheme.typography.s12.normal(),
                color = if (isActive) PrimaryColor else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RegionListStep(
    searchHint: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    items: List<String>,
    selectedItem: String?,
    onItemClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = searchHint, style = MaterialTheme.typography.s14.normal(), color = TextSecondary) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
            shape = RoundedCornerShape(AppShape.ShapeXL2),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = CardBorderColor,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.PaddingXL),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.location_empty_result),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items.size) { index ->
                    val name = items[index]
                    val isSelected = name == selectedItem

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(index) }
                            .padding(vertical = Dimen.PaddingM, horizontal = Dimen.PaddingS),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = if (isSelected) MaterialTheme.typography.s14.bold() else MaterialTheme.typography.s14.normal(),
                            color = if (isSelected) PrimaryColor else TextPrimary
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(Dimen.SizeS)
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorderColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun StreetDetailStep(
    province: String,
    district: String,
    ward: String,
    streetDetail: String,
    onStreetDetailChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.PaddingS)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppShape.ShapeL),
            color = PrimaryContainer.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(Dimen.PaddingM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(Dimen.SizeL)
                )

                Spacer(modifier = Modifier.width(AppSpacing.SPlus))

                Column {
                    Text(
                        text = "$ward, $district",
                        style = MaterialTheme.typography.s14.bold(),
                        color = TextPrimary
                    )
                    Text(
                        text = province,
                        style = MaterialTheme.typography.s12.normal(),
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.L))

        Text(
            text = stringResource(id = R.string.location_detail_label),
            style = MaterialTheme.typography.s14.bold(),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(AppSpacing.S))

        OutlinedTextField(
            value = streetDetail,
            onValueChange = onStreetDetailChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.location_detail_hint),
                    style = MaterialTheme.typography.s13.normal(),
                    color = TextSecondary
                )
            },
            shape = RoundedCornerShape(AppShape.ShapeM),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = CardBorderColor,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight
            ),
            minLines = 2,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(AppSpacing.XL))

        Button(
            onClick = onConfirm,
            enabled = streetDetail.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.SizeXLPlus),
            shape = RoundedCornerShape(AppShape.ShapeM),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text(
                text = stringResource(id = R.string.location_confirm_btn),
                style = MaterialTheme.typography.s14.bold(),
                color = TextWhite
            )
        }
    }
}

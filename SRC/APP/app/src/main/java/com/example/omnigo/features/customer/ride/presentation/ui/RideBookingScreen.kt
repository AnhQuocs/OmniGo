package com.example.omnigo.features.customer.ride.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnigo.R
import com.example.omnigo.features.customer.ride.presentation.ui.components.PricingEstimateSection
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideLocationPickerSection
import com.example.omnigo.features.customer.ride.presentation.ui.components.RideSearchingRadarDialog
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideBookingEvent
import com.example.omnigo.features.customer.ride.presentation.viewmodel.RideBookingViewModel
import com.example.omnigo.ui.dimens.AppShape
import com.example.omnigo.ui.dimens.AppSpacing
import com.example.omnigo.ui.dimens.Dimen
import com.example.omnigo.ui.theme.PrimaryColor
import com.example.omnigo.ui.theme.TextPrimary
import com.example.omnigo.ui.theme.TextWhite
import com.example.omnigo.utils.bold
import com.example.omnigo.utils.s14
import com.example.omnigo.utils.s18

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideBookingScreen(
    onBackClick: () -> Unit,
    viewModel: RideBookingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is RideBookingEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is RideBookingEvent.BookingCreated -> {
                    Toast.makeText(context, context.getString(R.string.ride_booking_in_progress), Toast.LENGTH_SHORT).show()
                }
                is RideBookingEvent.BookingCancelled -> {
                    Toast.makeText(context, context.getString(R.string.ride_booking_cancelled_success), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.ride_booking_title),
                        style = MaterialTheme.typography.s18.bold(),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = Dimen.PaddingS
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimen.PaddingM)
                ) {
                    val isButtonEnabled = uiState.pickupLocation != null &&
                            uiState.dropoffLocation != null &&
                            uiState.pricingEstimate != null &&
                            !uiState.isCreatingBooking &&
                            !uiState.isSearchingDriver &&
                            !uiState.isCalculatingPrice

                    Button(
                        onClick = viewModel::createBooking,
                        enabled = isButtonEnabled,
                        shape = RoundedCornerShape(AppShape.ShapeM),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor,
                            disabledContainerColor = PrimaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimen.HeightDefault)
                    ) {
                        if (uiState.isCreatingBooking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimen.SizeM),
                                color = TextWhite,
                                strokeWidth = Dimen.PaddingXXS
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.ride_book_now_button),
                                color = TextWhite,
                                style = MaterialTheme.typography.s14.bold()
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(Dimen.PaddingM)
        ) {
            // 1. Pickup & Dropoff Location Picker
            RideLocationPickerSection(
                pickupLocation = uiState.pickupLocation,
                dropoffLocation = uiState.dropoffLocation,
                isLocating = uiState.isLocatingCurrentPosition,
                savedAddresses = uiState.savedAddresses,
                onDetectCurrentLocation = viewModel::autoDetectPickupLocation,
                onSelectPickup = viewModel::setPickupLocation,
                onSelectDropoff = viewModel::setDropoffLocation,
                onSwapClick = viewModel::swapLocations
            )

            Spacer(modifier = Modifier.height(AppSpacing.L))

            // 2. Pricing Estimate Card
            PricingEstimateSection(
                isCalculating = uiState.isCalculatingPrice,
                pricingEstimate = uiState.pricingEstimate,
                errorMessage = uiState.pricingError
            )

            Spacer(modifier = Modifier.height(AppSpacing.XXL))
        }
    }

    // 3. Searching Radar Dialog when booking status is PENDING
    if (uiState.isSearchingDriver) {
        RideSearchingRadarDialog(
            countdownSeconds = uiState.searchCountdownSeconds,
            isCancelling = uiState.isCancellingBooking,
            onCancelClick = viewModel::cancelBooking
        )
    }
}

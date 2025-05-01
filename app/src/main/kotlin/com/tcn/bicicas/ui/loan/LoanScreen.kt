package com.tcn.bicicas.ui.loan

import android.content.Intent
import com.tcn.bicicas.ui.components.login.LoginDialog
import com.tcn.bicicas.ui.pin.PinState

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcn.bicicas.R
import com.tcn.bicicas.ui.components.ScrollableAlertDialog
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig
import org.koin.androidx.compose.getViewModel

@Composable
fun LoanScreen(padding: PaddingValues) {
    val viewModel: LoanViewModel = getViewModel()
    val state by viewModel.loanState.collectAsState()
    LoanScreen(state, padding, viewModel::logout, viewModel::login)
}

@Composable
fun LoanScreen(
    state: LoanState,
    padding: PaddingValues,
    onLogout: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    if (state.loggedIn) {
        LoanContent(state, padding, onLogout)
    } else {
        LoanWelcomeContent(state, padding, onLogin)
    }
}

@Composable
private fun LoanWelcomeContent(
    state: LoanState,
    padding: PaddingValues,
    onLogin: (String, String) -> Unit
) {
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var warningDialogShownInSession by rememberSaveable { mutableStateOf(false) }
    var showLoginDialog by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = stringResource(R.string.desbloqueo_mediante_codigo_qr),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.sizeIn(maxWidth = 600.dp),
            )
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = stringResource(R.string.pin_welcome_message),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.sizeIn(maxWidth = 600.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    if (!warningDialogShownInSession) {
                        showWarningDialog = true
                    } else {
                        showLoginDialog = true
                    }
                },
                content = { Text(stringResource(R.string.pin_login)) }
            )
            Spacer(modifier = Modifier.weight(1f))

        }
    }

    if (showWarningDialog) {
        ScrollableAlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text(stringResource(R.string.pin_welcome_title_warning)) },
            text = { Text(stringResource(R.string.pin_welcome_message_warning)) },
            dismissButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text(stringResource(R.string.popup_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showWarningDialog = false
                    warningDialogShownInSession = true
                    showLoginDialog = true
                }) {
                    Text(stringResource(R.string.pin_welcome_button_continue))
                }
            }
        )
    }

    if (showLoginDialog) {
        LoginDialog(
            loginError = state.loginError,
            isLoading = state.loading,
            onDismissRequest = { showLoginDialog = false },
            onLogin = onLogin,
        )
    }

}


@Composable
private fun LoanContent(state: LoanState, padding: PaddingValues, onLogout: () -> Unit) {
    var displayLogoutDialog by rememberSaveable { mutableStateOf(false) }
    val onLogoutClicked = { displayLogoutDialog = true }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            PortraitLoanContent(state, onLogoutClicked)
        } else {
            LandscapeLoanContent(state, onLogoutClicked)
        }
    }

    if (displayLogoutDialog) {
        AlertDialog(
            onDismissRequest = { displayLogoutDialog = false },
            title = { Text(stringResource(R.string.pin_logout)) },
            text = { Text(stringResource(R.string.pin_logout_popup_text)) },
            confirmButton = { TextButton(onClick = onLogout) { Text(stringResource(R.string.popup_accept)) } },
            dismissButton = {
                TextButton(onClick = { displayLogoutDialog = false }) {
                    Text(
                        stringResource(R.string.popup_cancel)
                    )
                }
            },
        )
    }

}

@Composable
private fun PortraitLoanContent(state: LoanState, onLogoutButtonClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logout
        TextButton(
            onClick = onLogoutButtonClicked,
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Text(stringResource(R.string.pin_logout))
        }
        val ctx = LocalContext.current
        val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanCustomCode()) { result ->
            val text = when (result) {
                is QRResult.QRSuccess -> {
                    result.content.rawValue
                    // decoding with default UTF-8 charset when rawValue is null will not result in meaningful output, demo purpose
                        ?: result.content.rawBytes?.let { String(it) }.orEmpty()
                }
                QRResult.QRUserCanceled -> "User canceled"
                QRResult.QRMissingPermission -> "Missing permission"
                is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
            }
            // handle QRResult
            Toast.makeText(ctx, text, Toast.LENGTH_LONG).show()
        }

        Button(
            onClick = {
                scanQrCodeLauncher.launch( ScannerConfig.build {
                    setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE)) // set interested barcode formats
                    setOverlayStringRes(R.string.app_name) // string resource used for the scanner overlay
                    //setOverlayDrawableRes(R.drawable.ic_scan_barcode) // drawable resource used for the scanner overlay
                    setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
                    setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
                    setShowCloseButton(true) // show or hide (default) close button
                    setHorizontalFrameRatio(1f) // set the horizontal overlay ratio (default is 1 / square frame)
                    setUseFrontCamera(false) // use the front camera
                    setKeepScreenOn(true) // keep the device's screen turned on
                })
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Escanear código QR")
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LandscapeLoanContent(state: LoanState, onLogoutButtonClicked: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Logout button
        TextButton(
            onClick = onLogoutButtonClicked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Text(stringResource(R.string.pin_logout))
        }


    }
}

@Composable
private fun UserText(userNumber: String?, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = stringResource(R.string.pin_label_user),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = userNumber ?: "",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp)
        )
    }
}
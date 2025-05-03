package com.tcn.bicicas.ui.loan


import com.tcn.bicicas.ui.components.login.LoginDialog

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tcn.bicicas.R
import com.tcn.bicicas.ui.components.ScrollableAlertDialog
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig
import org.koin.androidx.compose.getViewModel

@Composable
fun LoanScreen(padding: PaddingValues) {
    val viewModel: LoanViewModel = getViewModel()
    val state by viewModel.loanState.collectAsState()
    LoanScreen(state, padding, viewModel::logout, viewModel::login, viewModel::loanBike, viewModel::onLoanMsgShown)
}

@Composable
fun LoanScreen(
    state: LoanState,
    padding: PaddingValues,
    onLogout: () -> Unit,
    onLogin: (String, String) -> Unit,
    loanBike: (String) -> Unit,
    onLoanMsgShown: () -> Unit
) {
    if (state.loggedIn) {
        LoanContent(state, padding, onLogout, loanBike, onLoanMsgShown)
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
                text = stringResource(R.string.loans_qr_code_unlocking_msg),
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
private fun LoanContent(state: LoanState, padding: PaddingValues, onLogout: () -> Unit, loanBike: (String) -> Unit,  onLoanMsgShown: () -> Unit) {
    var displayLogoutDialog by rememberSaveable { mutableStateOf(false) }
    val onLogoutClicked = { displayLogoutDialog = true }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            PortraitLoanContent(state, onLogoutClicked, loanBike)
        } else {
            LandscapeLoanContent(state, onLogoutClicked, loanBike)
        }
    }


    LoadingDialog(state.loanLoading)

    // show loanSuccess
    if (state.loanSuccess){
        AlertDialog(
            onDismissRequest = {         onLoanMsgShown() },
            title = { Text(stringResource(R.string.popup_success)) },
            text = { Text(stringResource(R.string.loans_success)) },
            confirmButton = { TextButton(onClick = onLoanMsgShown) { Text(stringResource(R.string.popup_accept)) } },
        )
    }

    // show loanError
    if (state.loanError != null){
        val errMsg = when(state.loanError){
            LoanState.LoanError.Unauthenticated -> stringResource(R.string.loans_error_unauthenticated)
            LoanState.LoanError.NoBicycle -> stringResource(R.string.loans_error_no_bicycle)
            LoanState.LoanError.NoQRCode -> stringResource(R.string.loans_error_no_qr)
            LoanState.LoanError.Network -> stringResource(R.string.loans_error_network)
            LoanState.LoanError.Unknown -> stringResource(R.string.loans_error_unknown)
        }

        AlertDialog(
            onDismissRequest = {         onLoanMsgShown() },
            title = { Text(stringResource(R.string.popup_error_title)) },
            text = { Text(errMsg) },
            confirmButton = { TextButton(onClick = onLoanMsgShown) { Text(stringResource(R.string.popup_accept)) } },
        )


    }

    if (displayLogoutDialog) {
        AlertDialog(
            onDismissRequest = { displayLogoutDialog = false },
            title = { Text(stringResource(R.string.logout)) },
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
private fun PortraitLoanContent(
    state: LoanState,
    onLogoutButtonClicked: () -> Unit,
    loanBike:(String) -> Unit
) {
    val inputText = remember { mutableStateOf("") }

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
            Text(stringResource(R.string.logout))
        }

        Spacer(Modifier.weight(0.1f))


        // Manual number entering
        OutlinedTextField(
            value = inputText.value,
            onValueChange = { newText ->
                val filteredText = newText.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(3)
                inputText.value = filteredText },
            label = { Text(stringResource(R.string.loans_qr_code_text_input)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )

        Button(
            onClick = {
                loanBike(inputText.value)
            },
            enabled = inputText.value.length == 3,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.loans_unlock_btn))
        }

        // QR code scan
        val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanCustomCode()) { result ->
            when (result) {
                is QRResult.QRSuccess -> {
                    result.content.rawValue?.let{loanBike(it)}

                }
                QRResult.QRUserCanceled -> {
                    Log.e("QR", "UserCanceled")
                }
                QRResult.QRMissingPermission -> {
                    Log.e("QR", "MissingPermission")
                }
                is QRResult.QRError -> {
                    Log.e("QR","${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}")
                }
            }
        }

        Spacer(Modifier.weight(0.8f))

        Button(
            onClick = {
                scanQrCodeLauncher.launch(ScannerConfig.build {
                    setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE))
                    setOverlayStringRes(R.string.loans_qr_scan_string)
                    setHapticSuccessFeedback(false)
                    setShowTorchToggle(true)
                    setShowCloseButton(true)
                    setHorizontalFrameRatio(1f)
                    setUseFrontCamera(false)
                    setKeepScreenOn(true)
                })
            },
            modifier = Modifier
                .padding(16.dp)
                .height(120.dp)  // Make the button taller
                .fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.loans_qr_scan_string),
                fontSize = 24.sp)
        }
        Spacer(Modifier.weight(0.2f))

    }
}


@Composable
private fun LandscapeLoanContent(
    state: LoanState,
    onLogoutButtonClicked: () -> Unit,
    loanBike: (String) -> Unit
) {
    val inputText = remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Panel: Manual Input and Logout
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logout Button
            TextButton(
                onClick = onLogoutButtonClicked,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = 12.dp)
            ) {
                Text(stringResource(R.string.logout))
            }

            // Manual Text Input
            OutlinedTextField(
                value = inputText.value,
                onValueChange = { newText ->
                    val filteredText = newText.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(3)
                    inputText.value = filteredText
                },
                label = { Text(stringResource(R.string.loans_qr_code_text_input)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )

            // Unlock Button
            Button(
                onClick = {
                    loanBike(inputText.value)
                },
                enabled = inputText.value.length == 3,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.loans_unlock_btn))
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Right Panel: QR Code Scanner
        val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanCustomCode()) { result ->
            when (result) {
                is QRResult.QRSuccess -> {
                    result.content.rawValue?.let { loanBike(it) }
                }
                QRResult.QRUserCanceled -> Log.e("QR", "UserCanceled")
                QRResult.QRMissingPermission -> Log.e("QR", "MissingPermission")
                is QRResult.QRError -> Log.e("QR", "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}")
            }
        }

        Button(
            onClick = {
                scanQrCodeLauncher.launch(ScannerConfig.build {
                    setBarcodeFormats(listOf(BarcodeFormat.FORMAT_QR_CODE))
                    setOverlayStringRes(R.string.loans_qr_scan_string)
                    setHapticSuccessFeedback(false)
                    setShowTorchToggle(true)
                    setShowCloseButton(true)
                    setHorizontalFrameRatio(1f)
                    setUseFrontCamera(false)
                    setKeepScreenOn(true)
                })
            },
            modifier = Modifier
                .weight(1f)
                .height(160.dp)
                .fillMaxHeight()
        ) {
            Text(
                text = stringResource(R.string.loans_qr_scan_string),
                fontSize = 24.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@Composable
fun LoadingDialog(isShowingDialog: Boolean) {
    if (isShowingDialog) {
        Dialog(
            onDismissRequest = { },
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        ,
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp
                )
            }
        }
    }
}
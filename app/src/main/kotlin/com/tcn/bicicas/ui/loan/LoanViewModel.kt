package com.tcn.bicicas.ui.loan


import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.data.model.HttpError
import com.tcn.bicicas.data.model.Loan
import com.tcn.bicicas.data.model.NetworkError
import com.tcn.bicicas.data.repository.LoanRepository
import com.tcn.bicicas.ui.components.login.LoginError
import com.tcn.bicicas.ui.tickerFlow
import io.github.g00fy2.quickie.QRResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoanViewModel(
    private val loanRepository: LoanRepository,
) : ViewModel() {

    private val _loanState = MutableStateFlow(LoanState())
    val loanState: StateFlow<LoanState> = _loanState.asStateFlow()

    init {

        // Update auth state
        loanRepository.authenticatedState.onEach { loggedIn ->
            _loanState.update { state -> state.copy(loggedIn = loggedIn) }
        }.launchIn(viewModelScope)

    }

    fun login(username: String, password: String) {
        _loanState.update { it.copy(loading = true, loginError = null) }
        viewModelScope.launch {
            loanRepository.authenticate(username, password)
                .onSuccess { twoFactorAuth ->
                    _loanState.update { state ->
                        state.copy(
                            loggedIn = true,
                            loading = false,
                        )
                    }
                }.onFailure { error ->
                    val loginError = when (error) {
                        is HttpError -> LoginError.WrongUserPass
                        is NetworkError -> LoginError.Network
                        else -> LoginError.Unknown
                    }
                    _loanState.update { state ->
                        state.copy(loginError = loginError, loading = false)
                    }
                }
        }
    }

    fun logout() {
        loanRepository.logout()
    }

    fun loanBike(bikeCode: String) {
        _loanState.update { it.copy(loanLoading = true, loanError = null) }

        viewModelScope.launch {
            loanRepository.loanBike(bikeCode).onSuccess {
                _loanState.update { state ->
                    state.copy(
                        loanSuccess = true,
                        loanLoading = false
                    )
                }
            }.onFailure { error ->
                val loanError = when (error) {
                    is HttpError -> {
                        if (error.code == 403){
                            logout()
                            LoanState.LoanError.Unauthenticated
                        } else {
                            val errorStr = error.errorMessage?.let { JSONObject(it).getString("errors") }
                            if (errorStr == "EXCEPTION_QR_NOT_EXIST"){
                                LoanState.LoanError.NoQRCode
                            } else if (errorStr == "EXCEPTION_BICYCLE_NOT_EXIST"){
                                LoanState.LoanError.NoBicycle
                            } else {
                                LoanState.LoanError.Unknown
                            }
                        }
                    }
                    is NetworkError -> LoanState.LoanError.Network
                    else -> LoanState.LoanError.Unknown
                }

                _loanState.update { state ->
                    state.copy(loanError = loanError, loanLoading = false)
                }
            }
        }
    }

    fun onLoanMsgShown() {
        _loanState.update { it.copy(loanSuccess = false, loanError = null) }
    }



}
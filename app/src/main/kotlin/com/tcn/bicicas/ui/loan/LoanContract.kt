package com.tcn.bicicas.ui.loan


import androidx.compose.runtime.Immutable
import com.tcn.bicicas.ui.components.login.LoginError


@Immutable
data class LoanState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val loginError: LoginError? = null,
)

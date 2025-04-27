package com.tcn.bicicas.data.repository

import com.tcn.bicicas.data.datasource.local.LocalStore
import com.tcn.bicicas.data.datasource.remote.SecretApi
import com.tcn.bicicas.data.model.Token
import com.tcn.bicicas.data.model.TwoFactorAuth
import com.tcn.bicicas.data.resultOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoanRepository(
    private val secretApi: SecretApi,
    private val tokenAuthStore: LocalStore<Token>
) {

    private val _authenticatedState = MutableStateFlow(false)
    val authenticatedState: StateFlow<Boolean> = _authenticatedState.asStateFlow()

    init {
        tokenAuthStore.get()?.let { tokenAuth ->
            CoroutineScope(Dispatchers.IO).launch {
                checkToken(tokenAuth.value).onSuccess{
                    onTokenObtained(tokenAuth)

                }
            }
        }
    }
    suspend fun authenticate(username: String, password: String): Result<Token> {
        return doAuthRequest(username, password)
            .onSuccess { token -> onTokenObtained(token) }
    }

    fun logout() {
        tokenAuthStore.clear()
        _authenticatedState.update { false }
    }

    private suspend fun doAuthRequest(username: String, password: String): Result<Token> {
        return resultOf {
            secretApi.authenticate(
                username = username,
                password = password,
            )
        }.map { (_, token) -> token }
    }

    @Synchronized
    private fun onTokenObtained(token: Token) {
        tokenAuthStore.save(token)
        _authenticatedState.update { true }
    }

    fun getToken(): Token? {
        return tokenAuthStore.get()
    }

    private suspend fun checkToken(token: String): Result<TwoFactorAuth> {
        return resultOf { secretApi.getTwoFactorAuth("Bearer $token") }
            .map { (_, twoFactorAuth) -> twoFactorAuth }
    }

}
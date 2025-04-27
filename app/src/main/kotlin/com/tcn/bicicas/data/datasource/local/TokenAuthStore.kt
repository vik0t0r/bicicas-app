package com.tcn.bicicas.data.datasource.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.tcn.bicicas.data.model.Token
import org.jasypt.util.text.BasicTextEncryptor


class TokenAuthStore(
    private val preferences: SharedPreferences,
    password: CharArray
) : LocalStore<Token> {

    companion object {
        private const val TOKEN_KEY = "tokenAuth.token"
    }

    private val encryptor = BasicTextEncryptor().apply { setPasswordCharArray(password) }

    override fun get(): Token? = runCatching {
        val token = preferences.getString(TOKEN_KEY, null)?.let(encryptor::decrypt)
        Token( token ?: return null)
    }.getOrNull()

    override fun save(value: Token) =
        preferences.edit {
            putString(TOKEN_KEY, encryptor.encrypt(value.value))
        }

    override fun clear() =
        preferences.edit {
            remove(TOKEN_KEY)
        }


}

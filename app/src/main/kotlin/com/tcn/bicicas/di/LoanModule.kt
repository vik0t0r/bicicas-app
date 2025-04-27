package com.tcn.bicicas.di

import com.tcn.bicicas.BuildConfig
import com.tcn.bicicas.data.datasource.local.TokenAuthStore
import com.tcn.bicicas.data.repository.LoanRepository
import com.tcn.bicicas.ui.loan.LoanViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

fun loanModule(baseUrl: String = BuildConfig.OAUTH_ENDPOINT) = module {
    single {
        LoanRepository(
            secretApi = get<Retrofit.Builder>().baseUrl(baseUrl).build()
                .create(),
            tokenAuthStore = TokenAuthStore(get(), BuildConfig.ENCRYPT_PASSWORD.toCharArray())
        )
    }

    viewModel { LoanViewModel(get()) }

}
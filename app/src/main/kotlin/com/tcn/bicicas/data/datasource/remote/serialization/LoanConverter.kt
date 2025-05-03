package com.tcn.bicicas.data.datasource.remote.serialization

import com.tcn.bicicas.data.model.Loan
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter

class LoanConverter : Converter<ResponseBody, Loan> {
    override fun convert(value: ResponseBody): Loan {
        return JSONObject(value.string())
            .run { Loan(getString("use_uuid")) }
    }
}
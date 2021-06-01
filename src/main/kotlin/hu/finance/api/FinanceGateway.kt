package hu.finance.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.dto.StockDto
import hu.finance.api.dto.toStock
import hu.finance.api.model.BalanceSheet
import okhttp3.OkHttpClient
import okhttp3.Request

interface FinanceGateway {
    fun balanceSheet(ticker: String, exchange: String? = null): BalanceSheet
}

abstract class FinanceGatewayBase(
    protected val limiter: RateLimiter
) : FinanceGateway {
    protected val om = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}

class YahooApi(
    limiter: RateLimiter
) : FinanceGatewayBase(limiter) {

    companion object Constants {
        const val HOST = "https://query2.finance.yahoo.com/v10"
    }

    private val modules: List<String> by lazy {
        om.readValue(javaClass.getResource("/api/yahoo_api_modules.json")!!)
    }
    private val client = OkHttpClient.Builder().build()

    override fun balanceSheet(ticker: String, exchange: String?): BalanceSheet {
        val request = Request.Builder()
            .url("$HOST/finance/quoteSummary/$ticker?modules=${modules.joinToString(separator = ",") { it }}")
            .get()
            .build()
        limiter.acquire()
        return client.newCall(request).execute().use {
            requireNotNull(
                it.apply { check(code == 200) { "API call failed with: $code!" } }.body
            ) { "API response body can not be empty!" }.string()
        }.let { om.readValue<StockDto>(it) }.toStock()
    }

}
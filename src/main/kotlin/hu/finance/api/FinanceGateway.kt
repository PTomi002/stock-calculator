@file:Suppress("UnstableApiUsage")

package hu.finance.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.dto.QuoteDto
import hu.finance.api.dto.TimeSeriesDto
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.time.Instant

interface FinanceGateway {
    fun quote(ticker: String, filters: List<String>): QuoteDto
    fun timeSeries(ticker: String, filters: List<String>): TimeSeriesDto
}

abstract class FinanceGatewayBase(
    protected val limiter: RateLimiter
) : FinanceGateway {
    protected val om = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    protected fun Response.allowStatus(vararg codes: Int) = apply {
        check(codes.contains(code)) { "API call failed with: $code! and message: ${body?.string()}" }
    }
}

class YahooApi(
    limiter: RateLimiter
) : FinanceGatewayBase(limiter) {

    companion object Constants {
        const val QUOTE_SUMMARY_HOST = "https://query2.finance.yahoo.com/v10"
        const val TIMESERIES_HOST = "https://query1.finance.yahoo.com/ws"
    }

    private val client = OkHttpClient.Builder().build()

    override fun quote(ticker: String, filters: List<String>): QuoteDto {
        val request = Request.Builder()
            .url(
                "$QUOTE_SUMMARY_HOST/finance/quoteSummary/" +
                    "$ticker?modules=${filters.joinToString(separator = ",") { it }}"
            )
            .get()
            .build()
        limiter.acquire()
        return client.newCall(request).execute()
            .use { requireNotNull(it.allowStatus(200).body) { "API response body can not be empty!" }.string() }
            .let { om.readValue(it) }
    }

    override fun timeSeries(ticker: String, filters: List<String>): TimeSeriesDto {
        val request = Request.Builder()
            .url(
                "$TIMESERIES_HOST/fundamentals-timeseries/v1/finance/timeseries/" +
                    "$ticker?type=${filters.joinToString(separator = ",") { it }}" +
                    "&period1=${Instant.parse("2000-01-01T00:00:00Z").epochSecond}" +
                    "&period2=${Instant.now().epochSecond}" +
                    "&merge=false"
            )
            .get()
            .build()
        limiter.acquire()
        return client.newCall(request).execute()
            .use { requireNotNull(it.allowStatus(200).body) { "API response body can not be empty!" }.string() }
            .let { om.readValue(it) }
    }

}
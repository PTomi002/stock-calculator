@file:Suppress("UnstableApiUsage")

package hu.finance.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.util.concurrent.RateLimiter
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.jayway.jsonpath.ReadContext
import hu.finance.api.dto.*
import net.minidev.json.JSONArray
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.math.BigDecimal
import java.time.Instant

typealias NestedMap = Map<String, Map<String, Any>>

interface FinanceGateway {
    fun quote(ticker: String, filters: List<String>): QuoteDto
    fun timeSeries(ticker: String, filters: List<String>): TimeSeriesDto
    fun chart(ticker: String): ChartDto
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
        const val CHARTS_HOST = "https://query1.finance.yahoo.com/v8"
    }

    private val client = OkHttpClient.Builder().build()

    override fun quote(ticker: String, filters: List<String>): QuoteDto {
        val request = Request.Builder()
            .url(
                "$QUOTE_SUMMARY_HOST/finance/quoteSummary/" +
                    "$ticker?modules=${filters.joinToString(separator = ",") { it }}"
            )
            .cacheControl(CacheControl.FORCE_NETWORK)
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
            .cacheControl(CacheControl.FORCE_NETWORK)
            .get()
            .build()
        limiter.acquire()
        return client.newCall(request).execute()
            .use { requireNotNull(it.allowStatus(200).body) { "API response body can not be empty!" }.string() }
            .let { om.readValue(it) }
    }

    override fun chart(ticker: String): ChartDto {
        val request = Request.Builder()
            .url(
                "$CHARTS_HOST/finance/chart/$ticker" +
                    "?formatted=true" +
                    "&includeAdjustedClose=false" +
                    "&interval=1mo" +
                    "&period1=${Instant.parse("1970-01-01T00:00:00Z").epochSecond}" +
                    "&period2=${Instant.now().epochSecond}" +
                    "&events=div|split"
            )
            .cacheControl(CacheControl.FORCE_NETWORK)
            .get()
            .build()
        limiter.acquire()
        return client.newCall(request).execute()
            .use { requireNotNull(it.allowStatus(200).body) { "API response body can not be empty!" }.string() }
            .let { JsonPath.parse(it) }
            .let {
                val timestamps = it.readWith<JSONArray>("\$.chart.result[0].timestamp")
                val openQuotes = it.readWith<JSONArray>("\$.chart.result[0].indicators.quote[0].open")
                ChartDto(
                    splitEvents = it.readWith<NestedMap>("\$.chart.result[0].events.splits")?.toSplitEvent(),
                    quoteOpens = timestamps?.zip(openQuotes ?: emptyList())?.toChartDataDto()
                )
            }
    }

}

private fun <T> ReadContext.readWith(path: String) =
    try {
        read<T>(path)
    } catch (ex: PathNotFoundException) {
        null
    }

private fun List<Pair<*, *>>.toChartDataDto() =
    mapNotNull {
        try {
            ChartDataDto(
                date = Instant.ofEpochSecond((it.first as Int).toLong()),
                value = when (it.second) {
                    is Double -> (it.second as Double).toBigDecimal()
                    is BigDecimal -> it.second as BigDecimal
                    else -> throw IllegalArgumentException("Can not convert chart data: ${it.second}!")
                }
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

private fun Map<String, Map<String, Any>>.toSplitEvent() =
    map { (_, value) ->
        SplitEventDto(
            date = Instant.ofEpochSecond((value["date"] as Int).toLong()),
            numerator = value["numerator"] as Int,
            denominator = value["denominator"] as Int
        )
    }

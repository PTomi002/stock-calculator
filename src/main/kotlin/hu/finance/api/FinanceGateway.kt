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
import okhttp3.*
import java.math.BigDecimal
import java.time.Instant

typealias NestedMap = Map<String, Map<String, Any>>

interface FinanceGateway {
    fun quote(ticker: String, filters: List<String>): QuoteDto
    fun timeSeries(ticker: String, filters: List<String>, periodStart: Instant): TimeSeriesDto
    fun chart(ticker: String, periodStart: Instant): ChartDto
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

    private val tsPath = JsonPath.compile("\$.chart.result[0].timestamp")
    private val openQuotesPath = JsonPath.compile("\$.chart.result[0].indicators.quote[0].open")
    private val splitEventsPath = JsonPath.compile("\$.chart.result[0].events.splits")
    private val client = OkHttpClient
        .Builder()
        .connectionSpecs(
            listOf(
                ConnectionSpec
                    .Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build()
            )
        )
        .build()

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

    override fun timeSeries(ticker: String, filters: List<String>, periodStart: Instant): TimeSeriesDto {
        val request = Request.Builder()
            .url(
                "$TIMESERIES_HOST/fundamentals-timeseries/v1/finance/timeseries/" +
                    "$ticker?type=${filters.joinToString(separator = ",") { it }}" +
                    "&period1=${periodStart.epochSecond}" +
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

    override fun chart(ticker: String, periodStart: Instant): ChartDto {
        val request = Request.Builder()
            .url(
                "$CHARTS_HOST/finance/chart/$ticker" +
                    "?formatted=true" +
                    "&includeAdjustedClose=false" +
                    "&interval=1mo" +
                    "&period1=${periodStart.epochSecond}" +
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
                val timestamps = it.readWith<JSONArray>(tsPath)
                val openQuotes = it.readWith<JSONArray>(openQuotesPath)
                ChartDto(
                    splitEvents = it.readWith<NestedMap>(splitEventsPath)?.toSplitEvent(),
                    quoteOpens = timestamps?.zip(openQuotes ?: emptyList())?.toChartDataDto()
                )
            }
    }

}

private fun <T> ReadContext.readWith(path: JsonPath) =
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

package hu.finance.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.dto.BalanceSheetDto
import hu.finance.api.dto.toBalanceSheet
import hu.finance.api.model.BalanceSheet
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Information from markets:
 * 1. MorningStar.com
 * 2. valuline.com
 * 3. finance.yahoo.com
 * 4. macrotrends.net
 * 5. iocharts.com
 * 6. gurufocus.com
 * 7. https://www.sec.gov/edgar/searchedgar/companysearch.html
 * 7. https://sec.report/Ticker/TEVA
 */
interface FinanceGateway {
    /**
     * From 10-K and 10-Q SEC files.
     * ticker = AAPL or TEVA...
     * exchange = NASDAQ or US (virtual broker) or ...
     */
    fun balanceSheet(ticker: String, exchange: String): BalanceSheet
}

@Suppress("UnstableApiUsage")
abstract class FinanceGatewayBase(
    protected val limiter: RateLimiter
) : FinanceGateway

@Suppress("UnstableApiUsage")
class YahooFinanceAPI(
    limiter: RateLimiter
) : FinanceGatewayBase(limiter) {

    companion object Constants {
        const val HOST = "https://apidojo-yahoo-finance-v1.p.rapidapi.com"
        const val API_TOKEN_HEADER = "x-rapidapi-key"
        const val API_TOKEN_KEY = "a1711fd730mshb4db56ceb190a19p1e976cjsnc5452355d6c4"
        const val API_HOST_HEADER = "x-rapidapi-host"
        const val API_HOST_KEY = "apidojo-yahoo-finance-v1.p.rapidapi.com"
    }

    private val om = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request().newBuilder()
                .addHeader(API_TOKEN_HEADER, API_TOKEN_KEY)
                .addHeader(API_HOST_HEADER, API_HOST_KEY)
                .build()
            it.proceed(request)
        }
        .build()

    override fun balanceSheet(ticker: String, exchange: String): BalanceSheet {
        val request = Request.Builder()
            .url("$HOST/stock/v2/get-balance-sheet?symbol=$ticker&region=$exchange")
            .get()
            .build()
        limiter.acquire()
        return requireNotNull(
            client.newCall(request).execute().apply { check(code == 200) { "API call failed with: $code!" } }.body
        ) { "API response body can not be null!" }
            .string()
            .let { om.readValue<BalanceSheetDto>(it) }
            .toBalanceSheet()
    }

}

package hu.finance.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import hu.finance.api.FinanceGateway
import hu.finance.api.dto.ChartDto
import hu.finance.api.dto.QuoteDto
import hu.finance.api.dto.TimeSeriesDataDto
import hu.finance.api.dto.TimeSeriesDto
import hu.finance.model.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

private val formatter = DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd")
    .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
    .toFormatter()
    .withZone(ZoneOffset.UTC)

interface Finances {
    fun loadQuote(
        ticker: String,
        tsStart: Instant = Instant.parse("2000-01-01T00:00:00Z"),
        chStart: Instant = Instant.parse("1970-01-01T00:00:00Z")
    ): CompositeQuote
}

data class CompositeQuote(
    val quote: Quote,
    val timeSeries: TimeSeries,
    val chart: Chart
)

class FinanceService(
    private val pool: ExecutorService,
    private val financeGateway: FinanceGateway
) : Finances {

    private val quoteModules by lazy {
        jacksonObjectMapper().readValue<List<String>>(javaClass.getResource("/api/yahoo_api_modules.json")!!)
    }
    private val timeSeriesModules by lazy {
        jacksonObjectMapper().readValue<List<String>>(javaClass.getResource("/api/yahoo_api_timeseries_modules.json")!!)
    }

    override fun loadQuote(ticker: String, tsStart: Instant, chStart: Instant): CompositeQuote {
        val qF = CompletableFuture.supplyAsync({ financeGateway.quote(ticker, quoteModules) }, pool)
        val tsF = CompletableFuture.supplyAsync({ financeGateway.timeSeries(ticker, timeSeriesModules, tsStart) }, pool)
        val chF = CompletableFuture.supplyAsync({ financeGateway.chart(ticker, chStart) }, pool)
        CompletableFuture.allOf(qF, tsF, chF).get()
        return CompositeQuote(
            quote = qF.get().toQuote(),
            timeSeries = tsF.get().toTimeSeries(),
            chart = chF.get().toChart()
        )
    }
}

private fun ChartDto.toChart() =
    Chart(
        splitEvents = splitEvents?.map {
            SplitEvent(
                date = it.date!!,
                numerator = it.numerator!!,
                denominator = it.denominator!!
            )
        } ?: emptyList(),
        quoteOpens = quoteOpens?.map {
            ChartData(
                date = it.date!!,
                value = it.value!!
            )
        } ?: emptyList()
    )

private fun TimeSeriesDto.toTimeSeries() = timeseries?.result
    ?.run {
        TimeSeries(
            annualTotalAssets = flatMap { tsData ->
                tsData.annualTotalAssets?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualTotalLiabilitiesNetMinorityInterest = flatMap { tsData ->
                tsData.annualTotalLiabilitiesNetMinorityInterest?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualNetIncomeCommonStockholders = flatMap { tsData ->
                tsData.annualNetIncomeCommonStockholders?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualEBIT = flatMap { tsData ->
                tsData.annualEBIT?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualTotalCapitalization = flatMap { tsData ->
                tsData.annualTotalCapitalization?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualShareIssued = flatMap { tsData ->
                tsData.annualShareIssued?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualStockholdersEquity = flatMap { tsData ->
                tsData.annualStockholdersEquity?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualCapitalExpenditure = flatMap { tsData ->
                tsData.annualCapitalExpenditure?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualLongTermDebt = flatMap { tsData ->
                tsData.annualLongTermDebt?.map { it.toTimeSeriesData() } ?: emptyList()
            },
            annualCashFlowFromContinuingOperatingActivities = flatMap { tsData ->
                tsData.annualCashFlowFromContinuingOperatingActivities?.map { it.toTimeSeriesData() } ?: emptyList()
            }
        )
    } ?: TimeSeries()

private fun TimeSeriesDataDto.toTimeSeriesData() = TimeSeriesData(
    date = formatter.parse(asOfDate!!, Instant::from),
    value = reportedValue!!.raw.toBigDecimal()
)

private fun QuoteDto.toQuote() = quoteSummary!!.result!!.first().run {
    Quote(
        shareSummary = ShareSummary(
            open = summaryDetail!!.open!!.raw.toBigDecimal(),
            previousClose = summaryDetail.previousClose!!.raw.toBigDecimal(),
            currency = Currency.getInstance(price!!.currency),
            price = financialData?.currentPrice?.raw?.toBigDecimal() ?: price.regularMarketPrice!!.raw.toBigDecimal(),
            ttmPE = summaryDetail.trailingPE?.raw?.toDouble() ?: Double.NaN
        ),
        quoteSummary = QuoteSummary(
            longName = price.longName ?: "",
            shortName = price.shortName!!,
            exchange = price.exchangeName!!,
            type = price.quoteType!!
        )
    )
}

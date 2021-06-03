package hu.finance.service

import hu.finance.api.FinanceGateway
import hu.finance.api.dto.*
import hu.finance.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

interface Finances {
    fun loadQuote(ticker: String): CompositeQuote
}

data class CompositeQuote(
    val quote: Quote,
    val timeSeries: TimeSeries
)

class FinanceService(
    private val pool: ExecutorService,
    private val financeGateway: FinanceGateway
) : Finances {

    private val quoteModules = listOf(
        "balanceSheetHistory",
        "incomeStatementHistory",
        "summaryDetail",
        "price",
        "cashflowStatementHistory"
    )
    private val timeSeriesModules = listOf(
        "annualTotalCapitalization",
        "annualCapitalExpenditure",
        "annualShareIssued",
        "annualLongTermDebt",
        "annualStockholdersEquity"
    )

    override fun loadQuote(ticker: String): CompositeQuote {
        val qF = CompletableFuture.supplyAsync({ financeGateway.quote(ticker, quoteModules) }, pool)
        val tsF = CompletableFuture.supplyAsync({ financeGateway.timeSeries(ticker, timeSeriesModules) }, pool)
        CompletableFuture.allOf(qF, tsF).get()
        return CompositeQuote(
            quote = qF.get().toQuote(),
            timeSeries = tsF.get().toTimeSeries()
        )
    }
}

fun TimeSeriesDto.toTimeSeries() = timeseries.result.run {
    TimeSeries(
        annualShareIssued = flatMap { tsData ->
            tsData.annualShareIssued?.map { it.toTimeSeriesData() } ?: emptyList()
        },
        annualLongTermDebt = flatMap { tsData ->
            tsData.annualLongTermDebt?.map { it.toTimeSeriesData() } ?: emptyList()
        },
        annualStockholdersEquity = flatMap { tsData ->
            tsData.annualStockholdersEquity?.map { it.toTimeSeriesData() } ?: emptyList()
        },
        annualCapitalExpenditure = flatMap { tsData ->
            tsData.annualCapitalExpenditure?.map { it.toTimeSeriesData() } ?: emptyList()
        },
        annualTotalCapitalization = flatMap { tsData ->
            tsData.annualTotalCapitalization?.map { it.toTimeSeriesData() } ?: emptyList()
        }
    )
}

private fun TimeSeriesDataDto.toTimeSeriesData() = TimeSeriesData(
    date = LocalDate.parse(asOfDate).atStartOfDay().toInstant(ZoneOffset.UTC),
    value = reportedValue.raw.toBigDecimal()
)

fun QuoteDto.toQuote() = quoteSummary.result.first().run {
    Quote(
        shareSummary = ShareSummary(
            open = summaryDetail!!.open.raw.toBigDecimal(),
            previousClose = summaryDetail.previousClose.raw.toBigDecimal(),
            currency = Currency.getInstance(price!!.currency)
        ),
        quoteSummary = QuoteSummary(
            name = price.longName,
            exchange = price.exchangeName
        ),
        cashFlowStatements = cashflowStatementHistory!!.cashflowStatements.map { it.toCashFlowStatement() },
        balanceSheetStatements = balanceSheetHistory!!.balanceSheetStatements.map { it.toBalanceSheetStatement() },
        incomeStatements = incomeStatementHistory!!.incomeStatementHistory.map { it.toIncomeStatement() }
    )
}

private fun CashFlowStatementDto.toCashFlowStatement() = CashFlowStatement(
    date = Instant.ofEpochSecond(endDate.raw.toLong()),
    cashFromOperations = totalCashFromOperatingActivities.raw.toBigDecimal()
)

private fun BalanceSheetStatementDto.toBalanceSheetStatement() = BalanceSheetStatement(
    date = Instant.ofEpochSecond(endDate.raw.toLong()),
    totalAssets = totalAssets.raw.toBigDecimal(),
    totalLiabilities = totalLiab.raw.toBigDecimal()
)

private fun IncomeStatementDto.toIncomeStatement() = IncomeStatement(
    date = Instant.ofEpochSecond(endDate.raw.toLong()),
    netIncome = netIncome.raw.toBigDecimal(),
    ebit = ebit.raw.toBigDecimal()
)

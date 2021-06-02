package hu.finance.api.dto

import hu.finance.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

data class TimeSeriesDto(
    val timeseries: TimeSeriesResultDto
)

data class TimeSeriesResultDto(
    val result: List<TimeSeriesDataContainerDto>
)

data class TimeSeriesDataContainerDto(
    val meta: TimeSeriesMetadataDto,
    val annualShareIssued: List<TimeSeriesDataDto>? = null,
    val annualLongTermDebt: List<TimeSeriesDataDto>? = null,
    val annualStockholdersEquity: List<TimeSeriesDataDto>? = null,
    val annualCapitalExpenditure: List<TimeSeriesDataDto>? = null,
    val annualTotalCapitalization: List<TimeSeriesDataDto>? = null
)

data class TimeSeriesDataDto(
    val asOfDate: String,
    val reportedValue: DataDto
)

data class TimeSeriesMetadataDto(
    val symbol: List<String>,
    val type: List<String>
)

data class QuoteDto(
    val quoteSummary: QuoteSummaryDto
)

data class QuoteSummaryDto(
    val result: List<ResultDto>
)

data class ResultDto(
    val price: PriceDto,
    val summaryDetail: SummaryDetailsDto,
    val balanceSheetHistory: BalanceSheetHistoryDto,
    val incomeStatementHistory: IncomeStatementHistoryDto,
    val cashflowStatementHistory: CashFowStatementHistoryDto
)

data class CashFowStatementHistoryDto(
    val cashflowStatements: List<CashFlowStatementDto>
)

data class CashFlowStatementDto(
    val endDate: DataDto,
    val totalCashFromOperatingActivities: DataDto
)

data class IncomeStatementHistoryDto(
    val incomeStatementHistory: List<IncomeStatementDto>
)

data class IncomeStatementDto(
    val endDate: DataDto,
    val ebit: DataDto,
    val netIncome: DataDto
)

data class BalanceSheetHistoryDto(
    val balanceSheetStatements: List<BalanceSheetStatementDto>
)

data class BalanceSheetStatementDto(
    val endDate: DataDto,
    val totalAssets: DataDto,
    val totalLiab: DataDto
)

data class PriceDto(
    val longName: String,
    val currency: String,
    val exchangeName: String
)

data class SummaryDetailsDto(
    val open: DataDto,
    val previousClose: DataDto
)

data class DataDto(
    var raw: String
)

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

fun QuoteDto.toStock() = quoteSummary.result.first().run {
    Quote(
        shareSummary = ShareSummary(
            open = summaryDetail.open.raw.toBigDecimal(),
            previousClose = summaryDetail.previousClose.raw.toBigDecimal(),
            currency = Currency.getInstance(price.currency)
        ),
        quoteSummary = QuoteSummary(
            name = price.longName,
            exchange = price.exchangeName
        ),
        cashFlowStatements = cashflowStatementHistory.cashflowStatements.map { it.toCashFlowStatement() },
        balanceSheetStatements = balanceSheetHistory.balanceSheetStatements.map { it.toBalanceSheetStatement() },
        incomeStatements = incomeStatementHistory.incomeStatementHistory.map { it.toIncomeStatement() }
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

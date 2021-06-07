package hu.finance.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TimeSeries(
    val annualShareIssued: List<TimeSeriesData> = emptyList(),
    val annualLongTermDebt: List<TimeSeriesData> = emptyList(),
    val annualStockholdersEquity: List<TimeSeriesData> = emptyList(),
    val annualCapitalExpenditure: List<TimeSeriesData> = emptyList(),
    val annualTotalCapitalization: List<TimeSeriesData> = emptyList()
)

data class TimeSeriesData(
    val date: Instant,
    val value: BigDecimal
)

data class Quote(
    val shareSummary: ShareSummary,
    val quoteSummary: QuoteSummary,
    val balanceSheetStatements: List<BalanceSheetStatement> = emptyList(),
    val incomeStatements: List<IncomeStatement> = emptyList(),
    val cashFlowStatements: List<CashFlowStatement> = emptyList()
)

data class CashFlowStatement(
    val date: Instant,
    val cashFromOperations: BigDecimal
)

data class ShareSummary(
    val currency: Currency,
    val open: BigDecimal,
    val previousClose: BigDecimal
)

data class QuoteSummary(
    val shortName: String,
    val longName: String,
    val exchange: String
)

data class BalanceSheetStatement(
    val date: Instant,
    val totalAssets: BigDecimal,
    val totalLiabilities: BigDecimal
)

data class IncomeStatement(
    val date: Instant,
    val ebit: BigDecimal,
    val netIncome: BigDecimal
)

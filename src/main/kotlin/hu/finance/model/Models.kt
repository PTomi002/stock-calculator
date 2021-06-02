package hu.finance.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TimeSeries(
    val annualShareIssued: List<TimeSeriesData>,
    val annualLongTermDebt: List<TimeSeriesData>,
    val annualStockholdersEquity: List<TimeSeriesData>,
    val annualCapitalExpenditure: List<TimeSeriesData>,
    val annualTotalCapitalization: List<TimeSeriesData>
)

data class TimeSeriesData(
    val date: Instant,
    val value: BigDecimal
)

data class Quote(
    val shareSummary: ShareSummary,
    val quoteSummary: QuoteSummary,
    val balanceSheetStatements: List<BalanceSheetStatement>,
    val incomeStatements: List<IncomeStatement>,
    val cashFlowStatements: List<CashFlowStatement>
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
    val name: String,
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

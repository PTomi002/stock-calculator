package hu.finance.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TimeSeries(
    val annualTotalAssets: List<TimeSeriesData> = emptyList(),
    val annualTotalLiabilitiesNetMinorityInterest: List<TimeSeriesData> = emptyList(),
    val annualNetIncomeCommonStockholders: List<TimeSeriesData> = emptyList(),
    val annualEBIT: List<TimeSeriesData> = emptyList(),
    val annualTotalCapitalization: List<TimeSeriesData> = emptyList(),
    val annualShareIssued: List<TimeSeriesData> = emptyList(),
    val annualStockholdersEquity: List<TimeSeriesData> = emptyList(),
    val annualCapitalExpenditure: List<TimeSeriesData> = emptyList(),
    val annualLongTermDebt: List<TimeSeriesData> = emptyList(),
    val annualCashFlowFromContinuingOperatingActivities: List<TimeSeriesData> = emptyList()
)

data class TimeSeriesData(
    val date: Instant,
    val value: BigDecimal
)

data class Quote(
    val shareSummary: ShareSummary,
    val quoteSummary: QuoteSummary
)

data class ShareSummary(
    val currency: Currency,
    val open: BigDecimal,
    val previousClose: BigDecimal
)

data class QuoteSummary(
    val type: String,
    val shortName: String,
    val longName: String,
    val exchange: String
)

data class Chart(
    val quoteOpens: List<ChartData>,
    val splitEvents: List<SplitEvent>
)

data class ChartData(
    val date: Instant,
    val value: BigDecimal
)

data class SplitEvent(
    val date: Instant,
    val numerator: Int,
    val denominator: Int
)


package hu.finance.api.dto

import java.math.BigDecimal
import java.time.Instant

data class TimeSeriesDto(
    val timeseries: TimeSeriesResultDto? = null
)

data class TimeSeriesResultDto(
    val result: List<TimeSeriesDataContainerDto>? = null
)

data class TimeSeriesDataContainerDto(
    val meta: TimeSeriesMetadataDto? = null,
    val annualTotalAssets: List<TimeSeriesDataDto>? = null,
    val annualTotalLiabilitiesNetMinorityInterest: List<TimeSeriesDataDto>? = null,
    val annualNetIncomeCommonStockholders: List<TimeSeriesDataDto>? = null,
    val annualEBIT: List<TimeSeriesDataDto>? = null,
    val annualTotalCapitalization: List<TimeSeriesDataDto>? = null,
    val annualShareIssued: List<TimeSeriesDataDto>? = null,
    val annualStockholdersEquity: List<TimeSeriesDataDto>? = null,
    val annualCapitalExpenditure: List<TimeSeriesDataDto>? = null,
    val annualLongTermDebt: List<TimeSeriesDataDto>? = null,
    val annualCashFlowFromContinuingOperatingActivities: List<TimeSeriesDataDto>? = null
)

data class TimeSeriesDataDto(
    val asOfDate: String? = null,
    val reportedValue: DataDto? = null
)

data class TimeSeriesMetadataDto(
    val symbol: List<String>? = null,
    val type: List<String>? = null
)

data class QuoteDto(
    val quoteSummary: QuoteSummaryDto? = null
)

data class QuoteSummaryDto(
    val result: List<ResultDto>? = null
)

data class ResultDto(
    val price: PriceDto? = null,
    val summaryDetail: SummaryDetailsDto? = null,
    val financialData: FinancialDataDto? = null
)

data class FinancialDataDto(
    val currentPrice: DataDto? = null
)

data class PriceDto(
    val quoteType: String? = null,
    val longName: String? = null,
    val shortName: String? = null,
    val currency: String? = null,
    val exchangeName: String? = null,
    val regularMarketPrice: DataDto? = null
)

data class SummaryDetailsDto(
    val open: DataDto? = null,
    val previousClose: DataDto? = null
)

data class DataDto(
    var raw: String
)

data class ChartDto(
    val quoteOpens: List<ChartDataDto>? = null,
    val splitEvents: List<SplitEventDto>? = null
)

data class ChartDataDto(
    val date: Instant? = null,
    val value: BigDecimal? = null
)

data class SplitEventDto(
    val date: Instant? = null,
    val numerator: Int? = null,
    val denominator: Int? = null
)

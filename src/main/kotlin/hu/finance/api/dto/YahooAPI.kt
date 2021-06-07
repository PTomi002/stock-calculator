package hu.finance.api.dto

data class TimeSeriesDto(
    val timeseries: TimeSeriesResultDto? = null
)

data class TimeSeriesResultDto(
    val result: List<TimeSeriesDataContainerDto>? = null
)

data class TimeSeriesDataContainerDto(
    val meta: TimeSeriesMetadataDto? = null,
    val annualShareIssued: List<TimeSeriesDataDto>? = null,
    val annualLongTermDebt: List<TimeSeriesDataDto>? = null,
    val annualStockholdersEquity: List<TimeSeriesDataDto>? = null,
    val annualCapitalExpenditure: List<TimeSeriesDataDto>? = null,
    val annualTotalCapitalization: List<TimeSeriesDataDto>? = null,
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
    val balanceSheetHistory: BalanceSheetHistoryDto? = null,
    val incomeStatementHistory: IncomeStatementHistoryDto? = null,
    val cashflowStatementHistory: CashFowStatementHistoryDto? = null
)

data class CashFowStatementHistoryDto(
    val cashflowStatements: List<CashFlowStatementDto>? = null
)

data class CashFlowStatementDto(
    val endDate: DataDto? = null,
    val totalCashFromOperatingActivities: DataDto? = null
)

data class IncomeStatementHistoryDto(
    val incomeStatementHistory: List<IncomeStatementDto>? = null
)

data class IncomeStatementDto(
    val endDate: DataDto? = null,
    val ebit: DataDto? = null,
    val netIncome: DataDto? = null
)

data class BalanceSheetHistoryDto(
    val balanceSheetStatements: List<BalanceSheetStatementDto>? = null
)

data class BalanceSheetStatementDto(
    val endDate: DataDto? = null,
    val totalAssets: DataDto? = null,
    val totalLiab: DataDto? = null
)

data class PriceDto(
    val quoteType: String? = null,
    val longName: String? = null,
    val shortName: String? = null,
    val currency: String? = null,
    val exchangeName: String? = null
)

data class SummaryDetailsDto(
    val open: DataDto? = null,
    val previousClose: DataDto? = null
)

data class DataDto(
    var raw: String
)

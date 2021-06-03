package hu.finance.api.dto

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
    val price: PriceDto? = null,
    val summaryDetail: SummaryDetailsDto? = null,
    val balanceSheetHistory: BalanceSheetHistoryDto? = null,
    val incomeStatementHistory: IncomeStatementHistoryDto? = null,
    val cashflowStatementHistory: CashFowStatementHistoryDto? = null
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

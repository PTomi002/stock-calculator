package hu.finance.api.dto

import hu.finance.api.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class TimeSeriesDto(
    val timeseries: TimeSeriesResultDto
)

data class TimeSeriesResultDto(
    val result: List<TimeSeriesDataDto>
)

data class TimeSeriesDataDto(
    val meta: MetadataDto,
    val annualShareIssued: List<TimeSeriesDataContainerDto>? = null,
    val annualLongTermDebt: List<TimeSeriesDataContainerDto>? = null,
    val annualStockholdersEquity: List<TimeSeriesDataContainerDto>? = null
)

data class TimeSeriesDataContainerDto(
    val asOfDate: String,
    val reportedValue: DataDto
)

data class MetadataDto(
    val symbol: List<String>,
    val type: List<String>
)

data class StockDto(
    val quoteSummary: QuoteSummaryDto
)

data class QuoteSummaryDto(
    val result: List<ResultDto>
)

data class ResultDto(
    val price: PriceDto,
    val summaryDetail: SummaryDetailsDto,
    val balanceSheetHistory: BalanceSheetHistoryDto,
    val incomeStatementHistory: IncomeStatementHistoriesDto
)

data class IncomeStatementHistoriesDto(
    val incomeStatementHistory: List<IncomeStatementHistoryDto>
)

data class IncomeStatementHistoryDto(
    val endDate: DataDto,
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
        timeSeriesData = listOf(
            TimeSeriesData(
                annualShareIssued = flatMap { tsData ->
                    tsData.annualShareIssued?.map { it.toTimeSeriesDataContainer() } ?: emptyList()
                },
                annualLongTermDebt = flatMap { tsData ->
                    tsData.annualLongTermDebt?.map { it.toTimeSeriesDataContainer() } ?: emptyList()
                },
                annualStockholdersEquity = flatMap { tsData ->
                    tsData.annualStockholdersEquity?.map { it.toTimeSeriesDataContainer() } ?: emptyList()
                }
            )
        )
    )
}

fun TimeSeriesDataContainerDto.toTimeSeriesDataContainer() =
    TimeSeriesDataContainer(
        date = LocalDate.parse(asOfDate).atStartOfDay().toInstant(ZoneOffset.UTC),
        value = reportedValue.raw.toBigDecimal()
    )

fun StockDto.toStock() = quoteSummary.result.first().run {
    BalanceSheet(
        share = Share(
            open = summaryDetail.open.raw.toBigDecimal(),
            previousClose = summaryDetail.previousClose.raw.toBigDecimal(),
            currency = price.currency
        ),
        company = Company(
            name = price.longName,
            exchange = price.exchangeName
        ),
        balanceSheetHistory = BalanceSheetHistory(
            balanceSheetStatements = balanceSheetHistory.balanceSheetStatements.map {
                BalanceSheetStatement(
                    date = Instant.ofEpochSecond(it.endDate.raw.toLong()),
                    totalAssets = it.totalAssets.raw.toBigDecimal(),
                    totalLiabilities = it.totalLiab.raw.toBigDecimal()
                )
            }
        ),
        incomeStatementHistory = IncomeStatementHistory(
            incomeStatements = incomeStatementHistory.incomeStatementHistory.map {
                IncomeStatement(
                    date = Instant.ofEpochSecond(it.endDate.raw.toLong()),
                    netIncome = it.netIncome.raw.toBigDecimal()
                )
            }
        )
    )
}

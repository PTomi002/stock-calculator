package hu.finance.api.dto

import hu.finance.api.model.*

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
                    date = java.time.Instant.ofEpochSecond(it.endDate.raw.toLong()),
                    totalAssets = it.totalAssets.raw.toBigDecimal(),
                    totalLiabilities = it.totalLiab.raw.toBigDecimal()
                )
            }
        ),
        incomeStatementHistory = IncomeStatementHistory(
            incomeStatements = incomeStatementHistory.incomeStatementHistory.map {
                IncomeStatement(
                    date = java.time.Instant.ofEpochSecond(it.endDate.raw.toLong()),
                    netIncome = it.netIncome.raw.toBigDecimal()
                )
            }
        )
    )
}

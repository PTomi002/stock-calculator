package hu.finance.api.dto

import hu.finance.api.model.*
import java.time.Instant

data class BalanceSheetDto(
    var summaryDetail: SummaryDetailsDto,
    var quoteType: QuoteTypeDto,
    var balanceSheetHistory: BalanceSheetHistoryDto,
    var incomeStatementHistory: IncomeStatementHistoryDto
)

data class SummaryDetailsDto(
    var previousClose: DataDto,
    var open: DataDto,
    var currency: String
)

data class QuoteTypeDto(
    var exchange: String,
    var longName: String
)

data class BalanceSheetHistoryDto(
    var balanceSheetStatements: List<BalanceSheetStatementDto>
)

data class IncomeStatementHistoryDto(
    var incomeStatementHistory: List<IncomeStatementHistoryStatementDto>
)

data class IncomeStatementHistoryStatementDto(
    var netIncome: DataDto,
    var endDate: DataDto
)

data class BalanceSheetStatementDto(
    var totalAssets: DataDto,
    var totalLiab: DataDto,
    var endDate: DataDto
)

data class DataDto(
    var raw: String
)

fun BalanceSheetDto.toBalanceSheet() = BalanceSheet(
    share = Share(
        open = summaryDetail.open.raw.toBigDecimal(),
        previousClose = summaryDetail.previousClose.raw.toBigDecimal(),
        currency = summaryDetail.currency
    ),
    company = Company(
        name = quoteType.longName,
        exchange = quoteType.exchange
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

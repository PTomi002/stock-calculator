package hu.finance.api.dto

import hu.finance.api.model.*
import java.time.Instant

data class BalanceSheetDto(
    var balanceSheetHistory: BalanceSheetHistoryDto,
    var incomeStatementHistory: IncomeStatementHistoryDto
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

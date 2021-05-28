package hu.finance.api.model

import java.math.BigDecimal
import java.time.Instant

data class BalanceSheet(
    val balanceSheetHistory: BalanceSheetHistory,
    val incomeStatementHistory: IncomeStatementHistory
)

/**
 * Aktuális snapshot a vállalat eszközeiről, sajáttőkéjéről és forrásairól.
 */
data class BalanceSheetHistory(
    val balanceSheetStatements: List<BalanceSheetStatement>
)

data class BalanceSheetStatement(
    val date: Instant,
    val totalAssets: BigDecimal,
    val totalLiabilities: BigDecimal
)

/**
 * Mennyi pénzt termel egy vállalkozás az adott időszak alatt.
 */
data class IncomeStatementHistory(
    val incomeStatements: List<IncomeStatement>
)

data class IncomeStatement(
    val date: Instant,
    val netIncome: BigDecimal
)
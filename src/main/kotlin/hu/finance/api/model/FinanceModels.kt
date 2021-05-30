package hu.finance.api.model

import java.math.BigDecimal
import java.time.Instant

data class BalanceSheet(
    val share: Share,
    val company: Company,
    val balanceSheetHistory: BalanceSheetHistory,
    val incomeStatementHistory: IncomeStatementHistory
)

/**
 * Részvény összefoglaló.
 */
data class Share(
    val currency: String,
    val open: BigDecimal,
    val previousClose: BigDecimal
)

/**
 * Vállalati összefoglaló.
 */
data class Company(
    val name: String,
    val exchange: String
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
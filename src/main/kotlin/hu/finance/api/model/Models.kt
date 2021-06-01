package hu.finance.api.model

import java.math.BigDecimal
import java.time.Instant

data class BalanceSheet(
    val share: Share,
    val company: Company,
    val balanceSheetHistory: BalanceSheetHistory,
    val incomeStatementHistory: IncomeStatementHistory
)

data class Share(
    val currency: String,
    val open: BigDecimal,
    val previousClose: BigDecimal
)

data class Company(
    val name: String,
    val exchange: String
)

data class BalanceSheetHistory(
    val balanceSheetStatements: List<BalanceSheetStatement>
)

data class BalanceSheetStatement(
    val date: Instant,
    val totalAssets: BigDecimal,
    val totalLiabilities: BigDecimal
)

data class IncomeStatementHistory(
    val incomeStatements: List<IncomeStatement>
)

data class IncomeStatement(
    val date: Instant,
    val netIncome: BigDecimal
)
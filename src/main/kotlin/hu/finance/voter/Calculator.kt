package hu.finance.voter

import hu.finance.model.BalanceSheet
import hu.finance.model.TimeSeries
import hu.finance.util.Maths
import hu.finance.voter.DebtToEquityCalculator.DebtToEquity
import hu.finance.voter.EarningPerShareCalculator.EarningPerShare
import hu.finance.voter.ReturnOnEquityCalculator.ReturnOnEquity
import hu.finance.voter.ReturnOnTotalCapitalCalculator.ReturnOnTotalCapital
import java.math.BigDecimal
import java.time.Instant

data class CompositSheets(
    val bs: BalanceSheet,
    val ts: TimeSeries
)

interface Calculator<T, R> {
    fun calculate(value: T): R
}

class ReturnOnEquityCalculator : Calculator<BalanceSheet, List<ReturnOnEquity>> {
    data class ReturnOnEquity(
        val date: Instant,
        val roe: BigDecimal
    )

    override fun calculate(value: BalanceSheet): List<ReturnOnEquity> =
        value.balanceSheetHistory.balanceSheetStatements
            .map { bs -> bs to value.incomeStatementHistory.incomeStatements.find { it.date == bs.date }!! }
            .map {
                ReturnOnEquity(
                    date = it.first.date,
                    roe = Maths.returnOnEquity(
                        netIncome = it.second.netIncome,
                        shareholderEquity = Maths.equity(
                            totalAsset = it.first.totalAssets,
                            totalLiability = it.first.totalLiabilities
                        )
                    )
                )
            }
}

class ReturnOnTotalCapitalCalculator : Calculator<BalanceSheet, List<ReturnOnTotalCapital>> {
    data class ReturnOnTotalCapital(
        val date: Instant,
        val rotc: BigDecimal
    )

    override fun calculate(value: BalanceSheet): List<ReturnOnTotalCapital> =
        value.balanceSheetHistory.balanceSheetStatements
            .map { bs -> bs to value.incomeStatementHistory.incomeStatements.find { it.date == bs.date }!! }
            .map {
                ReturnOnTotalCapital(
                    date = it.first.date,
                    rotc = Maths.returnOnTotalCapital(
                        netIncome = it.second.netIncome,
                        totalCapital = it.first.totalAssets + it.first.totalLiabilities
                    )
                )
            }
}

class EarningPerShareCalculator : Calculator<CompositSheets, List<EarningPerShare>> {
    data class EarningPerShare(
        val date: Instant,
        val eps: BigDecimal
    )

    override fun calculate(value: CompositSheets): List<EarningPerShare> =
        value.bs.incomeStatementHistory.incomeStatements
            .map { incomeStatement ->
                incomeStatement to value.ts.timeSeriesData.find {
                    it.annualShareIssued.isNotEmpty()
                }!!.annualShareIssued.find { it.date == incomeStatement.date }!!
            }
            .map {
                EarningPerShare(
                    date = it.first.date,
                    eps = Maths.earningPerShare(
                        numOfShare = it.second.value,
                        netProfit = it.first.netIncome
                    )
                )
            }
}

class DebtToEquityCalculator : Calculator<CompositSheets, List<DebtToEquity>> {
    data class DebtToEquity(
        val date: Instant,
        val dte: BigDecimal
    )

    override fun calculate(value: CompositSheets): List<DebtToEquity> {
        val totalLiab = value.bs.balanceSheetHistory.balanceSheetStatements
        val stockHolderEquity =
            value.ts.timeSeriesData.find { it.annualStockholdersEquity.isNotEmpty() }!!.annualStockholdersEquity

        return totalLiab
            .map { tl -> tl to stockHolderEquity.find { it.date == tl.date }!! }
            .map {
                DebtToEquity(
                    date = it.first.date,
                    dte = Maths.debtToEquity(
                        totalLiab = it.first.totalLiabilities,
                        stockHolderEquity = it.second.value
                    )
                )
            }
    }
}

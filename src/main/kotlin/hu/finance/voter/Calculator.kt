package hu.finance.voter

import hu.finance.api.model.BalanceSheet
import hu.finance.util.Maths
import hu.finance.voter.ReturnOnEquityCalculator.ReturnOnEquity
import hu.finance.voter.ReturnOnTotalCapitalCalculator.ReturnOnTotalCapital
import java.math.BigDecimal
import java.time.Instant

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
                    roe = Maths.roe(
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
                    rotc = Maths.rotc(
                        netIncome = it.second.netIncome,
                        totalCapital = it.first.totalAssets + it.first.totalLiabilities
                    )
                )
            }
}


package hu.finance.calculator

import hu.finance.calculator.DebtToEquityCalculator.DebtToEquity
import hu.finance.calculator.EarningPerShareCalculator.EarningPerShare
import hu.finance.calculator.FreeCashFlowCalculator.FreeCashFlow
import hu.finance.calculator.ReturnOnEquityCalculator.ReturnOnEquity
import hu.finance.calculator.ReturnOnTotalCapitalCalculator.ReturnOnTotalCapital
import hu.finance.model.Quote
import hu.finance.model.TimeSeries
import hu.finance.util.Maths
import hu.finance.util.Maths.divWith
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class CompositeSheets(
    val bs: Quote,
    val ts: TimeSeries
)

interface Calculator<T, R> {
    fun calculate(data: T): R
}

class ReturnOnEquityCalculator : Calculator<Quote, List<ReturnOnEquity>> {
    data class ReturnOnEquity(
        val date: Instant,
        val roe: BigDecimal
    )

    override fun calculate(data: Quote): List<ReturnOnEquity> =
        data.balanceSheetStatements
            .map { bs -> bs to data.incomeStatements.find { it.date == bs.date }!! }
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

class ReturnOnTotalCapitalCalculator : Calculator<CompositeSheets, List<ReturnOnTotalCapital>> {
    data class ReturnOnTotalCapital(
        val date: Instant,
        val rotc: BigDecimal
    )

    override fun calculate(data: CompositeSheets): List<ReturnOnTotalCapital> =
        data.bs.incomeStatements
            .map { inStatement -> inStatement to data.ts.annualTotalCapitalization.find { it.date == inStatement.date } }
            .filter { it.second != null }
            .map {
                ReturnOnTotalCapital(
                    date = it.first.date,
                    rotc = Maths.returnOnTotalCapital(
                        netIncome = it.first.ebit,
                        totalCapital = it.second!!.value
                    )
                )
            }
}

class EarningPerShareCalculator : Calculator<CompositeSheets, List<EarningPerShare>> {
    data class EarningPerShare(
        val date: Instant,
        val eps: BigDecimal,
        val currency: Currency
    )

    override fun calculate(data: CompositeSheets): List<EarningPerShare> =
        data.bs.incomeStatements
            .map { incomeStatement -> incomeStatement to data.ts.annualShareIssued.find { it.date == incomeStatement.date } }
            .filter { it.second != null }
            .map {
                EarningPerShare(
                    date = it.first.date,
                    eps = Maths.earningPerShare(
                        numOfShare = it.second!!.value,
                        netProfit = it.first.netIncome
                    ),
                    currency = data.bs.shareSummary.currency
                )
            }
}

class DebtToEquityCalculator : Calculator<CompositeSheets, List<DebtToEquity>> {
    data class DebtToEquity(
        val date: Instant,
        val dte: BigDecimal
    )

    override fun calculate(data: CompositeSheets): List<DebtToEquity> {
        val totalLiabilities = data.bs.balanceSheetStatements
        val stockHolderEquity = data.ts.annualStockholdersEquity

        return totalLiabilities
            .map { tl -> tl to stockHolderEquity.find { it.date == tl.date } }
            .filter { it.second != null }
            .map {
                DebtToEquity(
                    date = it.first.date,
                    dte = Maths.debtToEquity(
                        totalLiabilities = it.first.totalLiabilities,
                        stockHolderEquity = it.second!!.value
                    )
                )
            }
    }
}

class FreeCashFlowCalculator : Calculator<CompositeSheets, List<FreeCashFlow>> {
    data class FreeCashFlow(
        val date: Instant,
        val freeCashFlow: BigDecimal,
        val longTermDebt: BigDecimal,
        val yearsToPaybackDebt: BigDecimal,
        val currency: Currency
    )

    override fun calculate(data: CompositeSheets): List<FreeCashFlow> =
        data.bs.cashFlowStatements
            .map { cashFlowStatement ->
                Triple(
                    cashFlowStatement,
                    data.ts.annualCapitalExpenditure.find { cashFlowStatement.date == it.date },
                    data.ts.annualLongTermDebt.find { cashFlowStatement.date == it.date }
                )
            }
            .filter { it.second != null && it.third != null }
            .map {
                val longTermDebt = it.third!!.value
                val freeCashFlow = Maths.freeCashFlow(
                    capitalExpenditure = it.second!!.value,
                    cashFlowFromOperations = it.first.cashFromOperations
                )
                FreeCashFlow(
                    date = it.first.date,
                    freeCashFlow = freeCashFlow,
                    yearsToPaybackDebt = longTermDebt.divWith(freeCashFlow),
                    longTermDebt = longTermDebt,
                    currency = data.bs.shareSummary.currency
                )
            }
}

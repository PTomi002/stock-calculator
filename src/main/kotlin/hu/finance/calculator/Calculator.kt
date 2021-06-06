package hu.finance.calculator

import hu.finance.calculator.EarningPerShareCalculator.EarningPerShare
import hu.finance.calculator.ReturnOnEquityCalculator.ReturnOnEquity
import hu.finance.calculator.ReturnOnTotalCapitalCalculator.ReturnOnTotalCapital
import hu.finance.model.Quote
import hu.finance.service.CompositeQuote
import hu.finance.util.Maths
import java.math.BigDecimal
import java.time.Instant

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
            .map { quote -> quote to data.incomeStatements.find { it.date == quote.date } }
            .filter { it.second != null }
            .map {
                ReturnOnEquity(
                    date = it.first.date,
                    roe = Maths.returnOnEquity(
                        netIncome = it.second!!.netIncome,
                        shareholderEquity = Maths.equity(
                            totalAsset = it.first.totalAssets,
                            totalLiability = it.first.totalLiabilities
                        )
                    )
                )
            }
}

class ReturnOnTotalCapitalCalculator : Calculator<CompositeQuote, List<ReturnOnTotalCapital>> {
    data class ReturnOnTotalCapital(
        val date: Instant,
        val rotc: BigDecimal
    )

    override fun calculate(data: CompositeQuote): List<ReturnOnTotalCapital> =
        data.quote.incomeStatements
            .map { inStatement -> inStatement to data.timeSeries.annualTotalCapitalization.find { it.date == inStatement.date } }
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

class EarningPerShareCalculator : Calculator<CompositeQuote, List<EarningPerShare>> {
    data class EarningPerShare(
        val date: Instant,
        val eps: BigDecimal
    )

    override fun calculate(data: CompositeQuote): List<EarningPerShare> =
        data.quote.incomeStatements
            .map { incomeStatement -> incomeStatement to data.timeSeries.annualShareIssued.find { it.date == incomeStatement.date } }
            .filter { it.second != null }
            .map {
                EarningPerShare(
                    date = it.first.date,
                    eps = Maths.earningPerShare(
                        numOfShare = it.second!!.value,
                        netProfit = it.first.netIncome
                    )
                )
            }
}
//
//class DebtToEquityCalculator : Calculator<CompositeQuote, List<DebtToEquity>> {
//    data class DebtToEquity(
//        val date: Instant,
//        val dte: BigDecimal
//    )
//
//    override fun calculate(data: CompositeQuote): List<DebtToEquity> {
//        val totalLiabilities = data.quote.balanceSheetStatements
//        val stockHolderEquity = data.timeSeries.annualStockholdersEquity
//
//        return totalLiabilities
//            .map { tl -> tl to stockHolderEquity.find { it.date == tl.date } }
//            .filter { it.second != null }
//            .map {
//                DebtToEquity(
//                    date = it.first.date,
//                    dte = Maths.debtToEquity(
//                        totalLiabilities = it.first.totalLiabilities,
//                        stockHolderEquity = it.second!!.value
//                    )
//                )
//            }
//    }
//}
//
//class FreeCashFlowCalculator : Calculator<CompositeQuote, List<FreeCashFlow>> {
//    data class FreeCashFlow(
//        val date: Instant,
//        val freeCashFlow: BigDecimal,
//        val longTermDebt: BigDecimal,
//        val yearsToPaybackDebt: BigDecimal,
//        val currency: Currency
//    )
//
//    override fun calculate(data: CompositeQuote): List<FreeCashFlow> =
//        data.quote.cashFlowStatements
//            .map { cashFlowStatement ->
//                Triple(
//                    cashFlowStatement,
//                    data.timeSeries.annualCapitalExpenditure.find { cashFlowStatement.date == it.date },
//                    data.timeSeries.annualLongTermDebt.find { cashFlowStatement.date == it.date }
//                )
//            }
//            .filter { it.second != null && it.third != null }
//            .map {
//                val longTermDebt = it.third!!.value
//                val freeCashFlow = Maths.freeCashFlow(
//                    capitalExpenditure = it.second!!.value,
//                    cashFlowFromOperations = it.first.cashFromOperations
//                )
//                FreeCashFlow(
//                    date = it.first.date,
//                    freeCashFlow = freeCashFlow,
//                    yearsToPaybackDebt = longTermDebt.divWith(freeCashFlow),
//                    longTermDebt = longTermDebt,
//                    currency = data.quote.shareSummary.currency
//                )
//            }
//}

package hu.finance.calculator

import hu.finance.calculator.DebtToEquityCalculator.DebtToEquity
import hu.finance.calculator.EarningPerShareCalculator.EarningPerShare
import hu.finance.calculator.FreeCashFlowCalculator.FreeCashFlow
import hu.finance.calculator.ReturnOnEquityCalculator.ReturnOnEquity
import hu.finance.calculator.ReturnOnTotalCapitalCalculator.ReturnOnTotalCapital
import hu.finance.model.TimeSeries
import hu.finance.model.TimeSeriesData
import hu.finance.util.FinanceCalculations
import hu.finance.util.FinanceCalculations.divWith
import java.math.BigDecimal
import java.time.Instant

interface Calculator<T, R> {
    fun calculate(data: T): R
}

class ReturnOnEquityCalculator : Calculator<TimeSeries, List<ReturnOnEquity>> {
    data class ReturnOnEquity(
        val date: Instant,
        val roe: BigDecimal
    )

    override fun calculate(data: TimeSeries): List<ReturnOnEquity> =
        data.annualNetIncomeCommonStockholders
            .map { netIncome ->
                Triple(
                    netIncome,
                    data.annualTotalAssets.matchDate(netIncome),
                    data.annualTotalLiabilitiesNetMinorityInterest.matchDate(netIncome)
                )
            }
            .filter { it.isNotEmpty() }
            .map {
                ReturnOnEquity(
                    date = it.first.date,
                    roe = FinanceCalculations.roe(
                        netIncome = it.first.value,
                        shareholderEquity = FinanceCalculations.shareHolderEquity(
                            totalAsset = it.second!!.value,
                            totalLiabilities = it.third!!.value
                        )
                    )
                )
            }
}

class ReturnOnTotalCapitalCalculator : Calculator<TimeSeries, List<ReturnOnTotalCapital>> {
    data class ReturnOnTotalCapital(
        val date: Instant,
        val rotc: BigDecimal
    )

    override fun calculate(data: TimeSeries): List<ReturnOnTotalCapital> =
        data.annualEBIT
            .map { it to data.annualTotalCapitalization.matchDate(it) }
            .filter { it.isNotEmpty() }
            .map {
                ReturnOnTotalCapital(
                    date = it.first.date,
                    rotc = FinanceCalculations.rotc(
                        ebit = it.first.value,
                        totalCapital = it.second!!.value
                    )
                )
            }
}

class EarningPerShareCalculator : Calculator<TimeSeries, List<EarningPerShare>> {
    data class EarningPerShare(
        val date: Instant,
        val eps: BigDecimal
    )

    override fun calculate(data: TimeSeries): List<EarningPerShare> =
        data.annualNetIncomeCommonStockholders
            .map { it to data.annualShareIssued.matchDate(it) }
            .filter { it.isNotEmpty() }
            .map {
                EarningPerShare(
                    date = it.first.date,
                    eps = FinanceCalculations.eps(
                        numOfShare = it.second!!.value,
                        netIncome = it.first.value
                    )
                )
            }
}

class DebtToEquityCalculator : Calculator<TimeSeries, List<DebtToEquity>> {
    data class DebtToEquity(
        val date: Instant,
        val dte: BigDecimal
    )

    override fun calculate(data: TimeSeries): List<DebtToEquity> =
        data.annualTotalLiabilitiesNetMinorityInterest
            .map { it to data.annualStockholdersEquity.matchDate(it) }
            .filter { it.isNotEmpty() }
            .map {
                DebtToEquity(
                    date = it.first.date,
                    dte = FinanceCalculations.dte(
                        totalLiabilities = it.first.value,
                        stockHolderEquity = it.second!!.value
                    )
                )
            }
}

class FreeCashFlowCalculator : Calculator<TimeSeries, List<FreeCashFlow>> {
    data class FreeCashFlow(
        val date: Instant,
        val freeCashFlow: BigDecimal,
        val longTermDebt: BigDecimal,
        val yearsToPaybackDebt: BigDecimal
    )

    override fun calculate(data: TimeSeries): List<FreeCashFlow> =
        data.annualCashFlowFromContinuingOperatingActivities
            .map {
                Triple(
                    it,
                    data.annualCapitalExpenditure.matchDate(it),
                    data.annualLongTermDebt.matchDate(it)
                )
            }
            .filter { it.isNotEmpty() }
            .map {
                val freeCashFlow = FinanceCalculations.fcf(
                    cashFlowFromOperations = it.first.value,
                    capitalExpenditure = it.second!!.value
                )
                FreeCashFlow(
                    date = it.first.date,
                    freeCashFlow = freeCashFlow,
                    yearsToPaybackDebt = it.third!!.value.divWith(freeCashFlow),
                    longTermDebt = it.third!!.value
                )
            }
}

private fun <A, B> Pair<A, B>.isNotEmpty() = !isEmpty()
private fun <A, B> Pair<A, B>.isEmpty() = first == null || second == null
private fun <A, B, C> Triple<A, B, C>.isNotEmpty() = !isEmpty()
private fun <A, B, C> Triple<A, B, C>.isEmpty() = first == null || second == null || third == null
private fun List<TimeSeriesData>.matchDate(data: TimeSeriesData) =
    find { it.date == data.date }

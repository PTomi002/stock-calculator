package hu.finance.util

import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

object FinanceCalculations {
    private const val SCALE = 3
    private val HUNDRED = 100.0.toBigDecimal()

    private fun BigDecimal.toPercent() = multiply(HUNDRED)

    fun eps(numOfShare: BigDecimal, netProfit: BigDecimal): BigDecimal = netProfit.divWith(numOfShare)

    fun equity(totalAsset: BigDecimal, totalLiability: BigDecimal): BigDecimal = totalAsset.subtract(totalLiability)

    fun roe(netIncome: BigDecimal, shareholderEquity: BigDecimal): BigDecimal =
        netIncome.divWith(shareholderEquity).toPercent()

    fun rotc(netIncome: BigDecimal, totalCapital: BigDecimal): BigDecimal =
        netIncome.divWith(totalCapital).toPercent()

    fun dte(totalLiabilities: BigDecimal, stockHolderEquity: BigDecimal): BigDecimal =
        totalLiabilities.divWith(stockHolderEquity)

    fun fcf(cashFlowFromOperations: BigDecimal, capitalExpenditure: BigDecimal): BigDecimal =
        cashFlowFromOperations.subtract(capitalExpenditure.abs())

    fun BigDecimal.divWith(other: BigDecimal): BigDecimal = divide(other, SCALE, HALF_EVEN)
}

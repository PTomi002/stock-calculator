package hu.finance.util

import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

object FinanceCalculations {
    private const val SCALE = 3
    val HUNDRED = 100.0.toBigDecimal()

    private fun BigDecimal.toPercent() = multiply(HUNDRED)

    fun eps(numOfShare: BigDecimal, netIncome: BigDecimal): BigDecimal = netIncome.divWith(numOfShare)

    fun shareHolderEquity(totalAsset: BigDecimal, totalLiabilities: BigDecimal): BigDecimal =
        totalAsset.subtract(totalLiabilities)

    fun roe(netIncome: BigDecimal, shareholderEquity: BigDecimal): BigDecimal =
        netIncome.divWith(shareholderEquity).toPercent()

    fun roa(netIncome: BigDecimal, totalAssets: BigDecimal): BigDecimal =
        netIncome.divWith(totalAssets).toPercent()

    fun rotc(ebit: BigDecimal, totalCapital: BigDecimal): BigDecimal =
        ebit.divWith(totalCapital).toPercent()

    fun dte(totalLiabilities: BigDecimal, stockHolderEquity: BigDecimal): BigDecimal =
        totalLiabilities.divWith(stockHolderEquity)

    fun fcf(cashFlowFromOperations: BigDecimal, capitalExpenditure: BigDecimal): BigDecimal =
        cashFlowFromOperations.subtract(capitalExpenditure.abs())

    fun bv(stockHolderEquity: BigDecimal, numOfShare: BigDecimal): BigDecimal =
        stockHolderEquity.divWith(numOfShare)

    fun BigDecimal.divWith(other: BigDecimal): BigDecimal = divide(other, SCALE, HALF_EVEN)
}

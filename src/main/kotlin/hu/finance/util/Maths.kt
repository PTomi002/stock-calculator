package hu.finance.util

import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

object Maths {
    private val HUNDRED = 100.0.toBigDecimal()
    private val SEVENTY_TWO = 72.0.toBigDecimal()
    private fun BigDecimal.asPercent() = this.div(HUNDRED)
    private fun BigDecimal.toPercent() = this.multiply(HUNDRED)

    fun interest(capital: BigDecimal, interest: BigDecimal) = capital.multiply(interest.asPercent())

    fun compoundInterest(capital: BigDecimal, interest: BigDecimal, years: Int) =
        capital.multiply((HUNDRED.add(interest)).asPercent().pow(years))

    fun seventyTwoRule(yield: BigDecimal) = SEVENTY_TWO.div(`yield`)

    fun earningPerShare(numOfShare: BigDecimal, netProfit: BigDecimal): BigDecimal = netProfit.div(numOfShare)

    fun peRation(eps: BigDecimal, sharePrice: BigDecimal) = sharePrice.div(eps)

    fun equity(totalAsset: BigDecimal, totalLiability: BigDecimal): BigDecimal = totalAsset.subtract(totalLiability)

    fun returnOnEquity(netIncome: BigDecimal, shareholderEquity: BigDecimal): BigDecimal =
        netIncome.div(shareholderEquity).toPercent()

    fun returnOnTotalCapital(netIncome: BigDecimal, totalCapital: BigDecimal): BigDecimal =
        netIncome.div(totalCapital).toPercent()

    fun debtToEquity(totalLiab: BigDecimal, stockHolderEquity: BigDecimal): BigDecimal =
        totalLiab.div(stockHolderEquity)

    fun freeCashFlow(cashFlowFromOperations: BigDecimal, capitalExpenditure: BigDecimal) =
        cashFlowFromOperations.subtract(capitalExpenditure.abs())

    private fun BigDecimal.div(other: BigDecimal) = divide(other, 6, HALF_EVEN)
}

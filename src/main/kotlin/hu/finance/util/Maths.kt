package hu.finance.util

import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

object Maths {
    private val HUNDRED = 100.0.toBigDecimal()

    //    private val SEVENTY_TWO = 72.0.toBigDecimal()
//    private fun BigDecimal.asPercent() = this.divWith(HUNDRED)
    private fun BigDecimal.toPercent() = this.multiply(HUNDRED)

//    fun interest(capital: BigDecimal, interest: BigDecimal) = capital.multiply(interest.asPercent())

//    fun compoundInterest(capital: BigDecimal, interest: BigDecimal, years: Int) =
//        capital.multiply((HUNDRED.add(interest)).asPercent().pow(years))

//    fun seventyTwoRule(yield: BigDecimal) = SEVENTY_TWO.divWith(`yield`)

    fun earningPerShare(numOfShare: BigDecimal, netProfit: BigDecimal): BigDecimal = netProfit.divWith(numOfShare)

//    fun peRation(eps: BigDecimal, sharePrice: BigDecimal) = sharePrice.divWith(eps)

    fun equity(totalAsset: BigDecimal, totalLiability: BigDecimal): BigDecimal = totalAsset.subtract(totalLiability)

    fun returnOnEquity(netIncome: BigDecimal, shareholderEquity: BigDecimal): BigDecimal =
        netIncome.divWith(shareholderEquity).toPercent()

    fun returnOnTotalCapital(netIncome: BigDecimal, totalCapital: BigDecimal): BigDecimal =
        netIncome.divWith(totalCapital).toPercent()

    fun debtToEquity(totalLiabilities: BigDecimal, stockHolderEquity: BigDecimal): BigDecimal =
        totalLiabilities.divWith(stockHolderEquity)

    fun freeCashFlow(cashFlowFromOperations: BigDecimal, capitalExpenditure: BigDecimal): BigDecimal =
        cashFlowFromOperations.subtract(capitalExpenditure.abs())

    fun BigDecimal.divWith(other: BigDecimal): BigDecimal = divide(other, 6, HALF_EVEN)
}

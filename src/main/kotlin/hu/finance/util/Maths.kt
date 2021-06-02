package hu.finance.util

import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

object Maths {
    private val HUNDRED = 100.0.toBigDecimal()
    private val SEVENTY_TWO = 72.0.toBigDecimal()
    private fun BigDecimal.asPercent() = this / HUNDRED
    private fun BigDecimal.toPercent() = this * HUNDRED

    fun interest(capital: BigDecimal, interest: Double) = capital * interest.toBigDecimal().asPercent()

    fun compoundInterest(capital: BigDecimal, interest: Double, years: Int) =
        capital * (HUNDRED + interest.toBigDecimal()).asPercent().pow(years)

    fun seventyTwoRule(yield: Double) = SEVENTY_TWO.divide(`yield`.toBigDecimal(), 6, HALF_EVEN)

    fun eps(numOfShare: BigDecimal, netProfit: BigDecimal) = netProfit.divide(numOfShare, 6, HALF_EVEN)

    fun peRation(eps: Double, sharePrice: Double) = sharePrice / eps

    fun equity(totalAsset: BigDecimal, totalLiability: BigDecimal) = totalAsset - totalLiability

    fun roe(netIncome: BigDecimal, shareholderEquity: BigDecimal) =
        (netIncome.divide(shareholderEquity, 6, HALF_EVEN)).toPercent()

    fun rotc(netIncome: BigDecimal, totalCapital: BigDecimal) =
        (netIncome.divide(totalCapital, 6, HALF_EVEN)).toPercent()

    fun debtToEquity(totalLiab: BigDecimal, stockHolderEquity: BigDecimal) =
        totalLiab.divide(stockHolderEquity, 6, HALF_EVEN)
}

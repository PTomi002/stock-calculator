package hu.finance.util

import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

object Maths {
    private val HUNDRED = 100.0.toBigDecimal()
    private val SEVENTY_TWO = 72.0.toBigDecimal()
    private fun BigDecimal.asPercent() = this / HUNDRED
    private fun BigDecimal.toPercent() = this * HUNDRED

    /**
     * Kamat számítás.
     * capital      [Currency]      =       kezdőtőke
     * interest     [0..100 %]      =       kamat
     * return       [Currency]
     */
    fun interest(capital: BigDecimal, interest: Double) = capital * interest.toBigDecimal().asPercent()

    /**
     * Kamatos kamat számítás.
     * capital      [Currency]      =       kezdőtőke
     * interest     [0..100 %]      =       kamat
     * year         [Year]          =       évek száma
     * return       [Currency]
     */
    fun compoundInterest(capital: BigDecimal, interest: Double, years: Int) =
        capital * (HUNDRED + interest.toBigDecimal()).asPercent().pow(years)

    /**
     * 72-es ökölszabály: X év múlva duplázódik meg a vagyon.
     * yield        [0..100 %]      =       hozam (befektetett tőke növekménye)
     * return       [Year]
     */
    fun seventyTwoRule(yield: Double) = SEVENTY_TWO.divide(`yield`.toBigDecimal(), 6, HALF_EVEN)

    /**
     * EPS (Earning Per Share): 1 részvényre eső éves nyereség.
     * numOfShare   [Piece]        =       részvények száma egy adott évben
     * netProfit    [Currency]     =       nettó éves profit (profit = nyereség)
     * return       [piece/Currency]
     */
    fun eps(numOfShare: Int, netProfit: BigDecimal) = netProfit / numOfShare.toBigDecimal()

    /**
     * P/E (Price / Earning): ha olcsón veszek részvényt hamarabb térül meg, kb. 10 körül jó, de max 15 legyen.
     * eps          [Piece/Currency]    =       1 részvényre eső éves nyereség
     * sharePrice   [Currency]          =       részvény aktuális ára
     * return       [Ratio]
     */
    fun peRation(eps: Double, sharePrice: Double) = sharePrice / eps

    /**
     * Equity (Shareholders Equity): sajáttőke, összes eszközö (ház, autó, számítógép, stb) - összes adósság (hitel, stb...).
     * totalAsset       [Currency]          =       összes eszköz
     * totalLiability   [Currency]          =       összes forrás
     * return           [Currency]
     */
    fun equity(totalAsset: BigDecimal, totalLiability: BigDecimal) = totalAsset - totalLiability

    /**
     * Return On Equity: sajáttőke megtérülés (hozam), akkor jó, ha hosszú távon magas és növekvő tendenciát mutat (amerikai átlag 12%).
     * netIncome            [Currency]          =       nettó profit
     * shareholderEquity    [Currency]          =       sajáttőke
     * return               [0..100 %]
     */
    fun roe(netIncome: BigDecimal, shareholderEquity: BigDecimal) =
        (netIncome.divide(shareholderEquity, 6, HALF_EVEN)).toPercent()
}

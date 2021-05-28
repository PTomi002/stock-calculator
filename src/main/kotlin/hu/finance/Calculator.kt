package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.YahooFinanceAPI
import hu.finance.voter.ReturnOnEquityVoter

@Suppress("UnstableApiUsage")
private val financeClient = YahooFinanceAPI(RateLimiter.create(4.0))

fun main(args: Array<String>) {
    val balanceSheet = financeClient.balanceSheet("TEVA", "NYSE")

    println("Check 1): ${ReturnOnEquityVoter().vote(balanceSheet)}")
}

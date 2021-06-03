@file:Suppress("UnstableApiUsage")

package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.YahooApi
import hu.finance.gui.CalcGui
import hu.finance.service.FinanceService
import java.util.concurrent.Executors

private val financeGateway = YahooApi(RateLimiter.create(4.0))
private val pool = Executors.newFixedThreadPool(5)
private val finances = FinanceService(
    financeGateway = financeGateway,
    pool = pool
)

fun main(args: Array<String>) {
    CalcGui(
        title = "Finance Calculator",
        finances = finances
    )
}

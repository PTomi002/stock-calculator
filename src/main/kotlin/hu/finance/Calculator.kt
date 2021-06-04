@file:Suppress("UnstableApiUsage")

package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.YahooApi
import hu.finance.gui.CalculatorGUI
import hu.finance.gui.util.AutoCloseableLock
import hu.finance.service.FinanceService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

private val financeGateway = YahooApi(
    limiter = RateLimiter.create(4.0)
)
private val pool = Executors.newFixedThreadPool(5)
private val finances = FinanceService(
    financeGateway = financeGateway,
    pool = pool
)
private val mainLock = ReentrantLock()

fun main() {
    CalculatorGUI("Stock Calculator", AutoCloseableLock(mainLock), finances)
        .apply {
            isVisible = true
        }
}

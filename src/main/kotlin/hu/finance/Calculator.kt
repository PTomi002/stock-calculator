@file:Suppress("UnstableApiUsage")

package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.YahooApi
import hu.finance.gui.CalculatorGUI
import hu.finance.gui.ChartsGUI
import hu.finance.service.FinanceService
import hu.finance.service.GuiUpdaterService
import hu.finance.service.StockWatchDog
import hu.finance.service.StockWatchDogConfig
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    val guiUpdater = GuiUpdaterService()
    val finances = FinanceService(
        financeGateway = YahooApi(RateLimiter.create(3.0)),
        pool = Executors.newFixedThreadPool(9)
    )
    val cGui = CalculatorGUI("Calculator")
    val chGui = ChartsGUI("Charts")

    guiUpdater.calcGui = cGui
    guiUpdater.chartsGui = chGui
    guiUpdater.finances = finances

    guiUpdater.attachCalculateInflation(cGui.calculateInflation)
    guiUpdater.attachLoadQuote(cGui.loadQuote)

    if (args.contains("start-watcher")) {
        StockWatchDog(
            finances = finances,
            config = StockWatchDogConfig()
        ).start()
    }
}

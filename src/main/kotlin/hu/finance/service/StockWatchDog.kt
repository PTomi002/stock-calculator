package hu.finance.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import hu.finance.gui.NotificationGUI
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.time.Duration
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime.now
import kotlin.concurrent.timer

class StockWatchDog(
    private val finances: Finances
) {
    private val companies by lazy {
        jacksonObjectMapper().readValue<List<String>>(javaClass.getResource("/daemon/companies.json")!!)
    }

    fun start() = timer(
        name = "stock-watchdog",
        daemon = true,
        initialDelay = Duration.ofSeconds(0).toMillis(),
        period = Duration.ofHours(1).toMillis()
    ) { companies.forEach { watch(it) } }

    private fun watch(company: String) {
        try {
            val quote = finances.loadQuote(
                ticker = company,
                chStart = now(UTC).minusYears(1).toInstant()
            )
            val nowPrice = quote.quote.shareSummary.price.toDouble()
            val avgOpenPrice = quote.chart.quoteOpens.map { it.value.toDouble() }.average()
            val ttmPE = quote.quote.shareSummary.ttmPE

            println("Stock company: $company, avg price: $avgOpenPrice, now price: $nowPrice")
            if (nowPrice < avgOpenPrice && ttmPE < 25.0) {
                buildNotificationPopUp(company, avgOpenPrice, nowPrice)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun buildNotificationPopUp(company: String, avgOpenPrice: Double, nowPrice: Double) {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val image = Toolkit.getDefaultToolkit().createImage("icon.png")
            val trayIcon = TrayIcon(image, "Calculator Notification").apply {
                isImageAutoSize = true
                toolTip = "Calculator Notification"
            }

            tray.add(trayIcon)
            trayIcon.displayMessage(
                "Stock Price ALert",
                String.format(NotificationGUI.MESSAGE, company, avgOpenPrice, nowPrice),
                TrayIcon.MessageType.INFO
            )
        } else NotificationGUI("Notification", company, avgOpenPrice, nowPrice)
    }
}
package hu.finance.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import hu.finance.gui.NotificationGUI
import java.time.Duration
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime.now
import kotlin.concurrent.timer

class StockWatchDog(
    private val finances: Finances,
    private val threshold: Double = 0.7
) {

    init {
        val range = 0.0..1.0
        check(threshold in range) { "Threshold must be within range: $range" }
    }

    private val companies by lazy {
        jacksonObjectMapper().readValue<List<String>>(javaClass.getResource("/daemon/companies.json")!!)
    }

    fun start() = timer(
        name = "stock-watchdog",
        daemon = true,
        initialDelay = Duration.ofSeconds(10).toMillis(),
        period = Duration.ofHours(1).toMillis()
    ) { companies.forEach { watch(it) } }

    private fun watch(company: String) {
        try {
            val quote = finances.loadQuote(
                ticker = company,
                chStart = now(UTC).minusYears(7).toInstant()
            )
            val nowPrice = quote.quote.shareSummary.price.toDouble()
            val avgOpenPrice = quote.chart.quoteOpens.map { it.value.toDouble() }.average()

            println("Stock company: $company, avg price: $avgOpenPrice, now price: $nowPrice")
            if (nowPrice < (avgOpenPrice * threshold)) {
                buildNotificationPopUp(company, avgOpenPrice, nowPrice)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun buildNotificationPopUp(company: String, avgOpenPrice: Double, nowPrice: Double) {
        NotificationGUI("Notification", company, avgOpenPrice, nowPrice)
    }
}
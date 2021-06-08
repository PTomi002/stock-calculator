package hu.finance.service

import hu.finance.calculator.*
import hu.finance.gui.CalculatorGUI
import hu.finance.gui.ChartsGUI
import hu.finance.gui.util.AutoCloseableLock
import hu.finance.gui.util.CalcWorker
import hu.finance.model.Chart
import hu.finance.model.Quote
import hu.finance.model.TimeSeries
import java.text.DecimalFormat
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime.ofInstant
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.table.DefaultTableModel
import kotlin.properties.Delegates.notNull

/**
 * GUI designer mvn plugin uses old asm lib, and the plugin itself is too old so it generates faulty bytecode.
 * </br>
 * So refactored every GUI update logic to this class.
 */
class GuiUpdaterService {
    var calcGui: CalculatorGUI by notNull()
    var chartsGui: ChartsGUI by notNull()
    var finances: Finances by notNull()
    private val lock = AutoCloseableLock(ReentrantLock())
    private val formatter = DecimalFormat("#,###.00");

    @Volatile
    @GuardedBy(value = "lock")
    private var cqCache: CompositeQuote? = null

    private val roeCalculator = ReturnOnEquityCalculator()
    private val rotcCalculator = ReturnOnTotalCapitalCalculator()
    private val epsCalculator = EarningPerShareCalculator()
    private val dteCalculator = DebtToEquityCalculator()
    private val flowCalculator = FreeCashFlowCalculator()

    fun attachLoadQuote(jMenuItem: JMenuItem) = jMenuItem.addActionListener { loadQuote() }

    private fun loadQuote() {
        JOptionPane.showInputDialog("Company ticker?")
            .takeIf { !it.isNullOrEmpty() }
            ?.run {
                CalcWorker<CompositeQuote, Void>(
                    loader = { finances.loadQuote(this) },
                    callback = { workerResult ->
                        if (!workerResult.result) {
                            JOptionPane.showMessageDialog(
                                null,
                                "Hiba történt!"
                            )
                            workerResult.error!!.printStackTrace()
                        } else {
                            lock.withLock { cqCache = workerResult.data!! }
                            cqCache!!.run {
                                updateQuotePanel(quote)
                                updateRoeTable(timeSeries)
                                updateRotcTable(timeSeries)
                                updateEpsTable(this)
                                updateDetTable(timeSeries)
                                updateFcfTable(timeSeries)
                                updateChart(chart)
                            }
                        }
                    }).execute()
            }
    }

    private fun updateChart(chart: Chart) {
        chartsGui.apply {

        }
    }

    private fun updateQuotePanel(quote: Quote) = calcGui.run {
        quoteLabel.text = quote.quoteSummary.longName
        quoteShortLabel.text = quote.quoteSummary.shortName
        quoteTypeLabel.text = quote.quoteSummary.type
        exchangeLabel.text = quote.quoteSummary.exchange
        openPriceLabel.text = "${quote.shareSummary.open}"
        previousOpenPriceLabel.text = "${quote.shareSummary.previousClose}"
        currencyLabel.text = "${quote.shareSummary.currency}"
    }

    private fun updateRoeTable(timeSeries: TimeSeries) =
        roeCalculator.calculate(timeSeries)
            .sortedByDescending { it.date }
            .map { listOf(ofInstant(it.date, UTC).year, "${it.roe} %").toTypedArray() }
            .run { calcGui.roeTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "ROE")) }

    private fun updateRotcTable(timeSeries: TimeSeries) =
        rotcCalculator.calculate(timeSeries)
            .sortedByDescending { it.date }
            .map { listOf(ofInstant(it.date, UTC).year, "${it.rotc} %").toTypedArray() }
            .run { calcGui.rotcTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "ROTC")) }

    private fun updateEpsTable(composite: CompositeQuote) =
        epsCalculator.calculate(composite.timeSeries)
            .sortedByDescending { it.date }
            .map {
                listOf(
                    ofInstant(it.date, UTC).year,
                    "${it.eps} ${composite.quote.shareSummary.currency}"
                ).toTypedArray()
            }
            .run { calcGui.epsTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "EPS")) }

    private fun updateDetTable(timeSeries: TimeSeries) =
        dteCalculator.calculate(timeSeries)
            .sortedByDescending { it.date }
            .map { listOf(ofInstant(it.date, UTC).year, "${it.dte} ratio").toTypedArray() }
            .run { calcGui.dteTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "DTE")) }

    private fun updateFcfTable(timeSeries: TimeSeries) =
        flowCalculator.calculate(timeSeries)
            .sortedByDescending { it.date }
            .map { fcf ->
                listOf(
                    ofInstant(fcf.date, UTC).year, formatter.format(fcf.freeCashFlow),
                    formatter.format(fcf.longTermDebt), fcf.yearsToPaybackDebt
                ).toTypedArray()
            }
            .run {
                calcGui.fcfTable.model = DefaultTableModel(
                    toTypedArray(),
                    arrayOf("Év", "FCF", "LTD", "Years to pay back")
                )
            }
}
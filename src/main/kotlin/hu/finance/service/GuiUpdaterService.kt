package hu.finance.service

import hu.finance.calculator.*
import hu.finance.gui.CalculationsGUI
import hu.finance.gui.CalculatorGUI
import hu.finance.gui.ChartsGUI
import hu.finance.gui.util.AutoCloseableLock
import hu.finance.gui.util.CalcWorker
import hu.finance.model.Chart
import hu.finance.model.Quote
import hu.finance.model.TimeSeries
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.axis.CategoryLabelPositions.DOWN_90
import org.jfree.chart.plot.PlotOrientation.VERTICAL
import org.jfree.data.category.DefaultCategoryDataset
import java.awt.BorderLayout
import java.text.DecimalFormat
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime.ofInstant
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import javax.swing.JButton
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.table.DefaultTableModel
import kotlin.math.pow
import kotlin.properties.Delegates.notNull

/**
 * GUI designer mvn plugin uses old asm lib, and the plugin itself is too old so it generates faulty bytecode.
 * </br>
 * So refactored every GUI update logic to this class.
 */
class GuiUpdaterService {
    var calcGui: CalculatorGUI by notNull()
    var chartsGui: ChartsGUI by notNull()
    var calculationsGUI: CalculationsGUI by notNull()
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
    private val roaCalculator = ReturnOnAssetsCalculator()
    private val bvCalculator = BookValueCalculator()

    fun attachLoadQuote(jMenuItem: JMenuItem) = jMenuItem.addActionListener { loadQuote() }

    fun attachCalculateInflation(jButton: JButton) = jButton.addActionListener { calculateInflation() }

    private fun calculateInflation() = calcGui.apply {
        try {
            val yearsNumber = endYear.text.trim().toInt() - startYear.text.trim().toInt()
            val pGrowth = endPrice.text.trim().toDouble() / startPrice.text.trim().toDouble()

            years.text = "$yearsNumber"
            priceGrowth.text = formatter.format(pGrowth)
            avgPriceGrowth.text = "${formatter.format((pGrowth.pow((1.0 / yearsNumber)) -1) * 100)} % növekedés"
        } catch (ex: NumberFormatException) {
            JOptionPane.showMessageDialog(
                null,
                "Hiba történt: valós számokat adj meg: ${ex.message}"
            )
        }
    }

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
                                "Hiba történt: [${workerResult.error!!.message!!}]!"
                            )
                        } else {
                            lock.withLock { cqCache = workerResult.data!! }
                            cqCache!!.run {
                                updateQuotePanel(quote)
                                updateRoeTable(timeSeries)
                                updateRotcTable(timeSeries)
                                updateRoaTable(timeSeries)
                                updateEpsTable(this)
                                updateBookValueTable(this)
                                updateDetTable(timeSeries)
                                updateFcfTable(timeSeries)
                                updateChart(this)
                                updateSharesTable(timeSeries)
                            }
                        }
                    }).execute()
            }
    }

    private fun updateSharesTable(timeSeries: TimeSeries) {
        timeSeries.annualShareIssued
            .sortedByDescending { it.date }
            .map { listOf(ofInstant(it.date, UTC).year, "${formatter.format(it.value)} piece").toTypedArray() }
            .run { calcGui.sharesTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "Share Issued")) }
    }

    private fun updateBookValueTable(composite: CompositeQuote) {
        bvCalculator.calculate(composite.timeSeries)
            .sortedByDescending { it.date }
            .map {
                listOf(
                    ofInstant(it.date, UTC).year,
                    "${formatter.format(it.bv)} ${composite.quote.shareSummary.currency}"
                ).toTypedArray()
            }
            .run { calcGui.bookValueTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "Book Value")) }
    }

    private fun updateRoaTable(timeSeries: TimeSeries) {
        roaCalculator.calculate(timeSeries)
            .sortedByDescending { it.date }
            .map { listOf(ofInstant(it.date, UTC).year, "${it.roa} %").toTypedArray() }
            .run { calcGui.roaTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "ROA")) }
    }

    private fun updateChart(composite: CompositeQuote) {
        ChartFactory
            .createLineChart(
                null, "Évek", "${composite.quote.shareSummary.currency}",
                composite.chart.toDataSet(), VERTICAL, false, false, false
            )
            .apply { categoryPlot.domainAxis.apply { categoryLabelPositions = DOWN_90 } }
            .let {
                chartsGui.pricePanel.removeAll()
                // Intellij grid layout manager is useless here, as we manually add the component to the form and does not know the grid constants.
                chartsGui.pricePanel.add(
                    ChartPanel(it)
                        .apply {
                            popupMenu = null
                            isDomainZoomable = false
                            isRangeZoomable = false
                        }, BorderLayout.CENTER
                )
                chartsGui.pricePanel.revalidate()
                chartsGui.pricePanel.repaint()
            }
    }

    private fun Chart.toDataSet() = DefaultCategoryDataset().apply {
        quoteOpens.forEach {
            addValue(it.value.toDouble(), "Részvény ára", ofInstant(it.date, UTC).year)
        }
    }

    private fun updateQuotePanel(quote: Quote) = calcGui.run {
        quoteLabel.text = quote.quoteSummary.longName
        quoteShortLabel.text = quote.quoteSummary.shortName
        peLabel.text = "${quote.shareSummary.ttmPE} (15 az átlagos)"
        quoteTypeLabel.text = when (quote.quoteSummary.type) {
            "EQUITY" -> "részvény"
            "CURRENCY" -> "valuta"
            else -> "index"
        }
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
            .map {
                listOf(
                    ofInstant(it.date, UTC).year, formatter.format(it.freeCashFlow),
                    formatter.format(it.longTermDebt), it.yearsToPaybackDebt
                ).toTypedArray()
            }
            .run {
                calcGui.fcfTable.model = DefaultTableModel(
                    toTypedArray(),
                    arrayOf("Év", "FCF", "LTD", "Years to pay back")
                )
            }
}
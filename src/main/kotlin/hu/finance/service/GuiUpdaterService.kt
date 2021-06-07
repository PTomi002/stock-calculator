package hu.finance.service

import hu.finance.calculator.*
import hu.finance.gui.CalculatorGUI
import hu.finance.gui.ChartsGUI
import hu.finance.gui.util.AutoCloseableLock
import hu.finance.gui.util.CalcWorker
import hu.finance.model.Quote
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import java.awt.BorderLayout
import java.text.DecimalFormat
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.ThreadLocalRandom
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
                                "Error happened: ${workerResult.error!!.message}"
                            )
                            workerResult.error.printStackTrace()
                        } else {
                            lock.withLock { cqCache = workerResult.data!! }
                            cqCache!!.run {
                                updateQuotePanel(quote)
                                updateRoeTable(quote)
                                updateRotcTable(this)
                                updateEpsTable(this)
                                updateDetTable(this)
                                updateFcfTable(this)
                                updateCharts(chartsGui, this)
                            }
                        }
                    }).execute()
            }
    }

    private fun updateCharts(chartsGUI: ChartsGUI, compositeQuote: CompositeQuote) {
        val lineChart = ChartFactory.createLineChart(
            "teszt",
            "Years", "Number of Schools",
            createDataset(),
            PlotOrientation.VERTICAL,
            true, true, false
        ).apply {
            categoryPlot.domainAxis.apply {
                categoryLabelPositions = CategoryLabelPositions.DOWN_90
            }
        }
        chartsGUI.pricePanel.removeAll()
        // Intellij grid layout manager is useless here, as we manually add the component to the form and does not know the grid constants.
        chartsGUI.pricePanel.add(ChartPanel(lineChart).apply {
            popupMenu = null
            isDomainZoomable = false
            isRangeZoomable = false
        }, BorderLayout.CENTER)
        chartsGUI.pricePanel.revalidate()
        chartsGUI.pricePanel.repaint()

    }

    private fun createDataset(): DefaultCategoryDataset? {
        val dataset = DefaultCategoryDataset()
        repeat(60) {
            dataset.addValue(ThreadLocalRandom.current().nextInt(), "schools", "$it")
        }
        return dataset
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

    private fun updateRoeTable(quote: Quote) =
        roeCalculator.calculate(quote)
            .map {
                listOf(
                    ZonedDateTime.ofInstant(it.date, ZoneOffset.UTC).year,
                    "${it.roe} %"
                ).toTypedArray()
            }
            .run { calcGui.roeTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "ROE")) }

    private fun updateRotcTable(quote: CompositeQuote) =
        rotcCalculator.calculate(quote)
            .map {
                listOf(
                    ZonedDateTime.ofInstant(it.date, ZoneOffset.UTC).year,
                    "${it.rotc} %"
                ).toTypedArray()
            }
            .run { calcGui.rotcTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "ROTC")) }

    private fun updateEpsTable(quote: CompositeQuote) =
        epsCalculator.calculate(quote)
            .map {
                listOf(
                    ZonedDateTime.ofInstant(it.date, ZoneOffset.UTC).year,
                    "${it.eps} ${quote.quote.shareSummary.currency}"
                ).toTypedArray()
            }
            .run { calcGui.epsTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "EPS")) }

    private fun updateDetTable(quote: CompositeQuote) =
        dteCalculator.calculate(quote)
            .map {
                listOf(
                    ZonedDateTime.ofInstant(it.date, ZoneOffset.UTC).year,
                    "${it.dte} ratio"
                ).toTypedArray()
            }
            .run { calcGui.dteTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "DTE")) }

    private fun updateFcfTable(quote: CompositeQuote) =
        flowCalculator.calculate(quote)
            .map { fcf ->
                listOf(
                    ZonedDateTime.ofInstant(fcf.date, ZoneOffset.UTC).year,
                    formatter.format(fcf.freeCashFlow),
                    fcf.longTermDebt?.let { formatter.format(it) } ?: "",
                    fcf.yearsToPaybackDebt ?: ""
                ).toTypedArray()
            }
            .run {
                calcGui.fcfTable.model =
                    DefaultTableModel(toTypedArray(), arrayOf("Év", "FCF", "LTD", "Years to pay back"))
            }
}
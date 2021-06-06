package hu.finance.service

import hu.finance.calculator.EarningPerShareCalculator
import hu.finance.calculator.ReturnOnEquityCalculator
import hu.finance.calculator.ReturnOnTotalCapitalCalculator
import hu.finance.gui.CalculatorGUI
import hu.finance.gui.util.AutoCloseableLock
import hu.finance.gui.util.CalcWorker
import hu.finance.model.Quote
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.table.DefaultTableModel

/**
 * GUI designer mvn plugin uses old asm lib, and the plugin itself is too old so it generates faulty bytecode.
 * </br>
 * So refactored every GUI update logic to this class.
 */
class GuiService(
    private val gui: CalculatorGUI,
    private val finances: Finances,
    private val lock: AutoCloseableLock
) {
    @Volatile
    private var cqCache: CompositeQuote? = null

    private val roeCalculator = ReturnOnEquityCalculator()
    private val rotcCalculator = ReturnOnTotalCapitalCalculator()
    private val epsCalculator = EarningPerShareCalculator()

    fun addLoadQuote(loadQuote: JMenuItem) = loadQuote.addActionListener { loadQuote() }

    fun loadQuote() {
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
                            }
                        }
                    }).execute()
            }
    }

    private fun updateQuotePanel(quote: Quote) = gui.run {
        quoteLabel.text = quote.quoteSummary.name
        exchangeLabel.text = quote.quoteSummary.exchange
        openPriceLabel.text = "${quote.shareSummary.open}"
        previousOpenPriceLabel.text = "${quote.shareSummary.previousClose}"
        currencyLabel.text = "${quote.shareSummary.currency}"
    }

    private fun updateRoeTable(quote: Quote) =
        roeCalculator.calculate(quote)
            .map { listOf(ZonedDateTime.ofInstant(it.date, ZoneOffset.UTC).year, "${it.roe} %").toTypedArray() }
            .run { gui.roeTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "ROE")) }

    private fun updateRotcTable(quote: CompositeQuote) =
        rotcCalculator.calculate(quote)
            .map { listOf(ZonedDateTime.ofInstant(it.date, ZoneOffset.UTC).year, "${it.rotc} %").toTypedArray() }
            .run { gui.rotcTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "ROTC")) }

    private fun updateEpsTable(quote: CompositeQuote) =
        epsCalculator.calculate(quote)
            .map {
                listOf(
                    ZonedDateTime.ofInstant(it.date, ZoneOffset.UTC).year,
                    "${it.eps} ${quote.quote.shareSummary.currency}"
                ).toTypedArray()
            }
            .run { gui.epsTable.model = DefaultTableModel(toTypedArray(), arrayOf("Év", "EPS")) }
}
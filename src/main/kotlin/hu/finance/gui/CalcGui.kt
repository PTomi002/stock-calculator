package hu.finance.gui

import hu.finance.calculator.CompositeSheets
import hu.finance.calculator.EarningPerShareCalculator
import hu.finance.formatter.CompanyFormatter
import hu.finance.formatter.EarningPerShareFormatter
import hu.finance.gui.component.*
import hu.finance.gui.util.AutoCloseableLock
import hu.finance.gui.util.CalcWorker
import hu.finance.model.Quote
import hu.finance.model.TimeSeries
import hu.finance.service.CompositeQuote
import hu.finance.service.Finances
import java.awt.Dimension
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane

private const val DEFAULT_SIZE = 600

class CalcGui(
    title: String,
    private val finances: Finances
) : JFrame(title) {

    private val lock = AutoCloseableLock(ReentrantLock())

    @Volatile
    private var isInitialized = false

    @GuardedBy(value = "lock")
    private var qCache: Quote? = null

    @GuardedBy(value = "lock")
    private var tsCache: TimeSeries? = null

    private var mainPanel: MainPanel
    private var bodyPanel: BodyPanel
    private var quotePanel: QuotePanel
    private var epsPanel: EarningPerSharePanel
    private var headerToolBar: HeaderToolBar
    private val loadQuoteButton = JButton("Load Quote")
    private val quoteButton = JButton("Quote")
    private val epsButton = JButton("Earning Per Share")

    init {
        // action buttons
        quoteButton.addActionListener { showQuote() }
        loadQuoteButton.addActionListener { loadQuote() }
        epsButton.addActionListener { calculateEps() }

        // changing panels
        quotePanel = QuotePanel()
        epsPanel = EarningPerSharePanel()

        // header
        headerToolBar = HeaderToolBar(loadQuoteButton, quoteButton, epsButton)

        // main panels
        bodyPanel = BodyPanel(quotePanel, epsPanel)
        mainPanel = MainPanel(
            headerToolBar = headerToolBar,
            bodyPanel = bodyPanel
        )

        add(mainPanel)

        pack()
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(DEFAULT_SIZE, DEFAULT_SIZE)
        isAutoRequestFocus = true
        isVisible = true
    }

    private fun showQuote() {
        if (!isInitialized) return
        bodyPanel.update(quotePanel)
    }

    private fun calculateEps() {
        if (!isInitialized) return
        epsPanel.update(
            lock.withLock {
                EarningPerShareFormatter().format(
                    EarningPerShareCalculator().calculate(
                        CompositeSheets(bs = qCache!!, ts = tsCache!!)
                    )
                )
            }
        )
        bodyPanel.update(epsPanel)
    }

    private fun loadQuote() {
        JOptionPane.showInputDialog("Company ticker?")
            .takeIf { !it.isNullOrEmpty() }
            ?.also { ticker ->
                CalcWorker<CompositeQuote, Unit>(
                    loader = { finances.loadQuote(ticker = ticker) },
                    callback = {
                        if (!it.result) {
                            JOptionPane.showMessageDialog(null, "Error happened: ${it.error?.message}");
                            it.error?.printStackTrace()
                        } else {
                            lock.withLock {
                                qCache = it.data!!.quote
                                tsCache = it.data.timeSeries
                                isInitialized = true
                                quotePanel.update(CompanyFormatter().format(qCache!!))
                            }
                            bodyPanel.update(quotePanel)
                        }
                    }
                ).execute()
            }
    }

}

package hu.finance.gui

import hu.finance.formatter.CompanyFormatter
import hu.finance.gui.component.BodyPanel
import hu.finance.gui.component.ExcerptPanel
import hu.finance.gui.component.HeaderToolBar
import hu.finance.gui.component.MainPanel
import hu.finance.gui.util.AutoCloseableLock
import hu.finance.gui.util.CalcWorker
import hu.finance.model.Quote
import hu.finance.model.TimeSeries
import hu.finance.service.CompositeQuote
import hu.finance.service.Finances
import java.awt.Dimension
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import javax.swing.*

private const val DEFAULT_SIZE = 600

class CalcGui(
    title: String,
    private val finances: Finances
) : JFrame(title) {

    private val lock = AutoCloseableLock(ReentrantLock())

    @GuardedBy(value = "lock")
    private lateinit var quote: Quote

    @GuardedBy(value = "lock")
    private lateinit var timeSeries: TimeSeries

    private var mainPanel: JPanel
    private var bodyPanel: BodyPanel
    private var excerptPanel: ExcerptPanel
    private var headerToolBar: JToolBar
    private val loadQuoteButton = JButton("Load Quote")

    init {
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(DEFAULT_SIZE, DEFAULT_SIZE)
        isAutoRequestFocus = true
        isVisible = true

        excerptPanel = ExcerptPanel()

        bodyPanel = BodyPanel()

        loadQuoteButton.apply {
            addActionListener { loadCompanyAction() }
        }

        headerToolBar = HeaderToolBar(
            loadQuoteButton = loadQuoteButton
        )

        mainPanel = MainPanel(
            headerToolBar = headerToolBar,
            detailsPanel = bodyPanel
        )

        add(mainPanel)
    }

    private fun loadCompanyAction() {
        JOptionPane.showInputDialog("Company ticker?")
            .takeIf { !it.isNullOrEmpty() }
            ?.also { ticker ->
                CalcWorker<CompositeQuote, Unit>(
                    loader = { finances.loadQuote(ticker = ticker) },
                    callback = {
                        lock.withLock {
                            quote = it.quote
                            timeSeries = it.timeSeries
                        }
                        excerptPanel.update(CompanyFormatter().format(quote))
                        bodyPanel.update(excerptPanel)
                    }
                ).execute()
            }
    }

}

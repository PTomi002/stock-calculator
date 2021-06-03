package hu.finance.gui.component

import hu.finance.formatter.FormattedData
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.*

interface UpdatableComponent<T> {
    fun update(data: T)
}

class QuotePanel : JPanel(), UpdatableComponent<FormattedData> {
    init {
        layout = GridLayout()
    }

    override fun update(data: FormattedData) = refresh {
        layout = GridLayout(data.data.size, 2)
        data.data.forEach { (label, value) ->
            add(JLabel(label))
            add(JTextField(value).apply { isEditable = false })
        }
    }
}

class BodyPanel(
    private val quotePanel: QuotePanel
) : JPanel(), UpdatableComponent<String> {
    init {
        layout = BorderLayout()
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        background = Color.WHITE
        add(quotePanel, BorderLayout.NORTH)
    }

    override fun update(data: String) {
        quotePanel.isVisible = true
    }
}

class MainPanel(
    headerToolBar: JToolBar,
    detailsPanel: JPanel
) : JPanel() {
    init {
        layout = BorderLayout()
        add(headerToolBar, BorderLayout.NORTH)
        add(detailsPanel, BorderLayout.CENTER)
    }
}

class HeaderToolBar(
    loadQuoteButton: JButton
) : JToolBar() {
    init {
        isFloatable = false
        add(loadQuoteButton)
    }
}

private fun JComponent.refresh(block: (JComponent) -> Unit) {
    removeAll()
    block.invoke(this)
    revalidate()
    repaint()
}
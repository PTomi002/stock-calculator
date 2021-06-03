package hu.finance.gui.component

import hu.finance.formatter.FormattedData
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

interface UpdatableComponent<T> {
    fun update(data: T)
}

class ExcerptPanel : JPanel(), UpdatableComponent<FormattedData> {
    init {
        layout = GridLayout()
    }

    override fun update(data: FormattedData) = replaceComponents {
        layout = GridLayout(data.data.size, 2)
        data.data.forEach { (label, value) ->
            add(JLabel(label))
            add(JTextField(value).apply { isEditable = false })
        }
    }
}

class BodyPanel : JPanel(), UpdatableComponent<JPanel> {
    init {
        layout = BorderLayout()
    }

    override fun update(data: JPanel) = replaceComponents {
        add(data, BorderLayout.NORTH)
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

private fun JComponent.replaceComponents(block: (JComponent) -> Unit) {
    removeAll()
    block.invoke(this)
    revalidate()
    repaint()
}
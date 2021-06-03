package hu.finance.gui.component

import hu.finance.formatter.FormattedData
import java.awt.*
import javax.swing.*

private val DEFAULT_GBC = GridBagConstraints().apply {
    weightx = 1.0
    fill = GridBagConstraints.NORTH
    gridwidth = GridBagConstraints.REMAINDER
}

interface UpdatableComponent<T> {
    fun update(data: T)
}

class EarningPerSharePanel : JPanel(), UpdatableComponent<FormattedData> {

    private val title = JLabel("Earning Per Share summary page")
    private val hintSection = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }
    private val bodySection = JPanel()

    init {
        isVisible = false
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(title)
        add(hintSection)
        add(bodySection)
    }

    override fun update(data: FormattedData) = refresh {
        hintSection.apply {
            removeAll()
            data.hints.forEach { add(JLabel(it)) }
        }

        bodySection.apply {
            removeAll()
            add(
                JTable(
                    Array(3) { Array(5) { 0 } },
                    data.data.keys.toTypedArray()
                )
            )
        }
    }
}

class QuotePanel : JPanel(), UpdatableComponent<FormattedData> {

    private val title = JLabel("Quote summary page")
    private val hintSection = JPanel()
    private val bodySection = JPanel()

    companion object Constants {
        private const val COL_NUM = 2
    }

    init {
        isVisible = false
        layout = GridBagLayout()
        add(title, DEFAULT_GBC)
        add(hintSection, DEFAULT_GBC)
        add(bodySection, DEFAULT_GBC)
    }

    override fun update(data: FormattedData) = refresh {
        hintSection.apply {
            removeAll()
            data.hints.forEach { add(JLabel(it)) }
        }
        bodySection.apply {
            removeAll()
            layout = GridLayout(data.data.size, COL_NUM)
            data.data.forEach { (label, value) ->
                add(JLabel(label))
                add(JTextField("$value").apply { isEditable = false })
            }
        }
    }
}

class BodyPanel(
    val panels: List<JPanel>
) : JPanel(), UpdatableComponent<JPanel> {
    constructor(vararg panels: JPanel) : this(panels.asList())

    companion object CONSTANTS {
        private const val PADDING = 10
    }

    init {
        layout = GridBagLayout()
        border = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        background = Color.WHITE
        panels.forEach { add(it, DEFAULT_GBC) }
    }

    override fun update(data: JPanel) = refresh {
        panels
            .partition { data === it }
            .let { partitioned ->
                partitioned.first.first().isVisible = true
                partitioned.second.forEach { it.isVisible = false }
            }
    }
}

class HeaderToolBar(
    buttons: List<JButton>
) : JToolBar() {
    constructor(vararg buttons: JButton) : this(buttons.asList())

    init {
        isFloatable = false
        buttons.forEach { add(it) }
    }
}

class MainPanel(
    headerToolBar: JToolBar,
    bodyPanel: JPanel
) : JPanel() {
    init {
        layout = BorderLayout()
        add(headerToolBar, BorderLayout.NORTH)
        add(bodyPanel, BorderLayout.CENTER)
    }
}

private fun JComponent.refresh(block: (JComponent) -> Unit) {
    block.invoke(this)
    revalidate()
    repaint()
}
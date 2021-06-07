@file:Suppress("UnstableApiUsage")

package hu.finance

import hu.finance.gui.CalculatorGUI

fun main() {
    CalculatorGUI("Stock Calculator")
        .apply {
            isVisible = true
        }
}

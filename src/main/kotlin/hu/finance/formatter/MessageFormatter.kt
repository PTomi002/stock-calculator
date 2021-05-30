package hu.finance.formatter

import hu.finance.api.model.BalanceSheet
import hu.finance.voter.ReturnOnEquityVoter.ROEDecision

interface MessageFormatter<T> {
    fun format(value: T): String
    fun print(value: T): Unit = println(format(value))
}

class CompanyFormatter : MessageFormatter<BalanceSheet> {
    override fun format(value: BalanceSheet): String =
        StringBuilder().apply {
            appendLine("Vállalkozás: ${value.company.name}")
            appendLine("Tőzsde: ${value.company.exchange}")
            appendLine("Részvény nyitáskor: ${value.share.open} ${value.share.currency}")
            appendLine("Részvény előző nyitáskor: ${value.share.previousClose} ${value.share.currency}")
        }.toString()
}

class ReturnOnEquityFormatter : MessageFormatter<ROEDecision> {
    override fun format(value: ROEDecision): String =
        StringBuilder().apply {
            appendLine("Return On Equity (sajáttőke megtérülés)")
            appendLine("Note 1: Hosszú Távú Versenyelőny => következetesen magas és növekvő tendenciát keresünk")
            appendLine("Note 2: 10-15% a jó egy árversenyben lévő vállalatnál")
            value.roe.forEach { appendLine("Év: ${it.first} ROE: ${it.second} %") }
            appendLine("Döntési warningok:")
            value.hint.forEach { appendLine(it) }
        }.toString()
}

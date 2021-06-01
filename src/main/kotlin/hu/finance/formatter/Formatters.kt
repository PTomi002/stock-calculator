package hu.finance.formatter

import hu.finance.api.model.BalanceSheet
import hu.finance.voter.ReturnOnEquityCalculator.ReturnOnEquity
import hu.finance.voter.ReturnOnTotalCapitalCalculator.ReturnOnTotalCapital

interface Formatter<T> {
    fun format(value: T): String
}

class CompanyFormatter : Formatter<BalanceSheet> {
    override fun format(value: BalanceSheet): String =
        StringBuilder().apply {
            appendLine("Vállalkozás: ${value.company.name}")
            appendLine("Tőzsde: ${value.company.exchange}")
            appendLine("Részvény nyitáskor: ${value.share.open} ${value.share.currency}")
            appendLine("Részvény előző nyitáskor: ${value.share.previousClose} ${value.share.currency}")
        }.toString()
}

class ReturnOnEquityFormatter : Formatter<List<ReturnOnEquity>> {
    override fun format(value: List<ReturnOnEquity>): String =
        StringBuilder().apply {
            appendLine("==== Return On Equity (sajáttőke megtérülés) ====")
            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk")
            appendLine("Hint: 10-15% a jó egy árversenyben lévő vállalatnál")
            appendLine("Hint: bank és pénzintézeteknél ez az érték 12% felett már jó")
            appendLine()
            value.forEach { appendLine("Év: ${it.date} ROE: ${it.roe} %") }
        }.toString()
}

class ReturnOnTotalCapitalFormatter : Formatter<List<ReturnOnTotalCapital>> {
    override fun format(value: List<ReturnOnTotalCapital>): String =
        StringBuilder().apply {
            appendLine("==== Return On Total Capital (teljes tőke megtérülés) ====")
            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk")
            appendLine("Hint: 12%-nál magasabb értékkel rendelkező vállalatok a jók")
            appendLine("Hint: bank és pénzintézeteknél ez az érték 1% felett már jó")
            appendLine()
            value.forEach { appendLine("Év: ${it.date} ROTC: ${it.rotc} %") }
        }.toString()
}


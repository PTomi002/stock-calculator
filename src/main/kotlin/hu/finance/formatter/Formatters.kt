package hu.finance.formatter

import hu.finance.api.model.BalanceSheet
import hu.finance.voter.DebtToEquityCalculator.DebtToEquity
import hu.finance.voter.EarningPerShareCalculator.EarningPerShare
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
            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk.")
            appendLine("Hint: 10-15% már jó egy árversenyben lévő vállalatnál.")
            appendLine("Hint: Bank és pénzintézeteknél ez az érték 12% felett már jó.")
            appendLine()
            value.forEach { appendLine("Év: ${it.date} ROE: ${it.roe} %") }
        }.toString()
}

class ReturnOnTotalCapitalFormatter : Formatter<List<ReturnOnTotalCapital>> {
    override fun format(value: List<ReturnOnTotalCapital>): String =
        StringBuilder().apply {
            appendLine("==== Return On Total Capital (teljes tőke megtérülés) ====")
            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk.")
            appendLine("Hint: 12% már jó egy árversenyben lévő vállalatnál.")
            appendLine("Hint: Bank és pénzintézeteknél ez az érték 1% felett már jó")
            appendLine()
            value.forEach { appendLine("Év: ${it.date} ROTC: ${it.rotc} %") }
        }.toString()
}

class EarningPerShareFormatter : Formatter<List<EarningPerShare>> {
    override fun format(value: List<EarningPerShare>): String =
        StringBuilder().apply {
            appendLine("==== Earning Per Share (1 részvény éves nyeresége) ====")
            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk")
            appendLine("Hint: Fluktuált eps a rossz vezetőség jele.")
            appendLine()
            value.forEach { appendLine("Év: ${it.date} EPS: ${it.eps}") }
        }.toString()
}

class DebtToEquityFormatter : Formatter<List<DebtToEquity>> {
    override fun format(value: List<DebtToEquity>): String =
        StringBuilder().apply {
            appendLine("==== Debt To Equity (saját-tőkére vetített adósság) ====")
            appendLine("Hint: Következetesen alacsony, nem ingadozó és csökkenő tendenciát keresünk")
            appendLine("Hint: 0.5 alatt elfogadható, DE nem irányadó a végső döntésnél.")
            appendLine()
            value.forEach { appendLine("Év: ${it.date} DTE: ${it.dte}") }
        }.toString()
}


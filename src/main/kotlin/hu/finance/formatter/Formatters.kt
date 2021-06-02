package hu.finance.formatter

import hu.finance.calculator.DebtToEquityCalculator.DebtToEquity
import hu.finance.calculator.EarningPerShareCalculator.EarningPerShare
import hu.finance.calculator.FreeCashFlowCalculator.FreeCashFlow
import hu.finance.calculator.ReturnOnEquityCalculator.ReturnOnEquity
import hu.finance.calculator.ReturnOnTotalCapitalCalculator.ReturnOnTotalCapital
import hu.finance.model.Quote
import java.text.DecimalFormat

private val formatter = DecimalFormat("#,###.00");

interface Formatter<T> {
    fun format(value: T): String
}

class CompanyFormatter : Formatter<Quote> {
    override fun format(value: Quote): String =
        StringBuilder().apply {
            appendLine("Vállalkozás: ${value.quoteSummary.name}")
            appendLine("Tőzsde: ${value.quoteSummary.exchange}")
            appendLine("Részvény nyitáskor: ${value.shareSummary.open} ${value.shareSummary.currency}")
            appendLine("Részvény előző nyitáskor: ${value.shareSummary.previousClose} ${value.shareSummary.currency}")
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
            appendLine("==== Return On Total Capital (teljes-tőke megtérülés) ====")
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
            value.forEach { appendLine("Év: ${it.date} EPS: ${it.eps} ${it.currency}") }
        }.toString()
}

class DebtToEquityFormatter : Formatter<List<DebtToEquity>> {
    override fun format(value: List<DebtToEquity>): String =
        StringBuilder().apply {
            appendLine("==== Debt To Equity (saját-tőkére vetített adósság) ====")
            appendLine("Hint: Következetesen alacsony, nem ingadozó és csökkenő tendenciát keresünk")
            appendLine("Hint: 0.5 alatt elfogadható, DE nem irányadó a végső döntésnél.")
            appendLine()
            value.forEach { appendLine("Év: ${it.date} Debt/Equity: ${it.dte} ratio") }
        }.toString()
}

class FreeCashFlowFormatter : Formatter<List<FreeCashFlow>> {
    override fun format(value: List<FreeCashFlow>): String =
        StringBuilder().apply {
            appendLine("==== Free Cash Flow (befolyó nettó készpénzáramlás) ====")
            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk")
            appendLine("Hint: Max 5 év, de általában 2-3 év alatt kifizeti egy jó cég a hiteleit.")
            appendLine()
            value.forEach {
                appendLine(
                    "Év: ${it.date} Free cash flow: ${formatter.format(it.freeCashFlow)} ${it.currency} Long-Term Debt: ${
                        formatter.format(
                            it.longTermDebt
                        )
                    } ${it.currency} Years to payback: ${it.yearsToPaybackDebt}"
                )
            }
        }.toString()
}


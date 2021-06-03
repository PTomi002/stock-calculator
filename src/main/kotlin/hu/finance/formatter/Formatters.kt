package hu.finance.formatter

import hu.finance.calculator.EarningPerShareCalculator.EarningPerShare
import hu.finance.model.Quote
import java.text.DecimalFormat

private val formatter = DecimalFormat("#,###.00");

data class FormattedData(
    val hints: List<String> = emptyList(),
    val data: Map<String, Any> = emptyMap()
)

interface Formatter<T> {
    fun format(data: T): FormattedData
}

class CompanyFormatter : Formatter<Quote> {
    override fun format(data: Quote): FormattedData =
        FormattedData(
            data = mapOf(
                "Vállalkozás" to data.quoteSummary.name,
                "Tőzsde" to data.quoteSummary.exchange,
                "Pénznem" to "${data.shareSummary.currency}",
                "Részvény nyitáskor" to "${data.shareSummary.open}",
                "Részvény előző nyitáskor" to "${data.shareSummary.previousClose}",
            )
        )
}

//class ReturnOnEquityFormatter : Formatter<List<ReturnOnEquity>> {
//    override fun format(value: List<ReturnOnEquity>): String =
//        StringBuilder().apply {
//            appendLine("==== Return On Equity (sajáttőke megtérülés) ====")
//            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk.")
//            appendLine("Hint: 10-15% már jó egy árversenyben lévő vállalatnál.")
//            appendLine("Hint: Bank és pénzintézeteknél ez az érték 12% felett már jó.")
//            appendLine()
//            value.forEach { appendLine("Év: ${it.date} ROE: ${it.roe} %") }
//        }.toString()
//}
//
//class ReturnOnTotalCapitalFormatter : Formatter<List<ReturnOnTotalCapital>> {
//    override fun format(value: List<ReturnOnTotalCapital>): String =
//        StringBuilder().apply {
//            appendLine("==== Return On Total Capital (teljes-tőke megtérülés) ====")
//            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk.")
//            appendLine("Hint: 12% már jó egy árversenyben lévő vállalatnál.")
//            appendLine("Hint: Bank és pénzintézeteknél ez az érték 1% felett már jó")
//            appendLine()
//            value.forEach { appendLine("Év: ${it.date} ROTC: ${it.rotc} %") }
//        }.toString()
//}
//
class EarningPerShareFormatter : Formatter<List<EarningPerShare>> {
    override fun format(value: List<EarningPerShare>): FormattedData =
        FormattedData(
            hints = listOf(
                "Következetesen magas, nem ingadozó és növekvő tendenciát keresünk.",
                "Fluktuált eps a rossz vezetőség jele."
            ),
            data = mapOf(
                "Év" to value.map { "${it.date}" },
                "EPS" to value.map { "${it.eps}" },
                "Pénznem" to value.map { "${it.currency}" }
            )
        )
}
//
//class DebtToEquityFormatter : Formatter<List<DebtToEquity>> {
//    override fun format(value: List<DebtToEquity>): String =
//        StringBuilder().apply {
//            appendLine("==== Debt To Equity (saját-tőkére vetített adósság) ====")
//            appendLine("Hint: Következetesen alacsony, nem ingadozó és csökkenő tendenciát keresünk")
//            appendLine("Hint: 0.5 alatt elfogadható, DE nem irányadó a végső döntésnél.")
//            appendLine()
//            value.forEach { appendLine("Év: ${it.date} Debt/Equity: ${it.dte} ratio") }
//        }.toString()
//}
//
//class FreeCashFlowFormatter : Formatter<List<FreeCashFlow>> {
//    override fun format(value: List<FreeCashFlow>): String =
//        StringBuilder().apply {
//            appendLine("==== Free Cash Flow (befolyó nettó készpénzáramlás) ====")
//            appendLine("Hint: Következetesen magas, nem ingadozó és növekvő tendenciát keresünk")
//            appendLine("Hint: Max 5 év, de általában 2-3 év alatt kifizeti egy jó cég a hiteleit.")
//            appendLine()
//            value.forEach {
//                appendLine(
//                    "Év: ${it.date} Free cash flow: ${formatter.format(it.freeCashFlow)} ${it.currency} Long-Term Debt: ${
//                        formatter.format(
//                            it.longTermDebt
//                        )
//                    } ${it.currency} Years to payback: ${it.yearsToPaybackDebt}"
//                )
//            }
//        }.toString()
//}


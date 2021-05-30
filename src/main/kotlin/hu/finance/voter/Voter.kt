package hu.finance.voter

import hu.finance.api.model.BalanceSheet
import hu.finance.formatter.MessageFormatter
import hu.finance.util.Maths
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

interface Voter<T> {
    fun vote(question: T): Decision
}

abstract class Decision(
    val result: Boolean,
    val hint: List<String>
)

class ReturnOnEquityVoter(
    private val formatter: MessageFormatter<ROEDecision>
) : Voter<BalanceSheet> {

    override fun vote(question: BalanceSheet): Decision {
        val roes = question.balanceSheetHistory.balanceSheetStatements
            .map { bs -> bs to question.incomeStatementHistory.incomeStatements.find { it.date == bs.date }!! }
            .map {
                it.first.date to Maths.roe(
                    netIncome = it.second.netIncome,
                    shareholderEquity = Maths.equity(
                        totalAsset = it.first.totalAssets,
                        totalLiability = it.first.totalLiabilities
                    )
                )
            }
        val negativeYears = roes.filter { it.second <= 0.toBigDecimal() }
            .takeIf { it.isNotEmpty() }
            ?.let {
                it.joinToString(prefix = "Veszteséges évek: ") {
                    "${ZonedDateTime.ofInstant(it.first, ZoneId.of("UTC")).year}"
                }
            }
        return ROEDecision(true, listOfNotNull(negativeYears), roes).also { formatter.print(it) }
    }

    inner class ROEDecision(
        result: Boolean,
        hints: List<String>,
        val roe: List<Pair<Instant, BigDecimal>>,
    ) : Decision(result, hints)
}

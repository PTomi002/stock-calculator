package hu.finance.voter

import hu.finance.api.model.BalanceSheet
import hu.finance.util.Maths
import java.math.BigDecimal
import java.util.logging.Logger

interface Voter<T> {
    fun vote(question: T): Decision
}

data class Decision(
    val vote: Boolean,
    val reason: String? = null
)

class ReturnOnEquityVoter : Voter<BalanceSheet> {

    private val logger = Logger.getLogger(javaClass.canonicalName)

    override fun vote(question: BalanceSheet): Decision {
        val tmp = BigDecimal.ZERO
        question.balanceSheetHistory.balanceSheetStatements
            .map { bs -> bs to question.incomeStatementHistory.incomeStatements.find { it.date == bs.date }!! }
            .map {
                val roe = Maths.roe(
                    netIncome = it.second.netIncome,
                    shareholderEquity = Maths.equity(
                        totalAsset = it.first.totalAssets,
                        totalLiability = it.first.totalLiabilities
                    )
                )
                val yearsToDoubleEquity = Maths.seventyTwoRule(roe.toDouble())
                Triple(it.first.date, roe, yearsToDoubleEquity)
            }
            .onEach { logger.info { "Year: ${it.first} - ROE: ${it.second} % - Years to double equity: ${it.third} year" } }
        return Decision(true)
    }
}

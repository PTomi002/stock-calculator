@file:Suppress("UnstableApiUsage")

package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.YahooApi
import hu.finance.calculator.*
import hu.finance.formatter.*
import org.apache.commons.cli.*

private val cliParser: CommandLineParser = DefaultParser()
private val cliOptions = Options().apply {
    addOption(Option.builder("ticker").desc("Company ticker.").required().hasArg().build())
}
private val financeGateway = YahooApi(RateLimiter.create(4.0))

fun main(args: Array<String>) {
    try {
        val cli = cliParser.parse(cliOptions, args)
        val quote = financeGateway.quote(
            ticker = requireNotNull(cli.getOptionValue("ticker")) { "Ticker can not be empty!" },
            filters = listOf(
                "balanceSheetHistory",
                "incomeStatementHistory",
                "summaryDetail",
                "price",
                "cashflowStatementHistory"
            )
        )
        val timeSeries = financeGateway.timeSeries(
            ticker = requireNotNull(cli.getOptionValue("ticker")) { "Ticker can not be empty!" },
            filters = listOf(
                "annualTotalCapitalization",
                "annualCapitalExpenditure",
                "annualShareIssued",
                "annualLongTermDebt",
                "annualStockholdersEquity"
            )
        )

        println(CompanyFormatter().format(quote))
        ReturnOnEquityCalculator().calculate(quote).run {
            println(ReturnOnEquityFormatter().format(this))
        }
        ReturnOnTotalCapitalCalculator().calculate(CompositeSheets(quote, timeSeries)).run {
            println(ReturnOnTotalCapitalFormatter().format(this))
        }
        EarningPerShareCalculator().calculate(CompositeSheets(quote, timeSeries)).run {
            println(EarningPerShareFormatter().format(this))
        }
        DebtToEquityCalculator().calculate(CompositeSheets(quote, timeSeries)).run {
            println(DebtToEquityFormatter().format(this))
        }
        FreeCashFlowCalculator().calculate(CompositeSheets(quote, timeSeries)).run {
            println(FreeCashFlowFormatter().format(this))
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
        HelpFormatter().printHelp("FinanceCalculator", cliOptions)
    }
}

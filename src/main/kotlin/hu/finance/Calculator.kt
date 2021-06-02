package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.YahooApi
import hu.finance.formatter.*
import hu.finance.voter.*
import org.apache.commons.cli.*

private val cliParser: CommandLineParser = DefaultParser()
private val cliOptions = Options().apply {
    addOption(Option.builder("ticker").desc("Company ticker.").required().hasArg().build())
}
private val financeGateway = YahooApi(RateLimiter.create(4.0))

fun main(args: Array<String>) {
    try {
        val cli = cliParser.parse(cliOptions, args)
        val balanceSheet = financeGateway.balanceSheet(
            ticker = requireNotNull(cli.getOptionValue("ticker")) { "Ticker can not be empty!" },
            filters = listOf(
                "balanceSheetHistory",
                "incomeStatementHistory",
                "summaryDetail",
                "price",
                "cashflowStatementHistory"
            )
        )
        val timeSeries = financeGateway.timeseries(
            ticker = requireNotNull(cli.getOptionValue("ticker")) { "Ticker can not be empty!" },
            filters = listOf(
                "annualShareIssued",
                "annualLongTermDebt",
                "annualStockholdersEquity"
            )
        )

        println(CompanyFormatter().format(balanceSheet))
        ReturnOnEquityCalculator().calculate(balanceSheet).run {
            println(ReturnOnEquityFormatter().format(this))
        }
        ReturnOnTotalCapitalCalculator().calculate(balanceSheet).run {
            println(ReturnOnTotalCapitalFormatter().format(this))
        }
        EarningPerShareCalculator().calculate(CompositSheets(balanceSheet, timeSeries)).run {
            println(EarningPerShareFormatter().format(this))
        }
        DebtToEquityCalculator().calculate(CompositSheets(balanceSheet, timeSeries)).run {
            println(DebtToEquityFormatter().format(this))
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
        HelpFormatter().printHelp("FinanceCalculator", cliOptions)
    }
}

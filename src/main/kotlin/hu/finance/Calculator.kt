package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.YahooApi
import hu.finance.formatter.CompanyFormatter
import hu.finance.formatter.ReturnOnEquityFormatter
import hu.finance.formatter.ReturnOnTotalCapitalFormatter
import hu.finance.voter.ReturnOnEquityCalculator
import hu.finance.voter.ReturnOnTotalCapitalCalculator
import org.apache.commons.cli.*

private val cliParser: CommandLineParser = DefaultParser()
private val cliOptions = Options().apply {
    addOption(Option.builder("ticker").desc("Company ticker.").required().hasArg().build())
    addOption(Option.builder("exchange").desc("Exchange to use. (optional)").hasArg().build())
}
private val financeGateway = YahooApi(RateLimiter.create(4.0))

fun main(args: Array<String>) {
    try {
        val cli = cliParser.parse(cliOptions, args)

        val balanceSheet = financeGateway.balanceSheet(
            requireNotNull(cli.getOptionValue("ticker")) { "Ticker can not be empty!" },
            cli.getOptionValue("exchange")
        )

        println(CompanyFormatter().format(balanceSheet))

        ReturnOnEquityCalculator().calculate(balanceSheet).run {
            println(ReturnOnEquityFormatter().format(this))
        }

        ReturnOnTotalCapitalCalculator().calculate(balanceSheet).run {
            println(ReturnOnTotalCapitalFormatter().format(this))
        }
    } catch (ex: Exception) {
        ex.printStackTrace(System.err)
        HelpFormatter().printHelp("FinanceCalculator", cliOptions)
    }
}

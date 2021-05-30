package hu.finance

import com.google.common.util.concurrent.RateLimiter
import hu.finance.api.FinanceGateway
import hu.finance.api.YahooFinanceAPI
import hu.finance.formatter.CompanyFormatter
import hu.finance.formatter.ReturnOnEquityFormatter
import hu.finance.voter.ReturnOnEquityVoter
import org.apache.commons.cli.*

private val cliParser: CommandLineParser = DefaultParser()
private val cliOptions = Options().apply {
    addOption(Option.builder("ticker").desc("Company ticker.").required().hasArg().build())
    addOption(Option.builder("exchange").desc("Exchange to use.").required().hasArg().build())
}
private val financeGateway: FinanceGateway = YahooFinanceAPI(RateLimiter.create(4.0))

fun main(args: Array<String>) {
    try {
        val cli = cliParser.parse(cliOptions, args)

        val balanceSheet = financeGateway.balanceSheet(
            requireNotNull(cli.getOptionValue("ticker")) { "Ticker can not be empty!" },
            requireNotNull(cli.getOptionValue("exchange")) { "Exchange can not be empty!" }
        )

        CompanyFormatter().print(balanceSheet)
        ReturnOnEquityVoter(ReturnOnEquityFormatter()).vote(balanceSheet)
    } catch (ex: ParseException) {
        ex.printStackTrace(System.err)
        HelpFormatter().printHelp("FinanceCalculator", cliOptions)
    }
}

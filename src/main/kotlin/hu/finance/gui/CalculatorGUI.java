package hu.finance.gui;

import com.google.common.util.concurrent.RateLimiter;
import hu.finance.api.YahooApi;
import hu.finance.service.FinanceService;
import hu.finance.service.GuiUpdaterService;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;

@SuppressWarnings("UnstableApiUsage")
public class CalculatorGUI extends JFrame {
    private final GuiUpdaterService guiUpdaterService;

    private JPanel mainPanel;
    private JLabel quoteLabel;
    private JLabel exchangeLabel;
    private JLabel openPriceLabel;
    private JLabel previousOpenPriceLabel;
    private JLabel currencyLabel;
    private JTable roeTable;
    private JTable rotcTable;
    private JTable epsTable;
    private JTable dteTable;
    private JTable fcfTable;
    private JLabel quoteShortLabel;
    private JLabel quoteTypeLabel;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem loadQuote;
    private JMenuItem help;

    public CalculatorGUI(String title) {
        super(title);
        guiUpdaterService = new GuiUpdaterService(
                this,
                new FinanceService(
                        Executors.newFixedThreadPool(4),
                        new YahooApi(RateLimiter.create(4))
                )
        );

        help = new JMenuItem("Help");
        loadQuote = new JMenuItem("Load Quote");
        guiUpdaterService.addLoadQuote(loadQuote);

        menu = new JMenu("Menu");
        menu.add(loadQuote);
        menu.add(help);

        menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);

        setContentPane(mainPanel);
        setPreferredSize(new Dimension(600, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public JLabel getQuoteTypeLabel() {
        return quoteTypeLabel;
    }

    public JLabel getQuoteShortLabel() {
        return quoteShortLabel;
    }

    public JTable getFcfTable() {
        return fcfTable;
    }

    public JTable getDteTable() {
        return dteTable;
    }

    public JTable getEpsTable() {
        return epsTable;
    }

    public JLabel getQuoteLabel() {
        return quoteLabel;
    }

    public JLabel getExchangeLabel() {
        return exchangeLabel;
    }

    public JLabel getOpenPriceLabel() {
        return openPriceLabel;
    }

    public JLabel getPreviousOpenPriceLabel() {
        return previousOpenPriceLabel;
    }

    public JLabel getCurrencyLabel() {
        return currencyLabel;
    }

    public JTable getRoeTable() {
        return roeTable;
    }

    public JTable getRotcTable() {
        return rotcTable;
    }
}

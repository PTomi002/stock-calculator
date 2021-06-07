package hu.finance.gui;

import com.google.common.util.concurrent.RateLimiter;
import hu.finance.api.YahooApi;
import hu.finance.gui.util.AutoCloseableLock;
import hu.finance.service.FinanceService;
import hu.finance.service.GuiUpdaterService;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("UnstableApiUsage")
public class CalculatorGUI extends JFrame {
    private final GuiUpdaterService guiUpdaterService;

    private JPanel mainPanel;
    private JPanel quotePanel;
    private JPanel calculationsPanel;
    private JLabel mainTitle;
    private JLabel quoteLabel;
    private JLabel exchangeLabel;
    private JLabel openPriceLabel;
    private JLabel previousOpenPriceLabel;
    private JLabel currencyLabel;
    private JTextPane roeInfo;
    private JTable roeTable;
    private JTable rotcTable;
    private JTable epsTable;
    private JTable dteTable;
    private JTable fcfTable;
    private JLabel quoteShortLabel;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem loadQuote;
    private JMenuItem help;

    public CalculatorGUI(String title) {
        super(title);
        this.guiUpdaterService = new GuiUpdaterService(
                this,
                new FinanceService(
                        Executors.newFixedThreadPool(4),
                        new YahooApi(RateLimiter.create(4))
                ),
                new AutoCloseableLock(
                        new ReentrantLock()
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

    public JMenuItem getHelp() {
        return help;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JPanel getQuotePanel() {
        return quotePanel;
    }

    public JPanel getCalculationsPanel() {
        return calculationsPanel;
    }

    public JLabel getMainTitle() {
        return mainTitle;
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

    public JTextPane getRoeInfo() {
        return roeInfo;
    }

    public JTable getRoeTable() {
        return roeTable;
    }

    public JTable getRotcTable() {
        return rotcTable;
    }

    public JMenu getMenu() {
        return menu;
    }

    public JMenuItem getLoadQuote() {
        return loadQuote;
    }
}

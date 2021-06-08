package hu.finance.gui;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("UnstableApiUsage")
public class CalculatorGUI extends JFrame {
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
    private JLabel peLabel;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem loadQuote;
    private JMenuItem help;

    public CalculatorGUI(String title) {
        super(title);

        buildGUI();

        setContentPane(mainPanel);
        setPreferredSize(new Dimension(600, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }

    private void buildGUI() {
        help = new JMenuItem("Help");
        loadQuote = new JMenuItem("Load");

        menu = new JMenu("Menu");
        menu.add(loadQuote);
        menu.add(help);

        menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    public JLabel getPeLabel() {
        return peLabel;
    }

    public JMenuItem getLoadQuote() {
        return loadQuote;
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

package hu.finance.gui;

import hu.finance.calculator.ReturnOnEquityCalculator;
import hu.finance.calculator.ReturnOnEquityCalculator.ReturnOnEquity;
import hu.finance.gui.util.AutoCloseableLock;
import hu.finance.gui.util.CalcWorker;
import hu.finance.model.Quote;
import hu.finance.service.CompositeQuote;
import hu.finance.service.Finances;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class CalculatorGUI extends JFrame {
    private final AutoCloseableLock lock;
    private final Finances finances;

    private volatile CompositeQuote cqCache;

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
    private JTable roeTable2;
    private JMenuBar menuBar;
    private JMenu quoteMenu;
    private JMenuItem loadQuote;

    public CalculatorGUI(String title, AutoCloseableLock lock, Finances finances) {
        super(title);
        this.lock = lock;
        this.finances = finances;

        loadQuote = new JMenuItem("Load Quote");
        loadQuote.addActionListener(this::actionPerformed);

        quoteMenu = new JMenu("Quote Operations");
        quoteMenu.add(loadQuote);

        menuBar = new JMenuBar();
        menuBar.add(quoteMenu);
        setJMenuBar(menuBar);

        setContentPane(mainPanel);
        setPreferredSize(new Dimension(600, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void updateQuotePanel(Quote quote) {
        Objects.requireNonNull(quote);

        quoteLabel.setText(quote.getQuoteSummary().getName());
        exchangeLabel.setText(quote.getQuoteSummary().getExchange());
        openPriceLabel.setText(quote.getShareSummary().getOpen().toString());
        previousOpenPriceLabel.setText(quote.getShareSummary().getPreviousClose().toString());
        currencyLabel.setText(quote.getShareSummary().getCurrency().toString());
    }

    private void updateRoeTable(Quote quote) {
        Objects.requireNonNull(quote);

        List<ReturnOnEquity> roeList = new ReturnOnEquityCalculator().calculate(quote);
        Object[][] table = new Object[roeList.size()][2];
        for (int i = 0; i < roeList.size(); i++) {
            table[i][0] = ZonedDateTime.ofInstant(roeList.get(i).getDate(), ZoneOffset.UTC).getYear();
            table[i][1] = roeList.get(i).getRoe() + " %";
        }
//        DefaultTableModel jTable = new DefaultTableModel(table, new Object[]{"Év", "ROE"});
//        roeTable.setModel(jTable);
//        roeTable2.setModel(jTable);
    }

    private void loadQuote() {
//        String ticker = JOptionPane.showInputDialog("Company ticker?");
//        if (ticker == null || ticker.isEmpty()) return;
//
//        new CalcWorker<CompositeQuote, Void>(
//                () -> finances.loadQuote(ticker),
//                workerResult -> {
//                    if (!workerResult.getResult()) {
//                        JOptionPane.showMessageDialog(
//                                null,
//                                "Error happened: " + Objects.requireNonNull(workerResult.getError()).getMessage()
//                        );
//                        workerResult.getError().printStackTrace();
//                    } else {
//                        lock.withLock(() -> {
//                            cqCache = Objects.requireNonNull(workerResult.getData());
//                            return null;
//                        });
//                        updateQuotePanel(cqCache.getQuote());
//                        updateRoeTable(cqCache.getQuote());
//                    }
//                    return null;
//                }
//        ).execute();
    }

    private void actionPerformed(ActionEvent action) {
        loadQuote();
    }
}

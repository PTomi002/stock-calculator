package hu.finance.gui;

import hu.finance.gui.util.AutoCloseableLock;
import hu.finance.gui.util.CalcWorker;
import hu.finance.service.CompositeQuote;
import hu.finance.service.Finances;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class CalculatorGUI extends JFrame {
    private final AutoCloseableLock lock;
    private final Finances finances;

    private volatile CompositeQuote cqCache;

    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JMenu quoteMenu;
    private JMenuItem loadQuote;

    public CalculatorGUI(String title, AutoCloseableLock lock, Finances finances) {
        super(title);
        this.lock = lock;
        this.finances = finances;

        loadQuote = new JMenuItem("Load Quote");
        loadQuote.addActionListener(action -> loadQuote());

        quoteMenu = new JMenu("Quote Operations");
        quoteMenu.add(loadQuote);

        menuBar = new JMenuBar();
        menuBar.add(quoteMenu);
        setJMenuBar(menuBar);

        setContentPane(mainPanel);
        setPreferredSize(new Dimension(600, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void loadQuote() {
        String ticker = JOptionPane.showInputDialog("Company ticker?");
        if (ticker == null || ticker.isEmpty()) return;

        new CalcWorker<CompositeQuote, Void>(
                () -> finances.loadQuote(ticker),
                workerResult -> {
                    if (!workerResult.getResult()) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Error happened: " + Objects.requireNonNull(workerResult.getError()).getMessage()
                        );
                        workerResult.getError().printStackTrace();
                    } else {
                        lock.withLock(() -> {
                            cqCache = Objects.requireNonNull(workerResult.getData());
                            return null;
                        });
                    }
                    return null;
                }
        ).execute();
    }

}

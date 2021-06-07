package hu.finance.gui;

import javax.swing.*;
import java.awt.*;

public class ChartsGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel pricePanel;

    public ChartsGUI(String title) {
        super(title);

        setContentPane(mainPanel);
        setPreferredSize(new Dimension(600, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }

    public JPanel getPricePanel() {
        return pricePanel;
    }
}

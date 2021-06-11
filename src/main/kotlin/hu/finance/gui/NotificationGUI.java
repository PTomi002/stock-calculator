package hu.finance.gui;

import javax.swing.*;
import java.awt.*;

public class NotificationGUI extends JFrame {
    public static final String MESSAGE = "Részvény vétel lehetőség: %s, átlagos ár: %s, jelenlegi ár: %s";

    private JPanel mainPanel;
    private JPanel notificationPanel;
    private final String company;
    private final Double avgOpenPrice;
    private final Double nowPrice;

    public NotificationGUI(String title, String company, Double avgOpenPrice, Double nowPrice) {
        super(title);
        this.company = company;
        this.avgOpenPrice = avgOpenPrice;
        this.nowPrice = nowPrice;

        buildGUI(company, avgOpenPrice, nowPrice);

        setContentPane(mainPanel);
        setPreferredSize(new Dimension(600, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        pack();
    }

    private void buildGUI(String company, Double avg, Double open) {
        notificationPanel.add(new JLabel(String.format(MESSAGE, company, avg, open)));
    }
}

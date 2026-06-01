package com.trackit.controller;

import com.trackit.service.FinancialAdviceService;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Optional;

/**
 * Coordinates AI financial advice and presents it in a Swing dialog.
 */
public class FinancialAdviceController {

    private static final String DIALOG_TITLE = "Financial advice";

    private final FinancialAdviceService financialAdviceService;

    public FinancialAdviceController(FinancialAdviceService financialAdviceService) {
        this.financialAdviceService = financialAdviceService;
    }

    /**
     * Shows a popup with AI advice when the in-app monthly budget is exceeded; otherwise an informational dialog.
     *
     * @param parent parent component for dialog placement (may be {@code null})
     * @param month  month 1-12
     * @param year   four-digit year
     */
    public void showAdviceDialogIfLimitExceeded(Component parent, int month, int year) {
        Optional<String> advice = financialAdviceService.getAdviceWhenLimitExceeded(month, year);
        if (advice.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "There is no advice to show. Either you are within your monthly budget for this month, "
                            + "no budget is saved for this month/year, or there is no spending recorded.",
                    DIALOG_TITLE,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String text = advice.get();
        System.out.println(text);

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        int lineCount = (int) text.lines().count();
        area.setRows(Math.min(12, Math.max(4, lineCount + 2)));
        area.setColumns(42);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 220));

        JOptionPane.showMessageDialog(parent, scroll, DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Returns advice or error text without showing a dialog.
     */
    public Optional<String> getAdviceWhenLimitExceeded(int month, int year) {
        return financialAdviceService.getAdviceWhenLimitExceeded(month, year);
    }
}

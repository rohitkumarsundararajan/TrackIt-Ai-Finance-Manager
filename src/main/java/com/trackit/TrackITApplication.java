package com.trackit;

import com.trackit.ui.frame.HomeFrame;

import javax.swing.SwingUtilities;

/**
 * Entry point for the TrackIT desktop application.
 * <p>
 * This class is responsible only for bootstrapping the Swing user interface
 * in a thread-safe manner and delegating to the main application frame.
 * </p>
 */
public final class TrackITApplication {

    private TrackITApplication() {
        // Utility class; prevent instantiation.
    }

    /**
     * Application main method.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HomeFrame homeFrame = new HomeFrame();
            homeFrame.setVisible(true);
        });
    }
}



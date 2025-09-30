package com.arcade.arkanoid;

import javax.swing.SwingUtilities;

public final class ArcadeLauncher {
    private ArcadeLauncher() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArkanoidGame game = new ArkanoidGame();
            game.start();
        });
    }
}

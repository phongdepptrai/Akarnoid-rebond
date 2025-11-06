package com.arcade.arkanoid.gameplay.cosmetics;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public final class SkinCatalog {

    public static final class PaddleSkin {
        private final Color fillColor;
        private final Color borderColor;

        public PaddleSkin(Color fillColor, Color borderColor) {
            this.fillColor = fillColor;
            this.borderColor = borderColor;
        }

        public Color fillColor() {
            return fillColor;
        }

        public Color borderColor() {
            return borderColor;
        }
    }

    public static final class BallSkin {
        private final Color fillColor;
        private final Color borderColor;

        public BallSkin(Color fillColor, Color borderColor) {
            this.fillColor = fillColor;
            this.borderColor = borderColor;
        }

        public Color fillColor() {
            return fillColor;
        }

        public Color borderColor() {
            return borderColor;
        }
    }

    private static final Map<String, PaddleSkin> PADDLE_SKINS = new HashMap<>();
    private static final Map<String, BallSkin> BALL_SKINS = new HashMap<>();

    static {
        PADDLE_SKINS.put("classic", new PaddleSkin(new Color(0x00BCD4), new Color(0x00849C)));
        PADDLE_SKINS.put("neon-stream", new PaddleSkin(new Color(0x7C4DFF), new Color(0x311B92)));
        PADDLE_SKINS.put("retro-grid", new PaddleSkin(new Color(0x1DE9B6), new Color(0x00695C)));
        PADDLE_SKINS.put("sunset-glow", new PaddleSkin(new Color(0xFF7043), new Color(0xBF360C)));
        PADDLE_SKINS.put("frost-byte", new PaddleSkin(new Color(0x4DD0E1), new Color(0x006064)));

        BALL_SKINS.put("classic", new BallSkin(Color.WHITE, new Color(0xC5CAE9)));
        BALL_SKINS.put("ion-burst", new BallSkin(new Color(0xFF5722), new Color(0xBF360C)));
        BALL_SKINS.put("supernova", new BallSkin(new Color(0xFFC107), new Color(0xFF6F00)));
        BALL_SKINS.put("aurora", new BallSkin(new Color(0x81D4FA), new Color(0x0277BD)));
        BALL_SKINS.put("quantum-core", new BallSkin(new Color(0xE1BEE7), new Color(0x6A1B9A)));
    }

    private SkinCatalog() {
    }

    public static PaddleSkin paddleSkin(String id) {
        PaddleSkin skin = PADDLE_SKINS.get(id);
        return skin != null ? skin : PADDLE_SKINS.get("classic");
    }

    public static BallSkin ballSkin(String id) {
        BallSkin skin = BALL_SKINS.get(id);
        return skin != null ? skin : BALL_SKINS.get("classic");
    }
}

package secur3dit.filters;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.*;

public class Filters {

    public static void grayscale(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {
                Color color = new Color(img.getRGB(i, j));

                int tr = (int) (color.getRed() * 0.2126);
                int tg = (int) (color.getGreen() * 0.7152);
                int tb = (int) (color.getBlue() * 0.0722);
                int sum = tr + tg + tb;

                Color shadeOfGray = new Color(sum, sum, sum);
                img.setRGB(i, j, shadeOfGray.getRGB());
            }
        }
    }

    public static void sepia(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {
                Color color = new Color(img.getRGB(i, j));

                int tr = (int) (color.getRed() * 0.393 + color.getGreen() * 0.769 + color.getBlue() * 0.189);
                int tg = (int) (color.getRed() * 0.349 + color.getGreen() * 0.686 + color.getBlue() * 0.168);
                int tb = (int) (color.getRed() * 0.272 + color.getGreen() * 0.534 + color.getBlue() * 0.131);

                int red = tr > 255 ? 255 : tr;
                int green = tg > 255 ? 255 : tg;
                int blue = tb > 255 ? 255 : tb;

                Color newColor = new Color(red, green, blue);
                img.setRGB(i, j, newColor.getRGB());
            }
        }

    }

    public static void negative(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {
                Color color = new Color(img.getRGB(i, j));

                int tr = (int) (255 - color.getRed());
                int tg = (int) (255 - color.getGreen());
                int tb = (int) (255 - color.getBlue());

                Color newColor = new Color(tr, tg, tb);
                img.setRGB(i, j, newColor.getRGB());
            }
        }

    }

    public static void addWatermark(BufferedImage img, String watermarkText) {
        Graphics2D graphics = (Graphics2D) img.getGraphics();

        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);

        graphics.setComposite(alphaChannel);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.BOLD, 64));
        
        FontMetrics fontMetrics = graphics.getFontMetrics();
        Rectangle2D rectangle = fontMetrics.getStringBounds(watermarkText, graphics);

        int centerX = (img.getWidth() - (int) rectangle.getWidth()) / 2;
        int centerY = img.getHeight() / 2;

        graphics.drawString(watermarkText, centerX, centerY);
        graphics.dispose();
    }
}

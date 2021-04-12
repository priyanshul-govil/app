/**
 * Filters.java 
 * Contains various filters that would be used by the main desktop application.
 */

package secur3dit.filters;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Filters {
    public static BufferedImage mirror(BufferedImage image, boolean vertical) {
        BufferedImage result = Helpers.deepCopy(image);
        int height = result.getHeight();
        int width  = result.getWidth();
    
        if (vertical) {
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width / 2; ++j) {
                    int temp = result.getRGB(j, i);
                    result.setRGB(j, i, result.getRGB(width - j - 1, i));
                    result.setRGB(width - j - 1, i, temp);
                }
            }
        }
        else {
            for (int j = 0; j < width; ++j) {
                for (int i = 0; i < height / 2; ++i) {
                    int temp = result.getRGB(j, i);
                    result.setRGB(j, i, result.getRGB(j, height - i - 1));
                    result.setRGB(j, height - i - 1, temp);
                }
            }
        }
        return result;
    }

    /**
     * @param angle: floating-point degree, anti-clockwise
     * Produces a clipped image rotated by angle degrees,
     * by treating the image as a rectangle on a cartesian plane
     * and using bilinear interpolation to find the pixel value at every co-ordinate that 
     * will not have a black color after rotation. For more information about the math involved, 
     * visit [https://en.wikipedia.org/wiki/Bilinear_interpolation]
     */
    public static BufferedImage rotate(BufferedImage image, double angle) {
        int width = image.getWidth();
        int height = image.getHeight();
        int imageType = BufferedImage.TYPE_INT_RGB;

        if (image.isAlphaPremultiplied()) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }

        BufferedImage result = new BufferedImage(width, height, imageType);

        angle = Helpers.toRadians(angle);
        int centreX = width / 2;
        int centreY = height / 2;

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                result.setRGB(j, i, Color.black.getRGB());
                int[] points = Helpers.toCartesian(j, i, centreX, centreY);
                if (points[0] == 0 && points[1] == 0) {
                    continue;
                }

                double[] polarData = Helpers.toPolar(points[0], points[1]);
                polarData[1] -= angle;
                double[] cartesianPoints = Helpers.toCartesian(polarData);
                double[] rasterPoints = Helpers.toRaster(cartesianPoints, centreX, centreY);
                int[] floored = Helpers.floorPoints(rasterPoints);
                int[] ceiled = Helpers.ceilPoints(rasterPoints);

                if (Helpers.outOfBounds(floored[0], width) || 
                    Helpers.outOfBounds(floored[1], height) || 
                    Helpers.outOfBounds(ceiled[0], width) || 
                    Helpers.outOfBounds(ceiled[1], height)) {
                        continue;
                }
                
                double deltaX = rasterPoints[0] - (double)floored[0];
                double deltaY = rasterPoints[1] - (double)floored[1];
                Color topLeft = new Color(image.getRGB(floored[0], floored[1]));
                Color topRight = new Color(image.getRGB(ceiled[0], floored[1]));
                Color bottomLeft = new Color(image.getRGB(floored[0], ceiled[1]));
                Color bottomRight = new Color(image.getRGB(ceiled[0], ceiled[1]));
                int finalColor = Helpers.bilinearInterpolation(topLeft, topRight, bottomLeft, 
                    bottomRight, deltaX, deltaY);
                result.setRGB(j, i, finalColor);
            }
        }
        return result;
    }

    /**
     * For the math, check [https://en.wikipedia.org/wiki/Sobel_operator]
     */
    public static BufferedImage detectEdges(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int imageType = BufferedImage.TYPE_INT_RGB;

        if (image.isAlphaPremultiplied()) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }

        BufferedImage result = new BufferedImage(width, height, imageType);

        int[] sobelSumX = new int[3];
        int[] sobelSumY = new int[3];
        int[] sobelXY = new int[3];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                for (int k = 0; k < 3; ++k) {
                    sobelSumX[k] = 0;
                    sobelSumY[k] = 0;
                }
                
                for (int m = 0; m < 3; ++m) {
                    for (int n = 0; n < 3; ++n) {
                        int indexI = i + m - 1;
                        int indexJ = j + n - 1;

                        if (Helpers.outOfBounds(indexI, height) || 
                            Helpers.outOfBounds(indexJ, width)) {
                                continue;
                        }

                        Color color = new Color(image.getRGB(indexJ, indexI));

                        sobelSumX[0] += Helpers.sobelKernelX[m][n] * color.getRed();
                        sobelSumX[1] += Helpers.sobelKernelX[m][n] * color.getGreen();
                        sobelSumX[2] += Helpers.sobelKernelX[m][n] * color.getBlue();
                        sobelSumY[0] += Helpers.sobelKernelX[m][n] * color.getRed();
                        sobelSumY[1] += Helpers.sobelKernelX[m][n] * color.getGreen();
                        sobelSumY[2] += Helpers.sobelKernelX[m][n] * color.getBlue();
                    }
                }

                for (int k = 0; k < 3; ++k) {
                    sobelXY[k] = (int)Math.round(Math.sqrt((sobelSumX[k] * sobelSumX[k] + 
                                                            sobelSumY[k] * sobelSumY[k])));
                    sobelXY[k] = Helpers.truncateIfNeeded(sobelXY[k]);
                }

                Color color = new Color(sobelXY[0], sobelXY[1], sobelXY[2]);
                result.setRGB(j, i, color.getRGB());
            }
        }
        return result;
    }

    public static BufferedImage brighten(BufferedImage image, double dial) {
        if (dial < 0.0) {
            return null;
        }
        BufferedImage result = Helpers.deepCopy(image);
        Helpers.lightDial(result, dial);
        return result;
    }

    public static BufferedImage darken(BufferedImage image, double dial) {
        if (dial > 0.0) {
            return null;
        }
        BufferedImage result = Helpers.deepCopy(image);
        Helpers.lightDial(result, dial);
        return result;   
    }

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
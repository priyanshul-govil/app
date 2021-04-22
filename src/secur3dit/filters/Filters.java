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
        int width = result.getWidth();

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

                double deltaX = rasterPoints[0] - (double) floored[0];
                double deltaY = rasterPoints[1] - (double) floored[1];
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
                    sobelXY[k] = (int) Math.round(Math.sqrt((sobelSumX[k] * sobelSumX[k] + 
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

    public static BufferedImage grayscale(BufferedImage image) {
        BufferedImage img = Helpers.deepCopy(image);

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
        return img;
    }

    public static BufferedImage sepia(BufferedImage image) {
        BufferedImage img = Helpers.deepCopy(image);

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
        return img;

    }

    public static BufferedImage negative(BufferedImage image) {
        BufferedImage img = Helpers.deepCopy(image);

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
        return img;

    }

    public static BufferedImage addWatermark(BufferedImage image, String watermarkText) {
        
        BufferedImage img = Helpers.deepCopy(image);
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

        return img;
    }

    static void boxBlur(BufferedImage source, BufferedImage target, int kernelRadius) {
        int width = source.getWidth();
        int height = source.getHeight();

        double kernelCoefficient = 1.0 / ((2.0 * (double)kernelRadius) + 1.0);
        // go through each row first, on the source itself
        for (int i = 0; i < height; ++i) {
            
            Color fcolor = new Color(target.getRGB(0, i));
            Color lastColor = new Color(target.getRGB(width - 1, i));
            int sumR =  fcolor.getRed() * (kernelRadius + 1);
            int sumG = fcolor.getGreen() * (kernelRadius + 1);
            int sumB = fcolor.getBlue() * (kernelRadius + 1);

            for (int j = 0; j < kernelRadius; ++j) {
                Color color = new Color(target.getRGB(j, i));
                sumR += color.getRed();
                sumG += color.getGreen();
                sumB += color.getBlue();
            }            
            int ti = 0;
            int li = ti;
            int ri = ti + kernelRadius;
            for (int j = 0; j <= kernelRadius; ++j) {
                Color color = new Color(target.getRGB(ri, i));
                sumR += (color.getRed() - fcolor.getRed());
                sumG += (color.getGreen() - fcolor.getGreen());
                sumB += (color.getBlue()- fcolor.getBlue());
                ++ri;
                int finalR = (int)Math.round((double)sumR * kernelCoefficient);
                int finalG = (int)Math.round((double)sumG * kernelCoefficient);
                int finalB = (int)Math.round((double)sumB * kernelCoefficient);
                Color finalColor = new Color(finalR, finalG, finalB);
                source.setRGB(ti, i, finalColor.getRGB());
                ++ti;
            }

            for (int j = kernelRadius + 1; j < width - kernelRadius; ++j) {
                Color color = new Color(target.getRGB(ri, i));
                Color lcolor = new Color(target.getRGB(li, i));
                ++li;
                sumR += (color.getRed() - lcolor.getRed());
                sumG += (color.getGreen() - lcolor.getGreen());
                sumB += (color.getBlue()- lcolor.getBlue());
                ++ri;
                int finalR = (int)Math.round((double)sumR * kernelCoefficient);
                int finalG = (int)Math.round((double)sumG * kernelCoefficient);
                int finalB = (int)Math.round((double)sumB * kernelCoefficient);
                Color finalColor = new Color(finalR, finalG, finalB);
                source.setRGB(ti, i, finalColor.getRGB());
                ++ti;
            }

            for (int j = width - kernelRadius; j < width; ++j) {
                Color lcolor = new Color(target.getRGB(li, i));
                ++li;
                sumR += (lastColor.getRed() - lcolor.getRed());
                sumG += (lastColor.getGreen() - lcolor.getGreen());
                sumB += (lastColor.getBlue()- lcolor.getBlue());
                
                int finalR = (int)Math.round((double)sumR * kernelCoefficient);
                int finalG = (int)Math.round((double)sumG * kernelCoefficient);
                int finalB = (int)Math.round((double)sumB * kernelCoefficient);
                Color finalColor = new Color(finalR, finalG, finalB);
                source.setRGB(ti, i, finalColor.getRGB());
                ++ti;
            }
        }

        for (int i = 0; i < width; ++i) {
            Color fcolor = new Color(source.getRGB(i, 0));
            Color lastColor = new Color(source.getRGB(i, height - 1));
            int sumR =  fcolor.getRed() * (kernelRadius + 1);
            int sumG = fcolor.getGreen() * (kernelRadius + 1);
            int sumB = fcolor.getBlue() * (kernelRadius + 1);

            for (int j = 0; j < kernelRadius; ++j) {
                Color color = new Color(source.getRGB(i, j));
                sumR += color.getRed();
                sumG += color.getGreen();
                sumB += color.getBlue();
            }            
            int ti = 0;
            int li = ti;
            int ri = ti + kernelRadius;
            for (int j = 0; j <= kernelRadius; ++j) {
                Color color = new Color(source.getRGB(i, ri));
                sumR += (color.getRed() - fcolor.getRed());
                sumG += (color.getGreen() - fcolor.getGreen());
                sumB += (color.getBlue()- fcolor.getBlue());
                ++ri;
                int finalR = (int)Math.round((double)sumR * kernelCoefficient);
                int finalG = (int)Math.round((double)sumG * kernelCoefficient);
                int finalB = (int)Math.round((double)sumB * kernelCoefficient);
                Color finalColor = new Color(finalR, finalG, finalB);
                target.setRGB(i, ti, finalColor.getRGB());
                ++ti;
            }

            for (int j = kernelRadius + 1; j < height - kernelRadius; ++j) {
                Color color = new Color(source.getRGB(i, ri));
                Color lcolor = new Color(source.getRGB(i, li));
                ++li;
                sumR += (color.getRed() - lcolor.getRed());
                sumG += (color.getGreen() - lcolor.getGreen());
                sumB += (color.getBlue()- lcolor.getBlue());
                ++ri;
                int finalR = (int)Math.round((double)sumR * kernelCoefficient);
                int finalG = (int)Math.round((double)sumG * kernelCoefficient);
                int finalB = (int)Math.round((double)sumB * kernelCoefficient);
                Color finalColor = new Color(finalR, finalG, finalB);
                target.setRGB(i, ti, finalColor.getRGB());
                ++ti;
            }

            for (int j = height - kernelRadius; j < height; ++j) {
                Color lcolor = new Color(source.getRGB(i, li));
                ++li;
                sumR += (lastColor.getRed() - lcolor.getRed());
                sumG += (lastColor.getGreen() - lcolor.getGreen());
                sumB += (lastColor.getBlue()- lcolor.getBlue());
                
                int finalR = (int)Math.round((double)sumR * kernelCoefficient);
                int finalG = (int)Math.round((double)sumG * kernelCoefficient);
                int finalB = (int)Math.round((double)sumB * kernelCoefficient);
                Color finalColor = new Color(finalR, finalG, finalB);
                target.setRGB(i, ti, finalColor.getRGB());
                ++ti;
            }
        }
    }

    public static BufferedImage gaussianBlur(BufferedImage image, int intensity) {
        BufferedImage source = Helpers.deepCopy(image);
        BufferedImage target = Helpers.deepCopy(image);
        boxBlur(source, target, intensity);
        boxBlur(target, source, intensity + 1);
        boxBlur(source, target, intensity + 1);
        return target;
    }

    /**
     * Takes an image and returns a posterized version of it. 
     * Posterization achieves this by reducing the distinct pixels in 
     * an image. We apply the Helpers.reducePixel() method on R,G,B 
     * value of every pixel. 
     */
    public static BufferedImage posterize(BufferedImage image) {
        
        BufferedImage result = Helpers.deepCopy(image);
        int width = image.getWidth();
        int height = image.getHeight();
    
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                // get the color
                Color originalColor = new Color(result.getRGB(j, i));

                // reduce the pixels
                int red = Helpers.reducePixel(originalColor.getRed());
                int green = Helpers.reducePixel(originalColor.getGreen());
                int blue = Helpers.reducePixel(originalColor.getBlue());

                // apply the color
                Color finalColor = new Color(red, green, blue);
                result.setRGB(j, i, finalColor.getRGB());
            }
        }

        return result;
    }
    
    /**
     * Takes an image and returns a pixelated version of it.
     * It needs an extra parameter, pixelWidth which can take values
     * in the range [1, min(height - 1, width - 1)]. A square window of side
     * length pixelWidth slides through the image, jumping at a length of 
     * pixelWidth after each iteration. In each slide, it sets the R,G,B values 
     * of every pixel to the average values in that window.
     */
    public static BufferedImage pixelate(BufferedImage image, int pixelWidth) {
        
        BufferedImage result = Helpers.deepCopy(image);
        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = 0; i < height; i += pixelWidth) {
            for (int j = 0; j < width; j += pixelWidth) {

                int avgRed = 0;
                int avgGreen = 0;
                int avgBlue = 0;

                int totalRed = 0;
                int totalGreen = 0;
                int totalBlue = 0;

                int count = 0;

                // Traverse and add the pixel values
                for (int y = i; y < i + pixelWidth && y < height; ++y) {
                    for (int x = j; x < j + pixelWidth && x < width; ++x) {
                        Color color = new Color(result.getRGB(x, y));

                        totalRed += color.getRed();
                        totalGreen += color.getGreen();
                        totalBlue += color.getBlue();
                        ++count;
                    }
                }

                // Compute the average
                avgRed = totalRed / count;
                avgGreen = totalGreen / count;
                avgBlue = totalBlue / count;
                
                // Get the color made using the R,G,B values
                Color finalColor = new Color(avgRed, avgGreen, avgBlue);

                // Set all pixels in the submatrix to finalColor
                for (int y = i; y < i + pixelWidth && y < height; ++y) {
                    for (int x = j; x < j + pixelWidth && x < width; ++x) {
                        result.setRGB(x, y, finalColor.getRGB());
                    }
                }
            }
        }
        
        return result;
    }
}
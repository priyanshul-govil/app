/**
 * Filters.java 
 * Contains various filters that would be used by the main desktop application.
 */

package secur3dit.filters;

import java.awt.Color;
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
};
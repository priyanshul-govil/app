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
};
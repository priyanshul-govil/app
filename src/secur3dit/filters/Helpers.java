/*
 * Helpers.java 
 * Contains utility methods used by Filter.java
 */

package secur3dit.filters;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class Helpers {
    static BufferedImage deepCopy(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    static double toRadians(double degreeAngle) {
        double result = (degreeAngle * Math.PI) / 180.0;
        return result;
    }

    static int[] toCartesian(int rasterX, int rasterY, int centreX, int centreY) {
        int points[] = new int[2];
        points[0] = rasterX - centreX;
        points[1] = centreY - rasterY;
        return points;
    }

    static double[] toPolar(int cartesianX, int cartesianY) {
        double data[] = new double[2];
        data[0] = Math.sqrt((cartesianX * cartesianX) + (cartesianY * cartesianY));
        if (cartesianX == 0) {
            if (cartesianY < 0) {
                data[1] = 1.5 * Math.PI;
            }
            else {
                data[1] = 0.5 * Math.PI;
            }
        }
        else {
            data[1] = Math.atan2((double)cartesianY, (double)cartesianX);
        }
        return data;
    }

    static double[] toCartesian(double[] polarData) {
        double[] points = new double[2];
        points[0] = polarData[0] * Math.cos(polarData[1]);
        points[1] = polarData[0] * Math.sin(polarData[1]);
        return points;
    }

    static double[] toRaster(double[] cartesianPoints, int centreX, int centreY) {
        double[] points = new double[2];
        points[0] = cartesianPoints[0] + (double)centreX;
        points[1] = (double)centreY - cartesianPoints[1];
        return points;
    }

    static int[] floorPoints(double[] rasterPoints) {
        int[] floored = new int[2];
        floored[0] = (int)Math.floor(rasterPoints[0]);
        floored[1] = (int)Math.floor(rasterPoints[1]);
        return floored;
    }

    static int[] ceilPoints(double[] rasterPoints) {
        int[] ceiled = new int[2];
        ceiled[0] = (int)Math.ceil(rasterPoints[0]);
        ceiled[1] = (int)Math.ceil(rasterPoints[1]);
        return ceiled;
    }

    static boolean outOfBounds(int givenIndex, int length) {
        return (givenIndex < 0 || givenIndex >= length);
    }

    static double linearInterpolation(double left, double delta, double right) {
        double result = (1 - delta) * left + (delta) * right;
        return result;
    }

    static int truncateIfNeeded(int pixel) {
        if (pixel < 0) {
            return 0;
        }
        if (pixel > 255) {
            return 255;
        }
        return pixel;
    }

    static int bilinearInterpolation(Color topLeft, Color topRight, 
                                Color bottomLeft, Color bottomRight, 
                                double deltaX, double deltaY) {

        double topR = linearInterpolation(topLeft.getRed(), deltaX, topRight.getRed());
        double topG = linearInterpolation(topLeft.getGreen(), deltaX, topRight.getGreen());
        double topB = linearInterpolation(topLeft.getBlue(), deltaX, topRight.getBlue());

        double bottomR = linearInterpolation(bottomLeft.getRed(), deltaX, bottomRight.getRed());
        double bottomG = linearInterpolation(bottomLeft.getGreen(), deltaX, bottomRight.getGreen());
        double bottomB = linearInterpolation(bottomLeft.getBlue(), deltaX, bottomRight.getBlue());

        int red = (int)Math.round(linearInterpolation(topR, deltaY, bottomR));
        int green = (int)Math.round(linearInterpolation(topG, deltaY, bottomG));
        int blue = (int)Math.round(linearInterpolation(topB, deltaY, bottomB));

        red = truncateIfNeeded(red);
        green = truncateIfNeeded(green);
        blue = truncateIfNeeded(blue);
        Color finalColor = new Color(red, green, blue);
        return finalColor.getRGB();
    }

    static final int[][] sobelKernelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    static final int[][] sobelKernelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
};
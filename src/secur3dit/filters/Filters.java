/*
 * Filters.java 
 * Contains various filters that would be used by the main desktop application.
 */

package secur3dit.filters;

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
};
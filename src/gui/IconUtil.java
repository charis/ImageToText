/*
 * File          : IconUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 25 November 2023
 * Last Modified : 25 November 2023
 */
package gui;

// Import Java SE classes
import java.io.File;
import javax.swing.ImageIcon;
// Import custom classes
import util.UIUtil;
// Import constants
import static constants.Constants.LOAD_IMAGES_FROM_CLASSPATH;
import static gui.Constants.IMAGE_PACKGAGE;
import static gui.Constants.IMAGE_REPOSITORY;

/**
 * Utility class to load image icons.
 */
public class IconUtil
{
    /**
     * Given a filename it returns the corresponding image icon or {@code null}
     * if the image icon is not found.
     * 
     * @param filename The image icon filename
     * 
     * @return the corresponding image icon or {@code null} if the image icon is
     *         not found
     */
    public static ImageIcon getIcon(String filename) {
        if (LOAD_IMAGES_FROM_CLASSPATH) {
            return UIUtil.loadIcon(IMAGE_PACKGAGE + "/" + filename);
        }
        else {
            return UIUtil.getIcon(IMAGE_REPOSITORY + File.separator + filename);
        }
        
    }
}

/*
 * File          : ImagePanel.java
 * Author        : Charis Charitsis
 * Creation Date : 24 November 2023
 * Last Modified : 24 November 2023
 */
package gui;

// Import Java SE classes
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JPanel;

import util.MathUtil;
// Import custom classes
import util.UIUtil;

public class ImagePanel extends JPanel {
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 3006769532505931833L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The size of the image panel */
    private final Dimension     size;
    /** The image to display */
    private       BufferedImage image;
    /** The horizontal offset to place the image in the panel */ 
    private       int           xOffset;
    /** The vertical offset to place the image in the panel */ 
    private       int           yOffset;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Constructs a panel to display an image.
     * 
     * @param size The size of the panel
     */
    public ImagePanel(Dimension size) {
        this.size = size;
        setMaximumSize(size);
    }
    
    // -------------------------------------------------------- //
    //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
    // -------------------------------------------------------- //
    /**
     * Sets the image to display.<br>
     * The image gets auto-scaled to fit the panel.
     * 
     * @param image The image to display
     * 
     * @throws IOException
     */
    public void setImage(BufferedImage image)
            throws IOException {
         this.image = scaleImage(image);
         repaint();
    }
    
    /**
     * Removes the image (if any) from the image panel.
     */
    public void clear() {
       image = null;
       repaint();
    }
     
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, xOffset, yOffset, null);
        }
    }
    
    // ------------------------------------------------------------ //
    //   P   R   I   V   A   T   E      M   E   T   H   O   D   S   //
    // ------------------------------------------------------------ //
    /**
     * Scales the provided image to fit the panel without changing the aspect
     * ratio.
     * 
     * @param image The image to scale
     * 
     * @return the scaled image
     */
    private BufferedImage scaleImage(BufferedImage image) {
        int imageWidth  = image.getWidth();
        int imageHeight = image.getHeight();
        
        double imageAspectRatio = (double)imageWidth / imageHeight;
        double panelAspectRatio = size.getWidth() / size.getHeight();
        
        int scaledWidth;
        int scaledHeight;
        if (imageAspectRatio > panelAspectRatio) {
            scaledWidth  = (int) size.getWidth();
            scaledHeight =
              MathUtil.round(size.getWidth() / imageAspectRatio, 0).intValue();
        }
        else {
            scaledWidth  =
              MathUtil.round(imageAspectRatio * size.getHeight(), 0).intValue();
            scaledHeight = (int) size.getHeight();
        }
        
        xOffset = MathUtil.round(
                           (size.getWidth() - scaledWidth) / 2, 0).intValue();
        yOffset = MathUtil.round(
                           (size.getHeight() - scaledHeight) / 2, 0).intValue();
        
        return UIUtil.resizeImage(image,
                                  scaledWidth,
                                  scaledHeight);
    }
}

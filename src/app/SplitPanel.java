/*
 * File          : SplitPanel.java
 * Author        : Charis Charitsis
 * Creation Date : 16 November 2020
 * Last Modified : 27 November 2023
 */
package app;

// Import Java SE classes
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
// Import custom classes
import gui.ImagePanel;
import gui.ScrollableTextPanel;
import util.MathUtil;
// Import constants
import static app.Constants.BACKGROUND_COLOR;
import static constants.Literals.NEW_LINE;


/**
 * The app's main panel with the image preview and the recognized text.
 */
public class SplitPanel extends JPanel
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -3753695282681760300L;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       C   O   N   S   T   A   N   T   S   //
    // --------------------------------------------------------------------- //
    /** The line number column background color */
    private static final Color  LINE_COLUMN_COLOR  = new Color(200, 210, 240);
    /** The text font for the text panel */
    private static final Font   TEXT_PANEL_FONT    = new Font("Courier New",
                                                              Font.PLAIN,
                                                              12);
    /** Scaling the image height to 75% to fit */
    private static final double IMAGE_HEIGHT_SCALE = 0.75;
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /**
     * The area to display the text that is extracted from the image
     */
    private final ScrollableTextPanel textPanel;
    /**
     * The area to display the image
     */
    private final ImagePanel          imagePanel;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates the app's main panel with the image preview and the recognized
     * text.
     * 
     * @param size The size of the panel
     * @param backgroundColor The background color
     */
    protected SplitPanel(Dimension size,
                         Color     backgroundColor) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(backgroundColor);
        
        // Text Panel
        int textPanelHeight = MathUtil.round(size.getHeight()/ 2.5, 0)
                                      .intValue();
        
        textPanel = new ScrollableTextPanel(TEXT_PANEL_FONT, null);
        textPanel.setEditable(false);
        textPanel.setLineDisplayBackground(LINE_COLUMN_COLOR);
        textPanel.setMaximumSize(new Dimension((int)size.getWidth(),
                                               textPanelHeight));
        add(textPanel);
        
        // Image Panel
        int imagePanelHeight = (int) size.getHeight() - textPanelHeight;
        int imageHeight =
           MathUtil.round(IMAGE_HEIGHT_SCALE * imagePanelHeight, 0).intValue();
        imagePanel = new ImagePanel(new Dimension((int)size.getWidth(),
                                                  imageHeight));
        imagePanel.setBackground(BACKGROUND_COLOR);
        add(imagePanel);
    }
    
    // ------------------------------------------------------------------ //
    //  P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S  //
    // ------------------------------------------------------------------ //
    /**
     * Highlights the text lines in the specified range 
     * 
     * @param startLine The start line, inclusive (gets highlighted)
     * @param endLine The start line, exclusive (does not get highlighted)
     */
    public void highlightText(int startLine, int endLine) {
        textPanel.highlightText(startLine, endLine);
    }
    
    /**
     * Sets the text in the text editor.
     *  
     * @param text The new text to be set
     * @param colorText {@code true} to color the text or {@code false} to use
     *                  black color only
     */
    protected void setText(String  text,
                           boolean colorText) {
        textPanel.setText(text, colorText);
    }
    
    /**
     * @return the text in the text editor
     */
    protected String getText() {
        return textPanel.getText();
    }
    
    
    /**
     * Sets the image in the image panel.
     * 
     * @param imageFile the image to display in the image panel.
     */
    protected void loadImage(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            imagePanel.setImage(image);
        } 
        catch (IOException ioe) {
            System.out.println("Error loading image '" + imageFile.getPath()
                             + "'. Details: " + NEW_LINE + ioe.getMessage());
        }
    	
    }
    
    /**
     * Clears the text (i.e., removes any text) and the image panel (removes
     * any image).
     */
    protected void clear() {
        textPanel.setText(null, false);
        imagePanel.clear();
    }
    
    
    /**
     * Sets the specified boolean flag to indicate whether or not the text area
     * is editable.
     * 
     * @param flag {@code true} to make the text area editable or {@code false}
     *             to make it non-editable
     */
    
    protected void setTextEditorEditable(boolean flag) {
        textPanel.setEditable(flag);
    }
}

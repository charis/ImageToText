/*
 * File          : GUIHelper.java
 * Author        : Charis Charitsis
 * Creation Date : 16 November 2020
 * Last Modified : 28 November 2023
 */
package app;

// Import Java SE classes
import java.io.File;
import java.util.List;
// Import custom classes
import exception.ErrorException;
import util.OCRUtil;
import util.filesystem.DirectoryUtil;
import util.io.FileIOUtil;
import static constants.Constants.IMAGE_EXTENSIONS;
// Import constants
import static constants.Constants.OCR_EXT;
import static constants.Literals.DOT;

/**
 * Collection of helper methods for the GUI.
 */
public class GUIHelper
{
    // -------------------------------------------------------------------- //
    //   P   R   O   T   E   C   T   E   D      M   E   T   H   O   D   S   //
    // -------------------------------------------------------------------- //
    /**
     * Given an image file it returns the file that has the image text in
     * case the image text has been recognized or null in case the image has
     * not been processed.<br>
     * The file with the text exists in the same directory as the image and has
     * the same file name except its extension which is '.ocr'<br>
     * E.g. if the image 'foo.png' has been processed there will be a file
     *     'foo.ocr' in the same directory.
     * 
     * @param imageFile The image file or {@code null} if there exists no file
     *                  with the recognized image text
     * 
     * @return the file with the recognized image text or {@code null} in case
     *         there is no such file
     */
    protected static File getTextFile(File imageFile) {
        String pathname = imageFile.getPath();
        // Replace the extension with .ocr
        int extIndex = pathname.lastIndexOf(DOT);
        if (extIndex != -1) {
            pathname = pathname.substring(0, extIndex) + OCR_EXT;
            File imageTextFile = new File(pathname);
            if (imageTextFile.exists()) {
                return imageTextFile;
            }
        }
        
        return null;
    }
    
    /**
     * Extracts the text from the provided image and it writes it to the
     * associated file (i.e., file in same directory and with the same
     * file name as the image except that it ends with .ocr extension).<br>
     * E.g. the text for image 'foo.png' will be written to a file 'foo.ocr' in
     *      the same directory.
     * 
     * @param imageFile The image file or to create the associated .ocr file
     *                  with the recognized image text
     * 
     * @throws ErrorException in case of error saving the file
     */
    protected static void recognizeText(File imageFile)
              throws ErrorException {
        String imageText = OCRUtil.imageToText(imageFile);
        saveImageText(imageFile, imageText);
    }
    
    /**
     * Saves the text for the provided image to the associated file (i.e., file
     * in the same directory and with the same file name except as the image
     * except that it ends with .ocr extension).<br>
     * E.g. the text for image 'foo.png' will be written to a file 'foo.ocr' in
     *      the same directory.
     * 
     * @param imageFile The image file or to determine the associated .ocr file
     *                  to save the text
     * @param imageText The text that is saved in the .ocr file
     * 
     * @throws ErrorException in case of error saving the file
     */
    protected static void saveImageText(File   imageFile,
                                        String imageText) 
               throws ErrorException {
        String pathname = imageFile.getPath();
        // Replace the extension with .ocr
        int extIndex = pathname.lastIndexOf(DOT);
        if (extIndex != -1) {
            pathname = pathname.substring(0, extIndex) + OCR_EXT;
            FileIOUtil.writeFile(pathname,
                                 imageText,
                                 false); // append
        }
    }
    
    /**
     * Given a directory with images, it checks if all of them have been
     * processed (i.e., text recognition) or not.
     * 
     * @param imageDir The directory with the images
     * 
     * @return {@code true} if all images have been processed or {@code false}
     *         otherwise
     */
    protected static boolean allImagesProcessed(File imageDir) {
        List<File> imageFiles;
        
        try {
            imageFiles = DirectoryUtil.listFiles(imageDir, IMAGE_EXTENSIONS);
        }
        catch (ErrorException error) {
            System.out.println(error.getMessage());
            return false;
        }
        
        for (File imageFile : imageFiles) {
            File ocrFile = GUIHelper.getTextFile(imageFile);
            if (ocrFile == null) { // Image not processed yet
                return false;
            }
        }
        
        return true;
    }
}

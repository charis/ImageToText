/*
 * File          : OCRUtil.java
 * Author        : Charis Charitsis
 * Creation Date : 22 November 2023
 * Last Modified : 23 November 2023
 */
package util;

// Import Java SE classes
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
// Import library classes
import com.aspose.ocr.AsposeOCR;
import com.aspose.ocr.InputType;
import com.aspose.ocr.OcrInput;
import com.aspose.ocr.RecognitionResult;
import com.aspose.ocr.RecognitionSettings;
// Import custom classes
import exception.ErrorException;
import util.filesystem.DirectoryUtil;
import util.io.FileIOUtil;
// Import constants
import static constants.Literals.COLON;
import static constants.Literals.NEW_LINE;
import static constants.Literals.STRAIGHT_LINE_SEPARATOR;


/**
 * Converts image to text
 */
public class OCRUtil
{
    /**
     * Instance of the module that handles OCR operations 
     */
    private static final AsposeOCR OCR = new AsposeOCR();
    
    /**
     * Extracts the text from the given image. 
     * 
     * @param imageFile The image to extract the text from
     * 
     * @return the recognized text
     * 
     * @throws ErrorException in case of an error
     */
    public static String imageToText(File imageFile)
                  throws ErrorException {
        OcrInput ocrInput = new OcrInput(InputType.SingleImage);
        ocrInput.add(imageFile.getPath());
        RecognitionSettings defaultSettings = new RecognitionSettings();
        
        ArrayList<RecognitionResult> recognitionResults;
        
        try {
            recognitionResults = OCR.Recognize(ocrInput, defaultSettings);
        }
        catch (Exception e) {
            throw new ErrorException(e.getMessage());
        }
        
        StringBuilder text = new StringBuilder();
        Iterator<RecognitionResult> itr = recognitionResults.iterator();
        while (itr.hasNext()) {
            text.append(itr.next().recognitionText);
            if (itr.hasNext()) {
                text.append(NEW_LINE);
            }
        }
        
        return text.toString();
    }
    
    /**
     * Extracts the text from the given image and appends it to the give file.
     * 
     * @param dir The directory with the images to extract the text from
     * @param ext The extension of the image files to consider
     * @param outputFile The output file with the 
     */
    public static void extractTextFromImages(File   dir,
                                             String ext,
                                             File   outputFile) {
        // Find the images in the directory
        List<File> imageFiles;
        try {
            imageFiles = DirectoryUtil.listFiles(dir, ext);
        }
        catch (ErrorException error) {
            System.out.println("Error listing the imades in " + dir.getPath()
                             + ": " + error.getMessage());
            return;
        }
        // Make sure there are images to process
        if (imageFiles.isEmpty()) {
            System.out.println("No images (*" + ext + ") found in "
                             + dir.getPath());
            return;
        }
        
        // Extract the text from the images
        StringBuilder errors = new StringBuilder();
        List<String> textFromImages = new ArrayList<String>();
        
        for (File imageFile : imageFiles) {
            try {
                String text = imageToText(imageFile);
                textFromImages.add(imageFile.getName() + COLON + NEW_LINE
                                 + NEW_LINE + text             + NEW_LINE
                                 + STRAIGHT_LINE_SEPARATOR);
            }
            catch (ErrorException error) {
                errors.append(imageFile.getName() + COLON + NEW_LINE);
                errors.append(error.getMessage() + NEW_LINE);
                errors.append(STRAIGHT_LINE_SEPARATOR + NEW_LINE);
            }
        }
        
        // Write the result to the output file
        try {
            FileIOUtil.writeFile(outputFile,
                                 textFromImages,
                                 false, // append
                                 true); // addNewLine
        }
        catch (ErrorException error) {
            System.out.println("Error creating output " + outputFile.getPath()
                             + ": " + error.getMessage());
            return;
        }
        
        // Report the errors (if any)
        if (!errors.isEmpty()) {
            System.out.println("Failed to process the following files: "
                             + NEW_LINE + errors);
        }
    }
}

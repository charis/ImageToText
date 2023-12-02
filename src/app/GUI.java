/*
 * File          : GUI.java
 * Author        : Charis Charitsis
 * Creation Date : 3 November 2020
 * Last Modified : 28 November 2023
 */
package app;

// Import Java SE classes
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
// Import custom classes
import exception.ErrorException;
import gui.BrowsePanel;
import gui.IconUtil;
import gui.OnOffSwitchPanel;
import gui.ProgressPanel;
import util.UIUtil;
import util.filesystem.DirectoryUtil;
import util.io.FileIOUtil;
// Import constants
import static constants.Constants.LOAD_IMAGES_FROM_CLASSPATH;
import static constants.Constants.IMAGE_EXTENSIONS;
import static constants.Literals.NEW_LINE;
import static app.Constants.BACKGROUND_COLOR;
import static gui.Constants.IMAGE_PACKGAGE;
import static gui.Constants.IMAGE_REPOSITORY;
import static gui.Constants.JCOMPONENT_HEIGHT;

/**
 * User interface for Delve 
 */
public class GUI extends JFrame
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = -5707156809453721497L;
    
    // ------------------------------------- //
    //   C   O   N   S   T   A   N   T   S   //
    // ------------------------------------- //
    /** The screen size */
    protected static final Dimension SCREEN_SIZE   = Toolkit.getDefaultToolkit()
                                                            .getScreenSize();
    /** The GUI width in pixels */
    protected static final int       WINDOW_WIDTH  = SCREEN_SIZE.width >= 1300?
                                                     1300 : SCREEN_SIZE.width;
    /** The GUI width in pixels */
    protected static final int       WINDOW_HEIGHT = SCREEN_SIZE.height >= 1035?
                                                     1000 :
                                                     SCREEN_SIZE.height - 35;
    /** Gap between GUI elements */
    private static final int         GAP               = 20;
    /**
     * The width in pixels for the column that shows the tree with the student
     * submission folders
     */
    private static final int         DIR_TREE_WIDTH    = 270;
    /**
     * The width in pixels for the main panel
     */
    private static final int         MAIN_PANEL_WIDTH    = WINDOW_WIDTH 
                                                         - DIR_TREE_WIDTH - GAP;
    /**
     * The size of the progress bar
     */
    private static final Dimension PROGRESS_BAR_SIZE   = new Dimension(150, 21);
    /** The size for the buttons */
    private static final Dimension BUTTON_SIZE         = new Dimension(80,
                                                             JCOMPONENT_HEIGHT);
    /**
     * The height in pixels for the control panel
     */
    private static final int       CONTROL_ITEM_HEIGHT = 65;
    /**
     * The size of the panel to start/stop the analysis for a given submission
     */
    private static final Dimension DIR_ANALYSIS_PANEL_SIZE = 
                                   new Dimension(4 * BUTTON_SIZE.width,
                                                 CONTROL_ITEM_HEIGHT);
    
    // --------------------------------------------------------------------- //
    //   P   R   I   V   A   T   E       V   A   R   I   A   B   L   E   S   //
    // --------------------------------------------------------------------- //
    /** The main panel with the image preview and the recognized image text */
    private SplitPanel         mainPanel;
    /** The panel with the image directories as a file tree */
    private JPanel             fileTreePanel;
    /** The file tree with the images to process */
    private ImageFileTree      fileTree;
    /** The control to process the images in the selected directory */
    private OnOffSwitchPanel   processDirSwitch;
    /** Module to visually display the OCR analysis progress */
    private ProgressPanel      ocrProgressPanel;
    /** The button to save the image text to the corresponding .ocr file */
    private JButton            saveOCRFileButton;
    /** Worker thread that analyzes an image directory */
    private Thread             workerThread;
    
    // ----------------------------------------------- //
    //  C   O   N   S   T   R   U   C   T   O   R   S  //
    // ----------------------------------------------- //
    /**
     * Creates a GUI for the method name evaluation.
     */
    public GUI() {
        super("Image to Text Conversion");
        
        if (LOAD_IMAGES_FROM_CLASSPATH) {
            URL url = ClassLoader.getSystemClassLoader()
                                 .getResource(IMAGE_PACKGAGE + "/app.png");
            if (url != null) {
                BufferedImage image = null;
                try {
                    image = ImageIO.read(url);
                    setIconImage(image);
                }
                catch (IOException e) {
                }
            }
        }
        else {
            setIconImage(Toolkit.getDefaultToolkit().getImage(IMAGE_REPOSITORY
                                                            + File.separator
                                                            + "app.png"));
        }
        
        // Step 1: Create visual components of the GUI
        createGUI();
        
        // Step 2: Make the frame visible only once done with everything
        setVisible(true);
    }
    
    // ---------------------------------------------------------- //
    //  P   R   I   V   A   T   E      M   E   T   H   O   D   S  //
    // ---------------------------------------------------------- //
    /**
     * Creates the GUI. <br>
     * The GUI has all the visual components, but no ability to interact with
     * the user.
     */
    private void createGUI() {
        configureWindow();
        
        JComponent container = (JComponent)getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        
        // Add GUI components
        
        // File Tree Panel: shows the image directories as a file tree
        JPanel fileTreePanel = createFileTreePanel();
        container.add(fileTreePanel);
        
        // Main Panel: shows the control toolbar on top and then either an
        //             editor to view/modify the source code or a number of
        //             plots with aggregated info
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        //  1) Control Panel
        JPanel controlPanel = createControlPanel();
        controlPanel.setBackground(BACKGROUND_COLOR);
        Dimension controlPanelSize = new Dimension(MAIN_PANEL_WIDTH, 0);
        controlPanel.setMinimumSize(controlPanelSize);
        centerPanel.add(controlPanel, BorderLayout.PAGE_START);
        
        // 2) Main Panel
        // The height of the main panel is the window height after we subtract
        // the control panel height (approx. equal to the button height) 
        int mainPanelHeight = (int) (WINDOW_HEIGHT - BUTTON_SIZE.getHeight());
        Dimension mainPanelSize = new Dimension(MAIN_PANEL_WIDTH,
                                                mainPanelHeight);
        mainPanel = new SplitPanel(mainPanelSize,
                                   BACKGROUND_COLOR);
        centerPanel.add(mainPanel, BorderLayout.CENTER);
        centerPanel.setPreferredSize(mainPanelSize);
        centerPanel.setMaximumSize(mainPanelSize);
        
        container.add(centerPanel, BorderLayout.CENTER);
        container.setBackground(BACKGROUND_COLOR);
        
        // Initially, the plot panel does not appear on screen
        reset();
        
        // Make the frame non-resizable
        setResizable(true);
    }
    
    // ----------------------------------------------------------------- //
    //   S  U  B  M  I  S  S  I  O  N     T  R  E  E     P  A  N  E  L   //
    // ----------------------------------------------------------------- //
    /**
     * Creates the panel that displays the image directories in a tree
     * structure.<br>
     * 
     * @return the panel with the image directories in a tree structure
     */
    private JPanel createFileTreePanel() {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        
        fileTreePanel = new JPanel();
        fileTreePanel.setLayout(new BoxLayout(fileTreePanel, BoxLayout.Y_AXIS));
        fileTreePanel.setAlignmentX(LEFT_ALIGNMENT);
        fileTreePanel.setMaximumSize(new Dimension(DIR_TREE_WIDTH,
                                                   WINDOW_HEIGHT));
        fileTreePanel.setBackground(BACKGROUND_COLOR);
        
        // 1. Select the root directory with subfolders with the images to
        //    process
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.X_AXIS));
        final JLabel loadingLabel = new JLabel(" ");
        @SuppressWarnings("serial")
        JPanel rootDirBrowsePanel = new BrowsePanel(
               null,
               "Select the root directory with the image folders to process",
               JFileChooser.DIRECTORIES_ONLY,
               null,
               null
        ) {
            public void performAction(File rootDir) {
                if (rootDir != null   &&
                    rootDir.canRead() && rootDir.isDirectory()) {
                    loadingLabel.setText("  Loading ...");
                    loadingLabel.validate();
                    reset();
                    
                    // Use a thread (do not block the Swing thread) to load the 
                    // file tree for the processed structure
                    Thread worker = new Thread() {
                        @Override
                        public void run() {
                            fileTree = new ImageFileTree(rootDir,
                                                         IMAGE_EXTENSIONS) {
                                @Override
                                public void nodeSelected(File node) {
                                    if (node.isFile()) {
                                        File ocr = GUIHelper.getTextFile(node);
                                        if (ocr != null) {
                                            displayImageText(ocr);
                                            // Enable button to save the text
                                            saveOCRFileButton.setEnabled(true);
                                        }
                                        else {
                                            // Clear the text panel
                                            mainPanel.clear();
                                            // Disable button to save the text
                                            saveOCRFileButton.setEnabled(false);
                                        }
                                        
                                        mainPanel.loadImage(node);
                                    }
                                    else if (node.isDirectory()) {
                                        mainPanel.clear();
                                        
                                        if (GUIHelper.allImagesProcessed(node)){
                                            //processDirSwitch.unlockButtons();
                                            processDirSwitch.disableButtons();
                                        }
                                        else {
                                            processDirSwitch.enableOnButton();
                                        }
                                        // Disable button to save text
                                        saveOCRFileButton.setEnabled(false);
                                   }
                                }
                            };
                            
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    fileTreePanel.add(fileTree);
                                    resultPanel.validate();
                                    loadingLabel.setText("");
                                }
                            });
                        }
                    };
                    
                    worker.start();
                }
            }
        };
        
        loadingPanel.setMaximumSize(new Dimension(DIR_TREE_WIDTH,
                                                  2 * JCOMPONENT_HEIGHT));
        
        // Now add the components
        rootDirBrowsePanel.setBackground(BACKGROUND_COLOR);
        loadingPanel.add(rootDirBrowsePanel);
        loadingPanel.add(loadingLabel);
        loadingPanel.setBorder(BorderFactory.createTitledBorder(
                                           "Load images for text recognition"));
        loadingPanel.setAlignmentX(LEFT_ALIGNMENT);
        loadingPanel.setBackground(BACKGROUND_COLOR);
        resultPanel.add(loadingPanel);
        resultPanel.add(fileTreePanel);
        resultPanel.setBackground(BACKGROUND_COLOR);
        
        return resultPanel;
    }
    
    /**
     * Creates the panel with the controls.
     * 
     * @return the panel with the controls
     */
    private JPanel createControlPanel() {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.X_AXIS));
        
        // ---------------------------------------------------------------- //
        //   Switch to start/stop OCR processing in the selected directory  //
        // ---------------------------------------------------------------- //
        ActionListener onActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Start analysis
                processSelectedDir();
                processDirSwitch.enableOffButton();
                processDirSwitch.lockButtons();
            }
        };
        ActionListener offActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Stop analysis
                if (workerThread != null && workerThread.isAlive()) {
                    workerThread.interrupt();
                }
                processDirSwitch.unlockButtons();
                processDirSwitch.enableOnButton();
            }
        };
        
        processDirSwitch =
               new OnOffSwitchPanel("Image Text Recognition",
                                    "Start",
                                    IconUtil.getIcon("start.png"),
                                    "Process the selected directory",
                                    onActionListener,
                                    "Stop",
                                    IconUtil.getIcon("stop.png"),
                                    "Stop processing the selected directory",
                                    offActionListener,
                                    BUTTON_SIZE);
        processDirSwitch.setBackground(BACKGROUND_COLOR);
        processDirSwitch.setMaximumSize(DIR_ANALYSIS_PANEL_SIZE);
        
        // ------------------ //
        //  OCR Progress Bar  //
        // ------------------ //
        ocrProgressPanel = new ProgressPanel(PROGRESS_BAR_SIZE);
        ocrProgressPanel.setBackground(BACKGROUND_COLOR);
        processDirSwitch.add(ocrProgressPanel);
        
        resultPanel.add(processDirSwitch);
        
        
        // ----------------------- //
        //   Save OCR File Button  //
        // ----------------------- //
        JPanel ocrPanel = new JPanel();
        ocrPanel.setLayout(new BoxLayout(ocrPanel, BoxLayout.X_AXIS));
        ocrPanel.setBorder(BorderFactory.createTitledBorder("OCR File"));
        ocrPanel.setBackground(BACKGROUND_COLOR);
        
        saveOCRFileButton = new JButton("Save", IconUtil.getIcon("save.png"));
        saveOCRFileButton.setPreferredSize(BUTTON_SIZE);
        saveOCRFileButton.setToolTipText("Save OCR File");
        saveOCRFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Start analysis
                File imageFile = fileTree.getSelectedItem();
                String imageText = mainPanel.getText();
                
                try {
                    GUIHelper.saveImageText(imageFile, imageText);
                }
                catch (ErrorException error) {
                    System.out.println("Error saving image text. Details "
                                     + NEW_LINE + error.getMessage());
                }
            }
        });
        saveOCRFileButton.setEnabled(false); // By default: disabled
        ocrPanel.add(saveOCRFileButton);
        resultPanel.add(ocrPanel);
        
        return resultPanel;
    }
    
    /**
     * Loads the text from the give image text file and displays it on screen.
     *  
     * @param imageTextFile The file with the image text
     */
    private void displayImageText(File imageTextFile) {
        try {
            List<String> lines = FileIOUtil.readFile(imageTextFile.getPath(),
                                                     false, // skipEmptyLines
                                                     false, // skipCommentLines
                                                     false, // trimLines
                                                     null); // regexToMatch
            
            StringBuilder fileContentsStrBuilder = new StringBuilder();
            for (String currLine: lines) {
                fileContentsStrBuilder.append(currLine);
                fileContentsStrBuilder.append(NEW_LINE);
            }
            
            String text = fileContentsStrBuilder.toString();
            mainPanel.setText(text, true);
            mainPanel.setTextEditorEditable(true);
            // Disable the buttons to analyze a submission folder (since we
            // selected a source file and not a submission folder)
            processDirSwitch.disableButtons();
        }
        catch (ErrorException ee) {
            UIUtil.showError(ee.getMessage(),
                             "Error loading code from file '" 
                            + imageTextFile.getName() + "'");
        }
    }
    
    /**
     * Reset to initial state after loading the application.<br>
     * Clears all text, plots etc.<br>
     */
    private void reset() {
        fileTreePanel.removeAll();
        mainPanel.clear();
        processDirSwitch.disableButtons();
        ocrProgressPanel.setVisible(false);
        validate();
    }
    
    /**
     * Configures the GUI window.<br>
     * 1. Sets the look and feel.<br>
     * 2. Defines the behavior when the close window button is pressed<br>
     * 3. Sets the window size and location<br>
     */
    private void configureWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored) {
            // Do nothing (it's ok even if we can't set the look-and-feel)
        }
        setForeground(Color.BLACK);
        setBackground(Color.LIGHT_GRAY);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        pack();
        
        setResizable(true); // Do not allow the user to resize the window
        setLocationRelativeTo(null); // Center the application window
    }
    
    // ------------   D  I  R     P  R  O  C  E  S  S  I  N  G   ------------ //
    /**
     * Processes the images in the selected directory.<br>
     * It recognizes the text for the images that have not been processed so far
     * (i.e., those which have not an associated .ocr file) 
     */
    private void processSelectedDir() {
        File imageDir = fileTree.getSelectedItem();
        List<File> imagesToProcess = new ArrayList<File>();
        try {
            List<File> imageFiles = DirectoryUtil.listFiles(imageDir,
                                                            IMAGE_EXTENSIONS);
            for (File imageFile : imageFiles) {
                File ocrFile = GUIHelper.getTextFile(imageFile);
                if (ocrFile == null) { // Image not processed yet
                    imagesToProcess.add(imageFile);
                }
            }
        }
        catch (ErrorException error) {
            System.out.println(error.getMessage());
        }
        
        // Kick off the thread that tries to process the images
        final double percent = 100.0 / imagesToProcess.size();
        ocrProgressPanel.reset(true); // Show the progress bar with a value of 0%
        
        workerThread = new Thread() {
            @Override
            public void run() {
                int count = 0;
                for (File imageFile : imagesToProcess) {
                    try {
                        // OCR recognition
                        GUIHelper.recognizeText(imageFile);
                    }
                    catch (ErrorException ee) { // Error processing image
                        System.out.println("Error processing '"
                                         + imageFile.getPath() + "'. Details:"
                                         + NEW_LINE + ee.getMessage());
                    }
                    count++;
                    
                    int progressValue = (int)Math.round(count * percent);
                    ocrProgressPanel.setValue(progressValue);
                    
                    if (isInterrupted()) {
                        ocrProgressPanel.reset(false); // Reset + hide progress bar
                        return;
                    }
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        processDirSwitch.unlockButtons();
                        processDirSwitch.disableButtons();
                        ocrProgressPanel.setVisible(false);
                    }
                });
            }
        };
        
        workerThread.start();
    }
    
    /**
     * Main application method.
     * 
     * @param args Ignored
     */
    public static void main(String[] args) {
        new GUI();
    }
}

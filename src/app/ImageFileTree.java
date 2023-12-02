/*
 * File          : ImageFileTree.java
 * Author        : Charis Charitsis
 * Creation Date : 25 December 2020
 * Last Modified : 25 November 2023
 */
package app;

// Import Java SE classes
import java.awt.Component;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
// Import custom classes
import gui.FileTree;
import gui.IconUtil;
import gui.TreeCellRenderer;

/**
 * Display a file system under a given root directory in a JTree view.
 */
public abstract class ImageFileTree extends FileTree
{
    /**
     * Universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object. If no match is found, then 
     * an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 5936066638048892473L;
    
    // ------------------------------------------------- //
    //   C   O   N   S   T   R   U   C   T   O   R   S   //
    // ------------------------------------------------- //
    /**
     * Creates a tree directory structure for the given directory.
     *  
     * @param rootDir The root directory/root node in the file tree
     * @param fileExtensions The extensions that a file should end with to be
     *                       included in the tree or {@code null} to ignore this
     *                       filter (i.e., any files extensions)
     */
    public ImageFileTree(File     rootDir,
                         String[] fileExtensions) {
        super(rootDir, fileExtensions);
        setCellRenderer(new ImageCellRenderer(rootDir));
    }
    
    // -------------------------------------------- //
    //   I   N   N   E   R      C   L   A   S   S   //
    // -------------------------------------------- //
    /**
     * Determines how a tree node is displayed.
     */
    private class ImageCellRenderer extends TreeCellRenderer
    {
        /**
         * Universal version identifier for this Serializable class.
         * Deserialization uses this number to ensure that a loaded class
         * corresponds exactly to a serialized object. If no match is found, then 
         * an InvalidClassException is thrown.
         */
        private static final long serialVersionUID = 1531699149333909L;
        
        // ------------------------------------- //
        //   C   O   N   S   T   A   N   T   S   //
        // ------------------------------------- //
        /**
         * Image icon for non-empty directories in the tree with all their
         * images processed.
         */
        private final ImageIcon PROCESSED_DIR_ICON   =
                                IconUtil.getIcon("folder-processed.png");
        /**
         * Image icon for non-empty directories in the tree with all at least
         * one image not processed.
         */
        private final ImageIcon NOT_PROCESSED_DIR_ICON =
                                IconUtil.getIcon("folder-not-processed.png");
        
        /**
         * Image icon for image files in the tree that are processed (i.e., the
         * text is extracted from the image).
         */
        private final ImageIcon PROCESSED_FILE_ICON      =
                                IconUtil.getIcon("file-processed.png");
        /**
         * Image icon for image files in the tree that are not processed (i.e.,
         * the text is not extracted from the image).
         */
        private final ImageIcon NOT_PROCESSED_FILE_ICON    =
                                IconUtil.getIcon("file-not-processed.png");
        
        // ------------------------------------------------- //
        //   C   O   N   S   T   R   U   C   T   O   R   S   //
        // ------------------------------------------------- //
        /**
         * Creates a new TreeCellRenderer which determines how a tree node is
         * displayed.
         * 
         * @param rootDir The root directory/root node in the file tree
         */
        private ImageCellRenderer(File rootDir) {
            super(rootDir);
        }
        
        // -------------------------------------------------------- //
        //   P   U   B   L   I   C      M   E   T   H   O   D   S   //
        // -------------------------------------------------------- //
        @Override
        public Component getTreeCellRendererComponent(JTree   tree,
                                                      Object  value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int     row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree,
                                               value,
                                               sel,
                                               expanded,
                                               leaf,
                                               row,
                                               hasFocus);
            
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                File fileOrDir = getFile(node, new File(getRootDirPathname()));
                if (fileOrDir.isDirectory()) {
                    if (GUIHelper.allImagesProcessed(fileOrDir)) {
                        setIcon(PROCESSED_DIR_ICON);
                    }
                    else {
                        setIcon(NOT_PROCESSED_DIR_ICON);
                    }
                }
                else if (fileOrDir.isFile()) {
                    if (GUIHelper.getTextFile(fileOrDir) != null) {
                        setIcon(PROCESSED_FILE_ICON);
                    }
                    else {
                        setIcon(NOT_PROCESSED_FILE_ICON);
                    }
                }
            }
            
            return this;
        }
    }
}

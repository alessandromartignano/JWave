package org.jwave.view;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * A container for JavaFX nodes
 * which is simple to set the content.
 * 
 * @author Alessandro Martignano
 *
 */
public class ScreenContainer {
    
    private Pane mainPane;
    
    /**
     * @param pane main Pane
     */
    public ScreenContainer(final Pane pane) {
        this.mainPane = pane;
    }

    /**
     * Replaces the screen displayed in the main pane with a new screen.
     *
     * @param node the screen node to be swapped in.
     */
    public void setScreen(final Node node) {
        this.mainPane.getChildren().setAll(node);
    }

}

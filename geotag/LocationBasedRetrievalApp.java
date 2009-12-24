/*
 * LocationBasedRetrievalApp.java
 */

package geotag;

import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class LocationBasedRetrievalApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        //show(new LocationBasedRetrievalView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }


}

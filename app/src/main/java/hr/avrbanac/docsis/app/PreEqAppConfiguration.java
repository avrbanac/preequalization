package hr.avrbanac.docsis.app;

/**
 * This is a main APP configuration class. These parameters could be defined in a properties file, but since the whole application serves as
 * a testing playground, there's really no reason to do so.
 */
public class PreEqAppConfiguration {
    public static final int APP_WIDTH = 1024;
    public static final int APP_HEIGHT = 768;
    public static final String APP_TITLE = "PreEqualization";
    public static final String APP_VIEW_FILE = "/views/preeq-view.fxml";
    public static final String ICON_16_FILE = "/icons/preEqIcon16.png";
    public static final String ICON_32_FILE = "/icons/preEqIcon32.png";
    public static final String ICON_64_FILE = "/icons/preEqIcon64.png";
    public static final String APP_CSS_FILE = "preEqualization.css";

    /**
     * No need for instance to this class.
     */
    private PreEqAppConfiguration() { }
}

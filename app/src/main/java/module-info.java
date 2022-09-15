module hr.avrbanac.preeequalization.app {
    requires javafx.controls;
    requires javafx.fxml;

    opens hr.avrbanac.docsis.app to javafx.fxml;
    exports hr.avrbanac.docsis.app;
}
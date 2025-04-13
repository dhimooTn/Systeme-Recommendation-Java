module com.sysrec.projet_ds1_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.sysrec.projet_ds1_java to javafx.fxml;
    exports com.sysrec.projet_ds1_java;
    exports com.sysrec.projet_ds1_java.Controller;
    opens com.sysrec.projet_ds1_java.Controller to javafx.fxml;
    exports com.sysrec.projet_ds1_java.Utils;
    opens com.sysrec.projet_ds1_java.Utils to javafx.fxml;
}
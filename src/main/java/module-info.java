module com.sysrec.projet_ds1_java {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.sysrec.projet_ds1_java to javafx.fxml;
    exports com.sysrec.projet_ds1_java;
}
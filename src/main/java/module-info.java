module com.sysrec.projet_ds1_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Ouvre les bons packages
    opens com.sysrec.projet_ds1_java to javafx.fxml, mahout.core;
    opens com.sysrec.projet_ds1_java.Controller to javafx.fxml;
    opens com.sysrec.projet_ds1_java.Utils to javafx.fxml;

    // 🔧 Ajouté : ouvrir le package Model à javafx.base
    opens com.sysrec.projet_ds1_java.Model to javafx.base;

    // Si ton service de recommandation est dans ce package :
    opens com.sysrec.projet_ds1_java.Service to mahout.core;

    // Exports
    exports com.sysrec.projet_ds1_java;
    exports com.sysrec.projet_ds1_java.Controller;
    exports com.sysrec.projet_ds1_java.Utils;
    exports com.sysrec.projet_ds1_java.Service;
}

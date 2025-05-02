module com.sysrec.projet_ds1_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    // 👇 Ajoute les opens/exports nécessaires
    opens com.sysrec.projet_ds1_java to javafx.fxml, mahout.core;
    exports com.sysrec.projet_ds1_java;

    exports com.sysrec.projet_ds1_java.Controller;
    opens com.sysrec.projet_ds1_java.Controller to javafx.fxml;

    exports com.sysrec.projet_ds1_java.Utils;
    opens com.sysrec.projet_ds1_java.Utils to javafx.fxml;

    // Si ton service de recommandation est dans ce package :
    exports com.sysrec.projet_ds1_java.Service;
    opens com.sysrec.projet_ds1_java.Service to mahout.core;
}

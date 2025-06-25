module com.mailapp.demo {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;

    requires java.base;
    requires com.google.gson;
    opens com.mailapp.demo.Mains to javafx.fxml;
    opens com.mailapp.demo.Controllers to javafx.fxml;
    opens com.mailapp.demo.Models to javafx.fxml;
    exports com.mailapp.demo.Mains;
    exports com.mailapp.demo.Controllers;
    exports com.mailapp.demo.Models;
}

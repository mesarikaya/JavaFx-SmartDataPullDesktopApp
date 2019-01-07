package com.mesarikaya.getItApp.Controllers;

import com.mesarikaya.getItApp.Services.DataService;
import com.mesarikaya.getItApp.Services.DataServiceImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class LoginController {
    private static final String CONN_URL_START="jdbc:impala://drona-impala.DETAILS_ARE_NOT_SHARED";

    // Add the parameters
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private GridPane topContainer;

    @FXML
    private GridPane loginView;

    @FXML
    private Button loginButton;

    @FXML
    private HBox loginButtonHBox;

    @FXML
    private Button closeButton;

    // Get Data Service
    private DataService dataService;

    // Scene
    private Scene secondScene;

    // PageLoader
    private FXMLLoader secondPageLoader;

    // Second Page Controller
    private UserPageController userPageController;

    // Initialize the login mainframe
    public void initialize() {

    }

    public GridPane getLoginView() {
        return loginView;
    }

    public void setSecondScene(Scene scene) {
        secondScene = scene;
    }

    public void setSecondPageLoader(FXMLLoader secondPageLoader) {
        this.secondPageLoader = secondPageLoader;
    }

    // inject the scene
    public void openSecondScene(ActionEvent actionEvent, Connection conn) throws Exception{
        Stage primaryStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        Parent secondPane = secondPageLoader.load();
        userPageController = (UserPageController) secondPageLoader.getController();
        userPageController.setConn(conn);
        Scene secondScene = new Scene(secondPane, 400, 400);
        primaryStage.setScene(secondScene);
    }

    // Close the App on Close(X) button click
    @FXML
    public void closeApp(ActionEvent e){
        closeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                Platform.exit();
            }
        });
    }

    @FXML
    public void onButtonClick(ActionEvent e){

        try{
            Connection connection = connectViaDS();

            // Connection is successful, open the second scene - User page
            if (connection != null){
                openSecondScene(e, connection);
            }else{
                dataService.showCredentialsErrorNotification("Connection is unsuccessful due to wrong password or network issues!");
            }

        }catch(Exception exc){
            exc.printStackTrace();
        }
    }

    private Connection connectViaDS() throws Exception{
        Connection result = null;

        try{
            // Set the connection URL
            String CONNECTION_URL = CONN_URL_START + "UID="+ username.getText() + "NO_DETAILS_SHARED;" + "PWD=" + password.getText() + ";";

            // Set data Service
            dataService = new DataServiceImpl();

            // Set the connection
            Connection conn = dataService.setConnection(CONNECTION_URL);

            if (conn!=null){
                result = conn;
            }
        }catch(Exception exc){
            throw new Exception("Sth went wrong during database connection trial " + exc);
        }

        return result;
    }


}

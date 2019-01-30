package com.mesarikaya.getItApp;

import com.mesarikaya.getItApp.Controllers.LoginController;
import com.mesarikaya.getItApp.Controllers.UserPageController;
import com.mesarikaya.getItApp.Services.ScreenService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private static LoginController loginController;
    private static UserPageController userPageController;
    private static double xOffset = 0;
    private static double yOffset = 0;
    private ScreenService screenService;

    @Override
    public void start(Stage primaryStage) throws Exception{

        // Set the login page scene
        FXMLLoader firstPaneLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Pane firstPane = firstPaneLoader.load();
        Scene firstScene = new Scene(firstPane);

        // Set the user page scene
        FXMLLoader secondPageLoader = new FXMLLoader(getClass().getResource("userPage.fxml"));

        // injecting second scene into the controller of the first scene
        LoginController firstPaneController = (LoginController) firstPaneLoader.getController();
        firstPaneController.setSecondPageLoader(secondPageLoader);
        firstPaneController.setPrimaryStage(primaryStage);
        firstPaneController.setScreenDraggable(true); // Enable screen dragging

        primaryStage.setResizable(true);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("GeTIt");
        primaryStage.setScene(firstScene);

        firstScene.setFill(Color.valueOf("#2F3739"));
        primaryStage.setMaxHeight(400);
        primaryStage.setMaxWidth(400);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

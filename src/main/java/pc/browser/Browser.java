/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.WritableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author prem
 */
public class Browser extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setOpacity(0);
        FXMLLoader loader = new FXMLLoader(Browser.class.getResource("/fxml/Main.fxml"));
        primaryStage.setScene(new Scene(loader.load(), 1600, 900));
        Timeline showTimeline = new Timeline();
        WritableValue<Double> stageOpacity = new WritableValue<Double>() {
            @Override
            public Double getValue() {
                return primaryStage.getOpacity();
            }

            @Override
            public void setValue(Double value) {
                primaryStage.setOpacity(value);
            }
        };
        showTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(300), new KeyValue(stageOpacity, 1d)));
        primaryStage.setOnShown(we -> showTimeline.play());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

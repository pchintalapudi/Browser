/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.resources;

import java.lang.management.ManagementFactory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author prem
 */
public class NullPageController {

    @FXML
    private StackPane root;

    @FXML
    private Rotate yRotate;

    private final Timeline rotateTimeline = new Timeline();

    @FXML
    private void initialize() {
        rotateTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), e -> yRotate.setAngle(360), new KeyValue(yRotate.angleProperty(), 0)));
        rotateTimeline.setCycleCount(Animation.INDEFINITE);
        root.sceneProperty().addListener((o, b, s) -> {
            if (s != null) {
                yRotate.setAngle(360);
                double val = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
                System.out.println(val);
                if (val < 30) {
                    rotateTimeline.play();
                }
            } else {
                rotateTimeline.stop();
            }
        });
    }

    private void checkCPUUsage() {

    }
}

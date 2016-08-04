package ph.edu.dlsu.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import ph.edu.dlsu.fx.utils.Utils;
import ph.edu.dlsu.fx.vision.Histogram;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    // the FXML startBtn
    @FXML
    private Button startBtn;

    // the FXML image view
    @FXML
    private ImageView currentFrame;

    // the FXML grayscale checkbox
    @FXML
    private CheckBox grayscale;

    // the FXML logo checkbox
    @FXML
    private CheckBox logoCheckBox;

    // the FXML histogram image
    @FXML
    private ImageView histogram;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;

    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();

    // a flag to change the startBtn behavior
    private boolean cameraActive = false;

    // the logo to be loaded
    private Mat logo;

    @FXML
    public void startCamera(ActionEvent actionEvent) {

        // set a fixed width for the frame
        this.currentFrame.setFitWidth(640);

        // preserve image ratio
        this.currentFrame.setPreserveRatio(true);

        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(0);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = () -> {
                    Image imageToShow = grabFrame();
                    currentFrame.setImage(imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the startBtn content
                this.startBtn.setText("Stop Camera");
            } else {
                System.err.println("Failed to open the camera...");
            }
        } else {

            // the camera is not active at this point
            this.cameraActive = false;
            // update again the startBtn content
            this.startBtn.setText("Start Camera");

            // stop the timer
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }

            // release the camera
            this.capture.release();
            // clean the frame
            this.currentFrame.setImage(null);
        }
    }


    private Image grabFrame() {

        Image imageToShow = null;
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {

                    showLogo(frame);

                    showHistogram(frame);

                    // convert the Mat object (OpenCV) to Image (JavaFX)
                    imageToShow = Utils.mat2Image(frame);
                }

            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return imageToShow;
    }

    private void showHistogram(Mat frame) {

        if (grayscale.isSelected()) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
        }

        // calculate histogram
        Mat histSrc = Histogram.computeHistogram(frame, grayscale.isSelected());

        // show the histogram
        Image histImg = Utils.mat2Image(histSrc);
        this.histogram.setImage(histImg);

    }

    private void showLogo(Mat frame) {
        // add a logo...
        if (logoCheckBox.isSelected() && this.logo != null) {
            Rect roi = new Rect(frame.cols() - logo.cols(), frame.rows() - logo.rows(), logo.cols(),
                    logo.rows());
            Mat imageROI = frame.submat(roi);
            Core.addWeighted(imageROI, 1.0, logo, 0.8, 0.0, imageROI);
        }
    }

    public void loadLogo(ActionEvent actionEvent) {
        if (logoCheckBox.isSelected()) {
            try {
                // read the logo only when the checkbox has been selected
                this.logo = Imgcodecs.imread("res/opencvlogo.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

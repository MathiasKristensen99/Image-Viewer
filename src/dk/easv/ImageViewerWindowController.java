package dk.easv;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class ImageViewerWindowController implements Initializable
{
    private final List<Image> images = new ArrayList<>();
    private final List<String> filenames = new ArrayList<>();
    private final List<Integer> pixelColors = new ArrayList<>();
    public Label lblRed;
    public Label lblGreen;
    public Label lblBlue;
    private BufferedImage image;
    public Slider slider;
    public Label lblFileName;
    private int currentImageIndex = 0;
    private int delay = 1000;

    AtomicInteger red = new AtomicInteger();
    AtomicInteger green = new AtomicInteger();
    AtomicInteger blue = new AtomicInteger();
    AtomicInteger mixed = new AtomicInteger();

    @FXML
    Parent root;

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        slider.valueProperty().addListener((obs, oldVal, newVal) ->
                delay = newVal.intValue());
    }

    @FXML
    private void handleBtnLoadAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (!files.isEmpty())
        {
            files.forEach((File f) ->
            {
                images.add(new Image(f.toURI().toString()));
                filenames.add(f.getName());
                try {
                    image = ImageIO.read(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            displayImage();
        }
    }

    @FXML
    private void handleBtnPreviousAction()
    {
        if (!images.isEmpty())
        {
            currentImageIndex =
                    (currentImageIndex - 1 + images.size()) % images.size();
            displayImage();
        }
    }

    @FXML
    private void handleBtnNextAction()
    {
        if (!images.isEmpty())
        {
            currentImageIndex = (currentImageIndex + 1) % images.size();
            displayImage();
        }
    }

    private void displayImage()
    {
        if (!images.isEmpty())
        {
            imageView.setImage(images.get(currentImageIndex));
        }
    }

    Task task = new Task() {
        @Override
        protected Object call() throws Exception {
            for (int i = 0; i < images.size(); i++) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(images.get(currentImageIndex));
                        lblFileName.setText(filenames.get(currentImageIndex));
                        currentImageIndex++;
                        getPixelColors();
                        lblRed.setText(String.valueOf(red.intValue()));
                        lblGreen.setText(String.valueOf(green.intValue()));
                        lblBlue.setText(String.valueOf(blue.intValue()));
                        if (currentImageIndex >= images.size()) {
                            currentImageIndex = 0;
                        }
                    }
                });
                Thread.sleep(delay);
                System.out.println(delay);
            }
            return null;
        }
    };

    public void getPixelColors() {
        Thread thread = new Thread(() -> {
            for (int y = 0; y < image.getHeight() ; y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);
                    Color color = new Color(pixel, true);

                    if (color.getRed() > color.getGreen() && color.getRed() > color.getBlue()) {
                        red.set(red.get() + 1);
                        System.out.println("Red: " + red);
                    } else if (color.getGreen() > color.getRed() && color.getGreen() > color.getBlue()) {
                        green.set(green.get() + 1);
                        System.out.println("Green: " + green);
                    } else if (color.getBlue() > color.getRed() && color.getBlue() > color.getGreen()) {
                        blue.set(blue.get() + 1);
                        System.out.println("Blue: " + blue);
                    } else {
                        mixed.set(mixed.get() + 1);
                        System.out.println("Mixed: " + mixed);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void handleBtnStartSlideshow(ActionEvent actionEvent) {
        Thread thread = new Thread(task);
        thread.start();
    }

    public void handleBtnStopSlideshow(ActionEvent actionEvent) {
        task.cancel();
    }
}
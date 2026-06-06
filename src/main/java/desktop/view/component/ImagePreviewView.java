package desktop.view.component;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

/**
 * 图表图片预览视图 — 支持缩放的图片查看器。
 */
public class ImagePreviewView extends ScrollPane {

    private final ImageView imageView;
    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 4.0;
    private double currentScale = 1.0;

    public ImagePreviewView() {
        getStyleClass().add("preview-content");
        setFitToWidth(true);
        setFitToHeight(true);
        setPannable(true);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(widthProperty().subtract(20));

        setContent(imageView);

        // 滚轮缩放
        setOnScroll(e -> {
            e.consume();
            double delta = e.getDeltaY() > 0 ? 0.1 : -0.1;
            currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, currentScale + delta));
            imageView.setScaleX(currentScale);
            imageView.setScaleY(currentScale);
        });
    }

    /** 加载图片文件。 */
    public void loadImage(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            currentScale = 1.0;
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
        }
    }

    /** 重置缩放。 */
    public void resetZoom() {
        currentScale = 1.0;
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
    }
}

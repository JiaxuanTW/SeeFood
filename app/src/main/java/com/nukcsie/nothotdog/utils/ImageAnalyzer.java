package com.nukcsie.nothotdog.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.nukcsie.nothotdog.ml.HotDogModel;
import com.nukcsie.nothotdog.models.RecognitionItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Byte.toUnsignedInt;

public class ImageAnalyzer implements ImageAnalysis.Analyzer {
    private final Context context;
    private final ANALYSIS_MODE analysisMode;
    private ByteBuffer outputBuffer;
    private ArrayList<RecognitionItem> recognitionItems;
    private ImageAnalyzerListener imageAnalyzerListener = null;

    public enum ANALYSIS_MODE {
        REAL_TIME,
        NON_REAL_TIME
    }

    public ImageAnalyzer(Context context, ANALYSIS_MODE analysisMode) {
        this.context = context;
        this.analysisMode = analysisMode;
        try {
            initializeRecognitionItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void analyze(@NonNull @NotNull ImageProxy image) {
        try {
            HotDogModel hotDogModel = HotDogModel.newInstance(context);
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(224, 224,
                            ResizeOp.ResizeMethod.BILINEAR)).build();
            TensorImage tensorImage = TensorImage.fromBitmap(toBitmap(image));
            tensorImage = imageProcessor.process(tensorImage);
            outputBuffer = hotDogModel.process(tensorImage.getTensorBuffer())
                    .getOutputFeature0AsTensorBuffer().getBuffer();

            // Trigger the listener if it is in real-time mode
            if (analysisMode == ANALYSIS_MODE.REAL_TIME)
                imageAnalyzerListener.onAnalysisDone(getRecognitionOutputs());

            image.close();
            hotDogModel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @SuppressLint("UnsafeOptInUsageError")
    private Bitmap toBitmap(@NotNull ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image == null) return null;

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
        Bitmap bitmapBuffer = Bitmap.createBitmap(imageProxy.getWidth(),
                imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
        YuvToRgbConverter yuvToRgbConverter = new YuvToRgbConverter(context);
        yuvToRgbConverter.yuvToRgb(image, bitmapBuffer);
        return Bitmap.createBitmap(bitmapBuffer, 0, 0,
                bitmapBuffer.getWidth(), bitmapBuffer.getHeight(),
                rotationMatrix, false);
    }

    private void initializeRecognitionItems() throws IOException {
        Scanner scanner = new Scanner(new InputStreamReader(
                context.getAssets().open("HotDogModelLabels.txt")));
        recognitionItems = new ArrayList<>();
        while (scanner.hasNextLine()) {
            recognitionItems.add(new RecognitionItem(scanner.nextLine(), 0));
        }
        scanner.close();
    }

    public ArrayList<RecognitionItem> getRecognitionOutputs() {
        recognitionItems.get(0).score = toUnsignedInt(outputBuffer.get(0));
        recognitionItems.get(1).score = toUnsignedInt(outputBuffer.get(1));
        recognitionItems.get(2).score = toUnsignedInt(outputBuffer.get(2));
        return recognitionItems;
    }

    public interface ImageAnalyzerListener {
        void onAnalysisDone(ArrayList<RecognitionItem> recognitionItems);
    }

    public void setImageAnalyzerListener(ImageAnalyzerListener listener) {
        imageAnalyzerListener = listener;
    }
}
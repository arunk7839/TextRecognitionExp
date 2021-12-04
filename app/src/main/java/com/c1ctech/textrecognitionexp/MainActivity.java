package com.c1ctech.textrecognitionexp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Button mFindTextBtn;
    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);

        mFindTextBtn = findViewById(R.id.btn_find_text);

        mGraphicOverlay = findViewById(R.id.graphic_overlay);

        //get bitmap of image from app assets.
        mSelectedImage = getBitmapFromAsset(this, "page.png");
        mImageView.setImageBitmap(mSelectedImage);

        mFindTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if bitmap is not null
                if (mSelectedImage != null) {
                    //Creates a new bitmap, scaled from an existing bitmap
                    Bitmap resizedBitmap = createScaleFactorUsingBitmap(mSelectedImage);
                    //setting new scaled bitmap in imageview
                    mImageView.setImageBitmap(resizedBitmap);
                    mSelectedImage = resizedBitmap;
                }

                runTextRecognition();
            }
        });

    }

    private Bitmap createScaleFactorUsingBitmap(Bitmap mSelectedImage) {
        // Determine how much to scale down the image
        float scaleFactor =
                Math.max(
                        (float) mSelectedImage.getWidth() / (float) mImageView.getWidth(),
                        (float) mSelectedImage.getHeight() / (float) mImageView.getHeight());

        Bitmap resizedBitmap =
                Bitmap.createScaledBitmap(
                        mSelectedImage,
                        (int) (mSelectedImage.getWidth() / scaleFactor),
                        (int) (mSelectedImage.getHeight() / scaleFactor),
                        true);

        return resizedBitmap;
    }

    //recognize and extract text from image bitmap
    private void runTextRecognition() {

        //prepare input image using bitmap
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);

        //creating TextRecognizer instance
        TextRecognizer recognizer = TextRecognition.getClient();

        //process the image
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text texts) {
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }


    //perform operation on the full text recognized in the image.
    private void processTextRecognitionResult(Text texts) {

        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(getApplicationContext(), "No text found", Toast.LENGTH_SHORT).show();
            return;
        }
        mGraphicOverlay.clear();
        for (Text.TextBlock block : texts.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element element : line.getElements()) {
                    //Draws the bounding box around the element.
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, element);
                    mGraphicOverlay.add(textGraphic);
                }
            }
        }
    }


    public Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

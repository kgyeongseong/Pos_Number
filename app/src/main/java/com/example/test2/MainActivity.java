package com.example.test2;

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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.textView);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssetManager assetManager = getResources().getAssets();

                InputStream is;
                Bitmap bitmap = null;
                try {
                  is = assetManager.open("barcode3.png");
                  bitmap = BitmapFactory.decodeStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 480, 360, true);
                    imageView.setImageBitmap(resizedBitmap);
                    bitmap = resizedBitmap;

                    InputImage image = InputImage.fromBitmap(bitmap, 0);
                    TextRecognizer recognizer = TextRecognition.getClient();
                    recognizer.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Text>() {
                                        @Override
                                        public void onSuccess(Text texts) {
                                            // 성공
                                            List<Text.TextBlock> blocks = texts.getTextBlocks();
                                            for (int i = 0; i < blocks.size(); i++) {
                                                List<Text.Line> lines = blocks.get(i).getLines();
                                                for (int j = 0; j < lines.size(); j++) {
                                                    List<Text.Element> elements = lines.get(j).getElements();
                                                    for (int k = 0; k < elements.size(); k++) {
                                                        textView.append(elements.get(k).getText());
                                                    }
                                                }
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 실패
                                            e.printStackTrace();
                                        }
                                    });
                }
            }
        });
    }
}
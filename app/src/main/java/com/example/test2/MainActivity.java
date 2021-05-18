package com.example.test2;

import androidx.activity.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

// Google ML Kit Vision API
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

// Google CameraX API
import androidx.core.content.FileProvider;

// 소켓 클라이언트 관련
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    Bitmap bitmap;
    Bitmap rotatedBitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    String mCurrentPhotoPath;

    String product_id;

    Handler handler = new Handler();

    List<Text.Element> elements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientThread thread = new ClientThread();
                thread.start();
            }
        });
    }

    // 카메라 관련
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            File file = new File(mCurrentPhotoPath);
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
            } catch (Exception e) {
                e.printStackTrace();
            }

            rotatedBitmap = null;

            try{
                ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            imageView.setImageBitmap(rotatedBitmap);

            TextRecognize(rotatedBitmap);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 카메라 이미지 회전
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Text Recognition (텍스트 인식)
    private void TextRecognize(Bitmap _bitmap) {
        InputStream is;

        if (_bitmap != null) {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(_bitmap, 480, 360, true);
            imageView.setImageBitmap(resizedBitmap);
            _bitmap = resizedBitmap;

            InputImage image = InputImage.fromBitmap(_bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient();
            recognizer.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text texts) {
                                    // 성공
                                    textView.setText("");
                                    product_id = "";
                                    elements = new ArrayList<>();
                                    List<Text.TextBlock> blocks = texts.getTextBlocks();
                                    for (int i = 0; i < blocks.size(); i++) {
                                        List<Text.Line> lines = blocks.get(i).getLines();
                                        for (int j = 0; j < lines.size(); j++) {
                                            elements = lines.get(j).getElements();
                                            for (int k = 0; k < elements.size(); k++) {
                                                //textView.append(elements.get(k).getText());
                                                product_id += elements.get(k).getText();
                                            }
                                        }
                                    }
                                    textView.setText(product_id);
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

    // 소켓 클라이언트 스레드
    class ClientThread extends Thread {
        public void run() {
            InputStream dataInputStream;
            OutputStream dataOutputStream;
            Socket socket;
            String ip = "172.30.1.35";
            int port = 3333;

            try {
                socket = new Socket(ip, port);
                dataInputStream = socket.getInputStream();
                dataOutputStream = socket.getOutputStream();

                // 서버로 데이터 주기
                byte[] inst = product_id.getBytes();
                //byte[] inst = "Hello".getBytes();
                dataOutputStream.write(inst);
                Log.d("ClientThread", "서버로 보냄");

                // 서버에서 부터 온 데이터 받기
                byte[] buffer = new byte[1024];
                int bytes;
                bytes = dataInputStream.read(buffer);
                String tmp = new String(buffer, 0, bytes);
                Log.d("ClientThread", "받은 데이터 " + tmp);

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
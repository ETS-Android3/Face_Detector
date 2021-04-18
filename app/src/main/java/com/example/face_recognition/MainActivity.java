package com.example.face_recognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE=1;


    private Button btn;
    private ImageView myImageView;
    private Bitmap myBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.button);
        myImageView = findViewById(R.id.imgView);
    }


    //Open Gallery
    public void open_gallery(View view) {
        Intent gg = new Intent();
        gg.setType("image/*");
        gg.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gg.createChooser(gg,"select"),PICK_IMAGE);
    }


    //Show Selected Image on Image View
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE
                && resultCode == RESULT_OK){

            if(myBitmap != null){
                myBitmap.recycle();
            }
            try {
                InputStream inputStream =
                        getContentResolver().openInputStream(data.getData());
                myBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                myImageView.setImageBitmap(myBitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



            public void detect (View v) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inMutable = true;
//                Bitmap myBitmap = BitmapFactory.decodeResource(
//                        getApplicationContext().getResources(),
//                        R.drawable.t2,
//                        options
//                );

                try {


                    //Create a Paint object for drawing with
                    Paint myRectPaint = new Paint();
                    myRectPaint.setStrokeWidth(3);
                    myRectPaint.setColor(Color.GREEN);
                    myRectPaint.setStyle(Paint.Style.STROKE);

                    Paint landmarksPaint = new Paint();
                    landmarksPaint.setStrokeWidth(5);
                    landmarksPaint.setColor(Color.RED);
                    landmarksPaint.setStyle(Paint.Style.STROKE);

                    Paint smilingPaint = new Paint();
                    smilingPaint.setStrokeWidth(3);
                    smilingPaint.setColor(Color.YELLOW);
                    smilingPaint.setStyle(Paint.Style.STROKE);

                    boolean somebodySmiling = false;

                    Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(),
                            Bitmap.Config.RGB_565);
                    Canvas tempCanvas = new Canvas(tempBitmap);
                    tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                    FaceDetector faceDetector = new
                            FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                            .build();

                    if (!faceDetector.isOperational()) {
                        new AlertDialog.Builder(v.getContext()).setMessage("Could not Detect Face").show();
                        return;
                    }

                    //Draw Rectangles on the Faces
                    Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                    SparseArray<Face> faces = faceDetector.detect(frame);

                    for (int i = 0; i < faces.size(); i++) {
                        Face thisFace = faces.valueAt(i);
                        float x1 = thisFace.getPosition().x;
                        float y1 = thisFace.getPosition().y;
                        float x2 = x1 + thisFace.getWidth();
                        float y2 = y1 + thisFace.getHeight();
                        tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);

                        //get Landmarks for the first face
                        List<Landmark> landmarks = thisFace.getLandmarks();
                        for (int l = 0; l < landmarks.size(); l++) {
                            PointF pos = landmarks.get(l).getPosition();
                            tempCanvas.drawPoint(pos.x, pos.y, landmarksPaint);
                        }

                        //check if this face is Smiling
                        final float smilingAcceptProbability = 0.5f;
                        float smilingProbability = thisFace.getIsSmilingProbability();
                        if (smilingProbability > smilingAcceptProbability) {
                            tempCanvas.drawOval(new RectF(x1, y1, x2, y2), smilingPaint);
                            somebodySmiling = true;
                        }

                    }

                        myImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));



                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            "Please Pick Image From Your Gallery First",
                            Toast.LENGTH_LONG).show();
                }
            }


} // Class End




//                if(somebodySmiling){
//                    Toast.makeText(MainActivity.this,
//                            "Done - somebody is Smiling",
//                            Toast.LENGTH_LONG).show();
//                }else{
//                    Toast.makeText(MainActivity.this,
//                            "Done - nobody is Smiling",
//                            Toast.LENGTH_LONG).show(); }
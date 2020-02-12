package com.example.drowsinessdetection;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.Surface;
import android.view.TextureView;
import android.os.Bundle;
import android.view.View;
import java.io.IOException;
import java.util.List;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

   private TextView textView;
   private MediaPlayer mp;
   private SurfaceHolder sh;
   private TextureView tv;
   private String p;
   private AssetFileDescriptor fd;
   private int closedDuration = 0;
   float leftprob;
   float rightprob;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
        //Assigns variables to text and video player elements on user interface
       textView = (TextView)findViewById(R.id.text);
       tv = (TextureView) findViewById(R.id.vid);
       tv.setSurfaceTextureListener(this);
       mp = new MediaPlayer();

       try {
            //Assigns video file to media player
           fd = getAssets().openFd("video_file1.3gp");
       } catch (IOException e) {
           e.printStackTrace();
       }

   }

   //Settings for face detector (FAST over ACCURATE, ALL_LANDMARKS allows the detection of the eyes,
   //ALL_CLASSIFICATIONS allows the classification of images into eyes open and eyes closed)
   FirebaseVisionFaceDetectorOptions options =
           new FirebaseVisionFaceDetectorOptions.Builder()
                   .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST) 
                   .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                   .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                   .build();

   //Initialization of face detector method   
   private void detectFaces (Bitmap bitmap) {
          FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

          FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                  .getVisionFaceDetector(options);

          detector.detectInImage(image)
                  .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                      @Override
                      public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                          for (FirebaseVisionFace face : firebaseVisionFaces) {

                              if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                  leftprob = face.getLeftEyeOpenProbability();
                              }
                              if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                  rightprob = face.getRightEyeOpenProbability();
                              }
                              if (leftprob < 0.5 && rightprob < 0.5) {
                                  textView.setText("Closed"); //If the calculated probability value for both eyes is less than 0.5, the application will say that the driver’s eyes were closed

                              }
                              if (leftprob >= 0.5 && rightprob >= 0.5) {
                                  textView.setText("Open");
                              }
                          }
                      }
                  })
                  .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {

                      }
                  });
      }

   @RequiresApi(api = Build.VERSION_CODES.N)
   @Override
   public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        try {
            mp.setDataSource(fd);
            mp.setSurface(surface);
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    new CountDownTimer(mp.getDuration(), 400) {
                        public void onTick(long millisUntilFinished) {
                            System.out.println("seconds remaining: " + millisUntilFinished / 500);
                            detectFaces(b);
                        }
                        public void onFinish() {
                            textView.setText("done!");
                        }

                    }.start();
                    mp.start();
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   @Override
   public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

   }

   @Override
   public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
       return false;
   }

   @Override
   public void onSurfaceTextureUpdated(SurfaceTexture surface) {

   }
}

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

   private View main;
   private TextView textView;
   private TextView textview;
   private MediaPlayer mp;
   private SurfaceHolder sh;
   private TextureView tv;
   private String p;
   private AssetFileDescriptor fd;
   public ArrayList<Integer> list = new ArrayList<Integer>();
   private int closedDuration = 0;
   float leftprob;
   float rightprob;
   private ImageView imageView;
   private Bitmap b;
   private int numConsecutiveInstances = 0;
   private int instancesClosed = 0;
   private boolean lastInstance = false;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Keeps screen on indefinitely
       main = findViewById(R.id.main);
        //Assigns variables to text and video player, and ImageView (displays images) elements on user interface
       imageView = (ImageView)findViewById(R.id.imageView);
       textView = (TextView)findViewById(R.id.text);
       textview = (TextView)findViewById(R.id.textview);
       tv = (TextureView) findViewById(R.id.vid); /*It is impossible to take a screenshot of the video if the video plays
       on a normal video player (VideoView) element; thus, a TextureView element is used to play the video instead */
       tv.setSurfaceTextureListener(this);
       mp = new MediaPlayer();
       ap = new MediaPlayer(); //Media player for playing the alarm
       ap = MediaPlayer.create(this, R.raw.ringtone); //Setting the source (file to be played) for the alarm

       try {
            //Assigns video file to file descriptor
           fd = getAssets().openFd("video_file1.3gp"); //Video file must be in either .3gp or .mp4 format for it to play
       } catch (IOException e) {
           e.printStackTrace();
       }

   }

   /*Settings for face detector (FAST over ACCURATE, ALL_LANDMARKS allows the detection of the eyes,
   ALL_CLASSIFICATIONS allows the classification of images into eyes open and eyes closed)*/
   FirebaseVisionFaceDetectorOptions options =
           new FirebaseVisionFaceDetectorOptions.Builder()
                   .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST) 
                   .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                   .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                   .build();

   //Initialization of face detector method   
   private void detectFaces (Bitmap bitmap) {
          FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap); /*Method takes in images in
      bitmap form as input*/

          FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                  .getVisionFaceDetector(options); //Applies the settings to the face detector

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
                              if (leftprob < 0.7 && rightprob < 0.7) {
                                  textView.setText("Closed"); /*If the calculated probability value for both eyes is
                                  less than 0.7, the application will say that the driver’s eyes were closed*/
                                  numConsecutiveInstances++;
                                  if (numConsecutiveInstances >= 3) {
                                    lastInstance = true; //If more than 3 consecutive frames feature closed eyes,
                                     //the instance will be recorded as an instance of drowsiness
                                  }
                                  if (numConsecutiveInstances == 3) {
                                    ap.start(); //Starts the alarm
                                    list.add(frame - 500); //Adds timestamp of drowsiness instance to array
                                 }
                              }
                              if (leftprob >= 0.7 && rightprob >= 0.7) {
                                 textView.setText("Open");
                                 numConsecutiveInstances = 0; /*The number of consecutive frames featuring closed eyes 
                                 resets to 0 when open eyes are detected*/
                                 if (lastInstance == true) {
                                    instancesClosed++; //The number/count of instances of drowsiness is increased by 1
                                    //after those frames have been recorded
                                    lastInstance = false;
                                 } else {
                                    lastInstance = false;
                                 }
                                 if (ap.isPlaying() == true) {
                                    ap.seekTo(0);
                                    ap.pause(); //If the subject's eyes open, the alarm stops
                                 }
                              }
                              System.out.println(leftprob + ", " + rightprob);
                              frame += 250;
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
            mp.setDataSource(fd); //Assigns file descriptor (mentioned earlier) to media player
            mp.setSurface(surface); /*Assigns media player to TextureView element which the video plays on
            (the media player merely allows the video to play and is not an actual element that shows up on the UI) */
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start(); //Starts playing the video
                    new CountDownTimer(mp.getDuration(), 250) { /*Sets a timer with intervals every 250 milliseconds
                    until the end of the video*/
                        public void onTick(long millisUntilFinished) {
                            b = tv.getBitmap(); //Gets a screenshot of the video in bitmap form
                            imageView.setImageBitmap(b); //Puts every screenshot taken on the ImageView display
                            detectFaces(b); //Passes each screenshot to the face detector
                        }
                        public void onFinish() {
                           
                        }
                    }.start(); //Starts timer
                   handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mp.start();
                            timerStarted = true;
                        }
                    }, 7000); //Video starts playing 7 seconds after application opens
                    System.out.println("START");
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    textview.setText(instancesClosed + " instances of drowsiness");
                    for (int i = 0; i <= list.size() - 1; i++) {
                        System.out.println(list.get(i) + ", ");
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
   /*The following 3 methods are abstract methods, which means that they are required to be put into the program
   otherwise it will return an error and the program will not run. They are left empty because they will not be used.*/

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

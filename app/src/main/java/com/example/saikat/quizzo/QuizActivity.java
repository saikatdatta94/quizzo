package com.example.saikat.quizzo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;


public class QuizActivity extends AppCompatActivity  implements BottomSnackbarClass.BottomSheetListener{



    private Dialog levelUpDialog;
    private boolean isAnyPopUpOn = false;

//    leader board Database constants
    private static final String USER_NAME = "userName";
    private static final String PROFILE_PHOTO_URL = "photoURL";
    private static final String USER_ID = "userId";
    private static final String SCORE = "score";


//    Animated logos
    private ImageView doubleChanceRotateImage;
    private ImageView fiftyFiftyPowerUpImage;
    private ImageView correctAnswerTickImage;

//    ANSWER LAYOUTS
    private LinearLayout answerLayout;

//    USer data variables
    private String userName;
    private String userId;
    private Uri photoUrl;
    private int scoreToWrite = 0;
    private int totalXp = 0;

    private int gem = 0;


//    Power ups
    private RelativeLayout correctAnsPowerUp;
    private RelativeLayout doubleChancePowerUp;
    private RelativeLayout fiftyFiftyPowerUp;


//    Receive Intent
    private String categoryName;
    private String categoryId;
    private String parentCategory;

    private int highScore;
    private int previousXp;
    DocumentReference userRef;

    private int levelToIncrement;
    private int residualXp;
    private int gamePlayBonus =100; //given only once ---- After 1 use reduce to 0
    private int correctXpStreak = 0; // needed to count and reset xp


//TODO:    is questions loaded from database

    private static final String TAG = "QUIZ_ACTIVITY";
    private ArrayList<Question> questionList = new ArrayList<Question>();




    private ProgressBar timerProgress ;
//    Question progressbar
    private ProgressBar questionProgressBar;

    private ObjectAnimator progressAnimator = ObjectAnimator.ofInt(timerProgress,"progress",100,0);
    private CountDownTimer countDownTimer;

    private int correctUntilLevel = 0;
    private int xp =0;

    private Timer timer;
    private int sec;

//    Stats Text view
    private TextView currentScoreView;
    private TextView timerText;
    private TextView highScoreTextView;
    private TextView xpTextView;
    private TextView gemNumberTextView;


    private TextView questionTextView;
    private ImageView questionImage;
    private Button b1;
    private Button b2;
    private Button b3;
    private Button b4;

//    category ,level, question no
    private String category;
    private int level =1; // TODO: get level from previous activity
    int solvedQuestionsInALevel = 0;
    private int questionRequestNo = 0;
    private int score = 0;

    private int qNo = 1;

//    database values
    private String questionText =" ";
    private String op1=" " ;
    private String op2=" " ;
    private String op3=" " ;
    private String op4 =" ";
    private int correctOption ;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference questionRef = db.collection("questions");
    private DocumentReference followingCatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Log.i(TAG, "onCreate: ");



//       Level Up dialog

        levelUpDialog = new Dialog(this);



//        Power Up views
        correctAnsPowerUp = findViewById(R.id.right_answer_power_up);
        correctAnsPowerUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctAnswerPowerUpUsed();
            }
        });

        doubleChancePowerUp = findViewById(R.id.double_chance_view);
        doubleChancePowerUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doubleChancePowerUpUsed();
            }
        });

        fiftyFiftyPowerUp = findViewById(R.id.fifty_fifty_layout);
        fiftyFiftyPowerUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fiftyFiftyPowerUpUsed();
            }
        });

//        find Animated logo ids

        doubleChanceRotateImage = findViewById(R.id.double_chance_logo);
        fiftyFiftyPowerUpImage = findViewById(R.id.fifty_fifty_icon);
        correctAnswerTickImage = findViewById(R.id.tick);


        currentScoreView = findViewById(R.id.current_score);

        questionTextView = findViewById(R.id.question);
        questionImage = findViewById(R.id.image_question);

        answerLayout = findViewById(R.id.options_layout);

//     TODO   6:10

        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b4 = findViewById(R.id.b4);

//     TODO   6:10





        highScoreTextView = findViewById(R.id.high_score);
        highScore = getIntent().getIntExtra("highScore",0);
        highScoreTextView.setText(String.valueOf(highScore));

        xpTextView = findViewById(R.id.xp);
        xpTextView.setText(String.valueOf(xp));

        gemNumberTextView = findViewById(R.id.gem_number);



        questionTextView.setVisibility(View.GONE);
        questionImage.setVisibility(View.GONE);
        b1.setVisibility(View.GONE);
        b2.setVisibility(View.GONE);
        b3.setVisibility(View.GONE);
        b4.setVisibility(View.GONE);



        timerText = findViewById(R.id.counter);
        timerProgress = findViewById(R.id.timer_progress);
        timerProgress.setMax(100);  //Setting the timer progressbar max to 100

        questionProgressBar = findViewById(R.id.questions_progressBar);
        questionProgressBar.setMax(5);



        BottomSnackbarClass popUp = new BottomSnackbarClass();
        popUp.show(getSupportFragmentManager(),"changeLater");


//     TODO   TRY CHANGING THESE INTO METHODS CALLED FROM XML attrs
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passButtonOfCorrectAnswer(b1,1);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passButtonOfCorrectAnswer(b2,2);
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passButtonOfCorrectAnswer(b3,3);
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passButtonOfCorrectAnswer(b4,4);
            }
        });



        getUserData();
        getGemNumberFromDatabase();
    }

    private void passButtonOfCorrectAnswer(Button button,int optionNumber) {
        stopTimer();
        disableAnswerButtons();
        disablePowerupButtons();


        if (isAnswerCorrect(optionNumber)){
//         TODO   button.setBackgroundColor(getResources().getColor(R.color.theme_green));
            ViewCompat.setBackgroundTintList(button, ContextCompat.getColorStateList(this,R.color.theme_green));
//            questionProgressBar.setProgress(++correctUntilLevel);

            //            answer correct increment score
            incrementScore();
            setXp();
            setHighScore();
            setQuestionProgressBar();
        }else {
//       TODO     button.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            ViewCompat.setBackgroundTintList(button, ContextCompat.getColorStateList(this,R.color.colorAccent));
            setCorrectOptionColor();
            writeHighScoreToProfile();
            final Handler wrongAnsHandler = new Handler();
            wrongAnsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openResultDialog();
                }
            },2000);
            return;

        }

        setCorrectOptionColor();

//        TODO : do not popup for next question when the level up or any other alert popup is there

        if (!isAnyPopUpOn){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    showBottomSheetDialog();
                }
            }, 2000);
        }





    }

    private void showBottomSheetDialog() {
        BottomSnackbarClass popUp = new BottomSnackbarClass();
        popUp.show(getSupportFragmentManager(),"changeLater");
    }

    private void setCorrectOptionColor() {
        switch (correctOption){
            case 1:
     //TODO           b1.setBackgroundColor(getResources().getColor(R.color.theme_green));
                ViewCompat.setBackgroundTintList(b1, ContextCompat.getColorStateList(this,R.color.theme_green));
                break;
            case 2:
        //TODO        b2.setBackgroundColor(getResources().getColor(R.color.theme_green));
                ViewCompat.setBackgroundTintList(b2, ContextCompat.getColorStateList(this,R.color.theme_green));
                break;
            case 3:
        //TODO        b3.setBackgroundColor(getResources().getColor(R.color.theme_green));
                ViewCompat.setBackgroundTintList(b3, ContextCompat.getColorStateList(this,R.color.theme_green));
                break;
            case 4:
       //TODO         b4.setBackgroundColor(getResources().getColor(R.color.theme_green));
                ViewCompat.setBackgroundTintList(b4, ContextCompat.getColorStateList(this,R.color.theme_green));
                break;
                default:
                    Log.i(TAG, "setCorrectOptionColor: Problem loading data");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "checkL: I was called");
        categoryName = getIntent().getStringExtra("catName");
        categoryId = getIntent().getStringExtra("catId");
        parentCategory = getIntent().getStringExtra("parent");
        previousXp = getIntent().getIntExtra("xp",0);
        level = getIntent().getIntExtra("level",1);
        Log.i(TAG, "checkLevel After each Q  Level Change Check: Level = "+level );
        questionRef
                .whereEqualTo("category",categoryName)
//                .whereEqualTo("level",level)
//                .orderBy("priority", Query.Direction.DESCENDING)   //TODO : LATER CHANGE IT TO LEVEL
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Question question = documentSnapshot.toObject(Question.class);
                            questionList.add(question);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });


    }

    //   TODO ********************************** BottomSheet passed text



    public void startTimer(){

        countDownTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
              timerText.setText(String.valueOf(millisUntilFinished/1000)+"''");
            }

            @Override
            public void onFinish() {
                Handler timeUpHandler = new Handler();
                timeUpHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openResultDialog();
                    }
                },2000);
            }
        };
        countDownTimer.start();
        if (Build.VERSION.SDK_INT >= 24) {
            sec = 100;
            timerProgress.setProgress(sec,true);
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (sec>=-1){
                            sec--;
                            timerProgress.setProgress(sec,true);
                    }
                    else {
                        timer.cancel();
                    }

                }
            },0,100);
        }
        else if (Build.VERSION.SDK_INT < 24) {


                    progressAnimator.setDuration(10000);
                    progressAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            timerProgress.setVisibility(View.GONE);
                        }
                    });

                    progressAnimator.start();
        }

    }

//    High score is updated each time if score is greater than highScore
    public void setHighScore(){
        if (score>highScore){
             highScore = score;
             highScoreTextView.setText(String.valueOf(highScore));
        }
    }

//    XP is updated after each correct answer
    public void setXp(){
        ++xp;
        checkLevel();
        xpTextView.setText(String.valueOf(xp*10));
    }


//    return true if the answer is correct or false otherwise
    public boolean isAnswerCorrect(int optionNo){
        if (optionNo == correctOption){
            return true;
        }
        return false;
    }

//    On backPressed show quit dialog
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Do you want to Quit?")
                .setMessage("")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(QuizActivity.this, "Quit", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(QuizActivity.this, "Stay", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    @Override
    public void onContinueClicked() {
//    Start the timer only if the player is ready to take the question
        questionTextView.setVisibility(View.VISIBLE);
        questionImage.setVisibility(View.VISIBLE);


        enableAnswerButtons();
        enablePowerupButtons();

        startTimer();

//        b1.setBackgroundColor(getResources().getColor(R.color.quizBtnDefault));
//    TODO    b1.setBackgroundColor(getResources().getColor(R.color.colorWhite));
//    TODO    b2.setBackgroundColor(getResources().getColor(R.color.colorWhite));
//     TODO   b3.setBackgroundColor(getResources().getColor(R.color.colorWhite));
//    TODO    b4.setBackgroundColor(getResources().getColor(R.color.colorWhite));



        questionTextView.setText(questionList.get(questionRequestNo).getQuestion());
//       TODO 6:12

//        b1.setText(questionList.get(questionRequestNo).getOption1());
//        b2.setText(questionList.get(questionRequestNo).getOption2());
//        b3.setText(questionList.get(questionRequestNo).getOption3());
//        b4.setText(questionList.get(questionRequestNo).getOption4());



        //       TODO 6:12


        correctOption = questionList.get(questionRequestNo).getCorrect();


//        TODO : SET IMAGE

//        TODO ------------****************CHECK VALIDITY LATER
//        if (questionList.get(questionRequestNo).getImgURL() != null){

        if (questionList.get(questionRequestNo).getIsImg().contentEquals("true")){

            Log.i(TAG, "onContinueClicked: TRUE"+questionList.get(questionRequestNo).getIsImg());

            answerLayout.setVisibility(View.GONE);
            answerLayout = findViewById(R.id.horizontal_options_layout);
            answerLayout.setVisibility(View.VISIBLE);
//            TODO--- SET HORIZONTAL ANSWER LAYOUT VISIBLE

//            TODO--- CHANGE B1/B2/B3/B4 ID CORRESPONDING
            b1 = findViewById(R.id.horizontal_btn_1);
            b2 = findViewById(R.id.horizontal_btn_2);
            b3 = findViewById(R.id.horizontal_btn_3);
            b4 = findViewById(R.id.horizontal_btn_4);

            Picasso.get().load(questionList.get(questionRequestNo).getImgURL()).into(questionImage);
            Log.i(TAG, "onContinueClicked: "+questionList.get(questionRequestNo).getImgURL());


//            CHANGE ButtonLayout
        }else {


                Log.i(TAG, "onContinueClicked: TRUE"+questionList.get(questionRequestNo).getIsImg());


            questionImage.setImageDrawable(null);
            answerLayout.setVisibility(View.GONE);
            answerLayout = findViewById(R.id.options_layout);
            answerLayout.setVisibility(View.VISIBLE);

            b1 = findViewById(R.id.b1);
            b2 = findViewById(R.id.b2);
            b3 = findViewById(R.id.b3);
            b4 = findViewById(R.id.b4);


            //            TODO--- THIS QUESTION HAS IMAGE SET VERTICAL ANSWER LAYOUT VISIBLE
            //            TODO--- CHANGE B1/B2/B3/B4 ID CORRESPONDING
        }
        ViewCompat.setBackgroundTintList(b1, ContextCompat.getColorStateList(this,R.color.colorWhite));
        ViewCompat.setBackgroundTintList(b2, ContextCompat.getColorStateList(this,R.color.colorWhite));
        ViewCompat.setBackgroundTintList(b3, ContextCompat.getColorStateList(this,R.color.colorWhite));
        ViewCompat.setBackgroundTintList(b4, ContextCompat.getColorStateList(this,R.color.colorWhite));

        b1.setVisibility(View.VISIBLE);
        b2.setVisibility(View.VISIBLE);
        b3.setVisibility(View.VISIBLE);
        b4.setVisibility(View.VISIBLE);

        b1.setText(questionList.get(questionRequestNo).getOption1());
        b2.setText(questionList.get(questionRequestNo).getOption2());
        b3.setText(questionList.get(questionRequestNo).getOption3());
        b4.setText(questionList.get(questionRequestNo).getOption4());


        qNo++;

//        increment for next question
        if (questionRequestNo<=4){
            questionRequestNo++;
        }else {
            questionRequestNo = 0;
        }


    }

    @Override
    public void onSurrenderClicked() {
//Send user to a non cancellable modal or a new activity to start again and to view stats
       writeHighScoreToProfile();
       openResultDialog();
    }



    public void openResultDialog(){
//        TODO write to leaderboard if score is higher
        writeToLeaderBoard();

        DialogFragment dialog = GameFinishPopUp.newInstance();
        Bundle args = new Bundle();
        args.putString("catId",categoryId);
        args.putString("catName",categoryName);
        args.putString("parent",parentCategory);
//        TODO send current score
//        TODO send highScore
        dialog.setArguments(args);

        ((GameFinishPopUp) dialog).setCallback(new GameFinishPopUp.Callback() {
            @Override
            public void onActionClick(String name) {

//                User used gem, now get him to next question
                showBottomSheetDialog();

//                TODO Subtract gem
//                TODO Increment score by 1
//                TODO Increment Horizontal progressbar by 1
//                TODO Increment XP and other things




            }
        });

        dialog.show(getSupportFragmentManager(),"tag");
    }

//    Set bottom modal components
    @Override
    public String setQuestionNo() {
//        return a string variable containing number of question

        return "Question "+ qNo;
    }

    @Override
    public String setSubCategoryName() {
        return categoryName;
    }

    @Override
    public String setParentCategoryName() {
        return "Somethung";
    }






//    TODO: PASS THESE DATA TO GAME FINISH POPUP AND UPDATE THERE
//    public void setHighScoreToUserProfile(){
//        final String followingItemsPath = "users/"+userId+"/Notebook/"+categoryId;
//        followingCatRef = db.document(followingItemsPath);
//
//        Map<String,Object> updatedFollowingCategory = new HashMap<>();
//        updatedFollowingCategory.put("highScore",highScore);
//        updatedFollowingCategory.put("xp",0);
//        followingCatRef.update(updatedFollowingCategory);
//    }




//    TODO:  MODIFY it will write to leaderboard if the score is high score or if the user is playing for the first time
    public void writeToLeaderBoard(){
        scoreToWrite = highScore;

        //   TODO:   If document already exists in the leaderboard collection in database then only
        //   TODO:   Update the highScore and XP

        Map<String,Object> leaderBoardObject = new HashMap<>();
        leaderBoardObject.put(USER_NAME,userName);
        leaderBoardObject.put(PROFILE_PHOTO_URL,photoUrl.toString());
        leaderBoardObject.put(USER_ID,userId);
        leaderBoardObject.put(SCORE,scoreToWrite);

//   TODO:     If user ID do not exists in leaderboard  increment the number of users in leaderboard by 1
//        DocumentReference docRef = db.collection("categoryItems").document(categoryId).collection("leaderBoard").document(userId);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
////                        Exists do not write anything
//                    } else {
////                        TODO: INCREMENT Number of users by 1
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });

        db.collection("categoryItems").document(categoryId).collection("leaderBoard").document(userId).
                set(leaderBoardObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuizActivity.this, "Failed to write data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUserData(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userName = user.getDisplayName();
        userId = user.getUid();
        photoUrl = user.getPhotoUrl();
    }


//    We have to check level after quit/wrong ans
//    TODO(Plan cancelled) Modify XP for higher level
//     e.g: for level 1 xp required to get into next level = 200
//     e.g: for level 2 xp required to get into next level = 300      ***********calculate (level*100 + 100)**************
//     e.g: for level 3 xp required to get into next level = 400
    public void checkLevel(){

        correctXpStreak++;

        totalXp = previousXp+(correctXpStreak*10)+gamePlayBonus;

        if (totalXp>=200){

            levelToIncrement = totalXp / 200;
            level = level+levelToIncrement; // Write this level to database
            residualXp = totalXp % 200;  // write this xp to database
            previousXp = residualXp;
            gamePlayBonus = 0;
            correctXpStreak = 0;
// TODO:   ***********************************
//            TODO XP is more than 200 i.e Level goes up by 1 Show a popup

            showLevelUpDialog(level);

//              TODO : LOAD new level Questions


        }else {
            residualXp = totalXp;
        }
        Log.i(TAG, "checkLevel After each Q    Level"+ level +" levelToIncrement: "+levelToIncrement+" :prevXp: "+previousXp+ " xp: "+xp+" streak: " + correctXpStreak +" totalXp : "+totalXp+" residualXp "+residualXp+" gamePlayBon:" +gamePlayBonus);
    }



    //    Write highScore to profile at the end of the game(When the answer is wrong or on Surrender clicked
    public void writeHighScoreToProfile(){
        final String followingItemsPath = "users/"+userId+"/Notebook/"+categoryId;
        DocumentReference followingCatDocRef = db.document(followingItemsPath);


        followingCatDocRef.update("xp",residualXp,
                "level",level)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                          correctXpStreak = 0;
                          previousXp = residualXp;
                          gamePlayBonus = 0;

                        Log.i(TAG, "checkLevel Ons: Level: "+level + "correctXpStreak:"+ correctXpStreak+" previousXp "+ previousXp+" gamePlayBonus: "+gamePlayBonus);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        if (highScore>=score){

            followingCatDocRef.update("highScore",highScore)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "onSuccess: Written Highscore :" + highScore);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "onFailure: Couldn't write score");
                }
            });
        }
    }

    public void setQuestionProgressBar(){
        questionProgressBar.setProgress(score%5);
        if (score%5==0){
            questionProgressBar.setProgress(5);

            final Handler restoreHandler = new Handler();
            restoreHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //            showAnimation
                    questionProgressBar.setProgress(0);
                }
            },2000);
        }



        if (score%5 == 0){

//                TODO  PROGRESSBAR FILLED SHOW SOME ACHIEVEMENT

        }
    }


    private void correctAnswerPowerUpUsed(){

        disablePowerupButtons();

        if (gem>=5){
            gem = gem - 5;
            gemNumberTextView.setText(String.valueOf(gem));
            doIfAnswerIsCorrect();
            userRef.update("gems",gem).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(TAG, "onComplete: Gems Updated");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "onFailure: Failed to update gem");
                }
            });

        }else {
            Log.i(TAG, "correctAnswerPowerUpUsed: Not enough Gem");
        }
        if (gem<5){
//            TODO: TURN THE BUTTON AREA INTO GREY
//            ImageView powerGem = findViewById(R.id.correct_answer_gem);
//            ImageView tick = findViewById(R.id.tick);
//            ColorMatrix matrix = new ColorMatrix();
//            matrix.setSaturation(0);
//
//            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
//            powerGem.setColorFilter(filter);
//            tick.setColorFilter(filter);


            Toast.makeText(this, "You ran out of Gems", Toast.LENGTH_LONG).show();

//            TODO: POP BUY OPTION
        }


    }

    public void fiftyFiftyPowerUpUsed(){
        disablePowerupButtons();


//TODO: Handle spark exhaust conditions
        fiftyFiftyPowerUpImage.animate().rotation(20).setDuration(64);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fiftyFiftyPowerUpImage.animate().rotation(-40).setDuration(64);
            }
        },64);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fiftyFiftyPowerUpImage.animate().rotation(0).setDuration(64);
            }
        },64);



//        DISABLE 2 wrong answers
        if(correctOption == 1){
            //Disable random between 2,3,4
            greyOutWrongOptions(2,3,4);
        }else if(correctOption == 2){
            //Disable random between 1,3,4
            greyOutWrongOptions(1,3,4);
        }else if(correctOption == 3){
            //Disable random between 1,2,4
            greyOutWrongOptions(1,2,4);
        }else if(correctOption == 4){
            //Disable random between 1,2,3
            greyOutWrongOptions(1,2,3);
        }
    }

    public void doubleChancePowerUpUsed(){
        //        Rotate the icon
        Log.i(TAG, "doubleChancePowerUpUsed: I was used");
        doubleChanceRotateImage.animate().rotationBy(360).start();

    }

    public void greyOutWrongOptions(int a,int b,int c){
        int randNo =(int) (Math.random() * 3);

//        Assign random between a,b,c to zero so 2 randomly will be disabled
        if (randNo ==0){
            a = 0;
        }else if (randNo ==1){
            b = 0;
        }else if (randNo == 2){
            c = 0;
        }


        if (a==1 || b==1 || c ==1){
            ViewCompat.setBackgroundTintList(b1, ContextCompat.getColorStateList(this,R.color.inactiveGrey));
            b1.setClickable(false);
        }
        if(a==2 || b==2 || c == 2){
            ViewCompat.setBackgroundTintList(b2, ContextCompat.getColorStateList(this,R.color.inactiveGrey));
            b2.setClickable(false);
        }
        if(a==3 || b==3 || c == 3){
            ViewCompat.setBackgroundTintList(b3, ContextCompat.getColorStateList(this,R.color.inactiveGrey));
            b3.setClickable(false);
        }
        if (a==4 || b==4 || c == 4){
            ViewCompat.setBackgroundTintList(b4, ContextCompat.getColorStateList(this,R.color.inactiveGrey));
            b4.setClickable(false);
        }
    }


//    Get the gem number  from database
    private void getGemNumberFromDatabase(){
        userRef = db.collection("users").document(userId);
        Log.i(TAG, "onFailure: Failed");
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                      gem = Integer.parseInt(String.valueOf(document.get("gems"))) ;
                        Log.i(TAG, "onComplete: Gems: "+gem);
                      gemNumberTextView.setText(String.valueOf(gem));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: Failed");
            }
        });
    }

    public void doIfAnswerIsCorrect(){
        stopTimer();
        disableAnswerButtons();
        incrementScore();
        setXp();
        setHighScore();
        setQuestionProgressBar();
        setCorrectOptionColor();     // Just set the correct option color to green

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//              don't  Show bottomSheetDialog if some popup is there
if (!isAnyPopUpOn)  {
    showBottomSheetDialog();
}

            }
        },2000);
    }



    public void stopTimer(){
        progressAnimator.end();
        timer.cancel();
        countDownTimer.cancel();
    }


    public void incrementScore(){
        score++;
        currentScoreView.setText(String.valueOf(score));
    }

//    Put answer buttons to non-clickable state
    public void disableAnswerButtons(){
        b1.setClickable(false);
        b2.setClickable(false);
        b3.setClickable(false);
        b4.setClickable(false);
    }

//    Set answer buttons to clickable
    public void enableAnswerButtons(){
        b1.setClickable(true);
        b2.setClickable(true);
        b3.setClickable(true);
        b4.setClickable(true);
    }

    public void disablePowerupButtons(){
        correctAnsPowerUp.setClickable(false);
        doubleChancePowerUp.setClickable(false);
        fiftyFiftyPowerUp.setClickable(false);

    }

    public void enablePowerupButtons(){
        correctAnsPowerUp.setClickable(true);
        doubleChancePowerUp.setClickable(true);
        fiftyFiftyPowerUp.setClickable(true);
    }



    public void b1Clicked(View view) {
        Log.i(TAG, "b1Clicked: b1 clicked");
        passButtonOfCorrectAnswer(b1,1);
    }

    public void b2Clicked(View view) {
        Log.i(TAG, "b2Clicked: b2 clicked");
        passButtonOfCorrectAnswer(b2,2);
    }

    public void b3Clicked(View view) {
        Log.i(TAG, "b3Clicked: b3 clicked");
        passButtonOfCorrectAnswer(b3,3);
    }

    public void b4Clicked(View view) {
        Log.i(TAG, "b4Clicked: b4 clicked");
        passButtonOfCorrectAnswer(b4,4);
    }


    private void showLevelUpDialog(int level) {
//        TODO set new level to Dialog text view
         levelUpDialog.setContentView(R.layout.layout_level_up_dialog);
    levelUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    levelUpDialog.show();
    isAnyPopUpOn = true;
    levelUpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.i(TAG, "onDismiss: Dismissed");
            isAnyPopUpOn = false;

//            User dismissed the popup now show next question option

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    showBottomSheetDialog();
                }
            },2000);
        }
    });
    }
}

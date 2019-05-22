package com.example.saikat.quizzo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.white.progressview.CircleProgressView;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CategoryDetailActivity extends AppCompatActivity {
    private static final String TAG = "CAT_DETAIL_ACTIVITY";
    //TODO:DELETE this later
    private TextView followersTextView;
    private DocumentReference followingCatRef;

    private FirebaseUser user;

//    Get category id
    private String categoryId="";
    private String userId="";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference catRef;
    private TextView headingView;
    private TextView descriptionView;
    private String followState="null";
    private Button playBtn;
    private Button followButton;

private CircleProgressView circleProgressView;

    private int followers;

//    Received intent
    private String title;
    private String description;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

//TODO followersTextView tracks number of followers
        followersTextView = findViewById(R.id.followers);

        followButton = findViewById(R.id.foll_button);

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFollowButtonClicked();
            }
        });



//        Toolbar
        final Toolbar toolbar = findViewById(R.id.question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = getIntent().getStringExtra("title");
        description = getIntent().getStringExtra("description");

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle(title);


        final LinearLayout heading_description = findViewById(R.id.heading_desc);
        TransitionManager.beginDelayedTransition(heading_description);

        circleProgressView = findViewById(R.id.circle_progress_normal);


//     add Animation to circular progressbar
        if (Build.VERSION.SDK_INT >= 24) {

            final int xp = 60;// TODO Get player xp from database
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 2000ms
                    circleProgressView.setProgressInTime(0,xp,1000);
                }
            }, 512);

        }
        else if (Build.VERSION.SDK_INT < 24) {


//            TODO code for animation for build version < 24

//            progressAnimator.setDuration(10000);
//            progressAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    timerProgress.setVisibility(View.GONE);
//                }
//            });
//
//            progressAnimator.start();

//            private ObjectAnimator progressAnimator = ObjectAnimator.ofInt(timerProgress,"progress",100,0);
        }

        headingView = findViewById(R.id.head);
        headingView.setText(title);
        descriptionView = findViewById(R.id.description);
        descriptionView.setText(description);
        playBtn = findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClicked(v);
            }
        });

        //        Make expanded Toolbar title invisible
         final AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
        final AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;



        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1){
                    scrollRange = appBarLayout.getTotalScrollRange();

                }
                if (scrollRange+verticalOffset == 0){
                    headingView.startAnimation(fadeOut);
                    descriptionView.startAnimation(fadeOut);
                    fadeOut.setDuration(128);
                    fadeOut.setFillAfter(true);


//                    headingView.setVisibility(View.GONE);
//                    descriptionView.setVisibility(View.GONE);
                    isShow = true;
                }else if (isShow){
                    collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
                    headingView.startAnimation(fadeIn);
                    descriptionView.startAnimation(fadeIn);
                    fadeIn.setDuration(128);
                    fadeIn.setFillAfter(true);
//                    headingView.animate().translationYBy(32).setDuration(1000);
//                    headingView.setVisibility(View.VISIBLE);
//                    descriptionView.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });



        getDataFromFirebase();

//        Get userId
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        categoryId = getIntent().getStringExtra("id");
        checkIfFollowing();

    }

    private void onFollowButtonClicked() {
        DocumentReference categoryRef = db.document(path);
        Map<String,Object> category = new HashMap<>();

//     TODO   db.collection("categoryItems/")


//        TODO if not following create a following class inside user

        if(followState.matches("true") ){  //if follow State if found true
            followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_circle_outline_black_24dp,0,0,0);
            followButton.setText("Follow");
//            getDataFromFirebase();
            setFollowStateInDatabase("false");  //change state to false
//            TODO currently do not decrement followers
//            category.put("followers",--followers);
//            categoryRef.update(category);

        }else if (followState.matches("false")){
            followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_remove_circle_black_24dp,0,0,0);
            followButton.setText("Following");
            setFollowStateInDatabase("true");
            getDataFromFirebase();
            category.put("followers",++followers);
            categoryRef.update(category);

        }

    }

    private void setFollowButtonState(){
        Log.i(TAG, "setFollowButtonState: "+ followState);
        if(followState.matches("true") ){
//            If followState is found true set the button as following
            followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_remove_circle_black_24dp,0,0,0);
            followButton.setText("Following");
        }else if (followState.matches("false")){
            followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_circle_outline_black_24dp,0,0,0);
            followButton.setText("Follow");
        }
    }

    private void setFollowStateInDatabase(String b) {

//                     TODO:   If doesn't exist create
//                        DocumentReference followingCategoryRef = db.document(followingItemsPath);
        Map<String,Object> followingCategory = new HashMap<>();
        followingCategory.put("isFollowing",b);


        db.collection("users").document(userId).collection("Notebook").document(categoryId)
                .update(followingCategory)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: Updated");
                         checkIfFollowing();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        Log.i(TAG, "onComplete: Doesn't Exists Create");
    }

    private String checkIfFollowing() {
        final String followingItemsPath = "users/"+userId+"/Notebook/"+categoryId;

        followingCatRef = db.document(followingItemsPath);

        followingCatRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot followingCatDoc = task.getResult();
                    if (followingCatDoc.exists()){
                        FollowingCategoryItemClass followingCatObject = followingCatDoc.toObject(FollowingCategoryItemClass.class);
                        //                        TODO Check if already following
                        followState = followingCatObject.getIsFollowing();

//                        TODO  !!!!!!!---------Warning------------!!!!!!!! as the date 18-5-2019 boolean inside a document is always is returning as false so we will use string instead for is following
                        setFollowButtonState();
                        Log.i(TAG, "check If following: State:"+ followState);
                        Log.i(TAG, "onComplete: title:"+ followingCatObject.getTitle());
                        Log.i(TAG, "onComplete: descr:"+ followingCatObject.getDescription());
                    }else {
//                     TODO:   If doesn't exist create
//                        DocumentReference followingCategoryRef = db.document(followingItemsPath);
                        Map<String,Object> followingCategory = new HashMap<>();
                        followingCategory.put("isFollowing","false");


                        db.collection("users").document(userId).collection("Notebook").document(categoryId)
                                .set(followingCategory)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        followState = "false";
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                        Log.i(TAG, "onComplete: Doesn't Exists Create");
                    }
                }else {
                    Log.i(TAG, "onComplete: Not successFul");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });


        return followState;



    }


    public void playClicked(View v){

        Intent intent = new Intent(CategoryDetailActivity.this,QuizActivity.class);
        intent.putExtra("catName",title);
        startActivity(intent);
    }

    public void getDataFromFirebase(){
        path = getIntent().getStringExtra("path");

        catRef = db.document(path);
        catRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    DocumentSnapshot categoryDoc = task.getResult();

                    if (categoryDoc.exists()){
                        CategoryItemClass catObject = categoryDoc.toObject(CategoryItemClass.class);
//                        TODO No. of followers
                        categoryId = categoryDoc.getId(); //TODO recieve from intent
                        followers = catObject.getFollowers();
                        followersTextView.setText(String.valueOf(catObject.getFollowers()));

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void onBackPressed(){
        Toast.makeText(this, "BackPressed", Toast.LENGTH_SHORT).show();
        finish();
    }


}

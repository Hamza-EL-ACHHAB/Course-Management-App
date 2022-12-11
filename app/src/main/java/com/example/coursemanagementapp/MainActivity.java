 package com.example.coursemanagementapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

 public class MainActivity extends AppCompatActivity implements CourseRVAdapter.CourseClickInterface {
     private FloatingActionButton addCourseFAB;
     FirebaseDatabase firebaseDatabase;
     DatabaseReference databaseReference;
     private RecyclerView courseRV;
     private FirebaseAuth mAuth;
     private ProgressBar loadingPB;
     private ArrayList<CourseRVModal> courseRVModalArrayList;
     private CourseRVAdapter courseRVAdapter;
     private RelativeLayout bottomSheetRL;

     @SuppressLint("MissingInflatedId")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         courseRV = findViewById(R.id.idRVCourses);
         loadingPB = findViewById(R.id.idPBLoading);
         addCourseFAB = findViewById(R.id.idFABAddCourse);
         firebaseDatabase = FirebaseDatabase.getInstance();
         mAuth = FirebaseAuth.getInstance();
         bottomSheetRL = findViewById(R.id.idRLBSheet);
         courseRVModalArrayList = new ArrayList<>();

         databaseReference = firebaseDatabase.getReference("Courses");
         courseRVAdapter = new CourseRVAdapter(courseRVModalArrayList, this, this);
         courseRV.setLayoutManager(new LinearLayoutManager(this));
         courseRV.setAdapter(courseRVAdapter);
         addCourseFAB.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i = new Intent(MainActivity.this, AddCourseActivity.class);
                 startActivity(i);

             }
         });
         courseRVAdapter = new CourseRVAdapter(courseRVModalArrayList, this, this::onCourseClick);
         courseRV.setLayoutManager(new LinearLayoutManager(this));
         courseRV.setAdapter(courseRVAdapter);
         getCourses();
     }

     private void getCourses() {
         // on below line clearing our list.
         courseRVModalArrayList.clear();
         // on below line we are calling add child event listener method to read the data.
         databaseReference.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                 // on below line we are hiding our progress bar.
                 loadingPB.setVisibility(View.GONE);
                 // adding snapshot to our array list on below line.
                 courseRVModalArrayList.add(snapshot.getValue(CourseRVModal.class));
                 // notifying our adapter that data has changed.
                 courseRVAdapter.notifyDataSetChanged();
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                 // this method is called when new child is added
                 // we are notifying our adapter and making progress bar
                 // visibility as gone.
                 loadingPB.setVisibility(View.GONE);
                 courseRVAdapter.notifyDataSetChanged();
             }

             @Override
             public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                 // notifying our adapter when child is removed.
                 courseRVAdapter.notifyDataSetChanged();
                 loadingPB.setVisibility(View.GONE);
             }

             @Override
             public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                 // notifying our adapter when child is moved.
                 courseRVAdapter.notifyDataSetChanged();
                 loadingPB.setVisibility(View.GONE);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
     }

     @Override
     public void onCourseClick(int position) {
         displayBottomSheet(courseRVModalArrayList.get(position));
     }

     private void displayBottomSheet(CourseRVModal modal) {
         // on below line we are creating our bottom sheet dialog.
         final BottomSheetDialog bottomSheetTeachersDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
         // on below line we are inflating our layout file for our bottom sheet.
         View layout = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_dialog, bottomSheetRL);
         // setting content view for bottom sheet on below line.
         bottomSheetTeachersDialog.setContentView(layout);
         // on below line we are setting a cancelable
         bottomSheetTeachersDialog.setCancelable(false);
         bottomSheetTeachersDialog.setCanceledOnTouchOutside(true);
         // calling a method to display our bottom sheet.
         bottomSheetTeachersDialog.show();
         // on below line we are creating variables for
         // our text view and image view inside bottom sheet
         // and initialing them with their ids.
         TextView courseNameTV = layout.findViewById(R.id.idTVCourseName);
         TextView courseDescTV = layout.findViewById(R.id.idTVCourseDesc);
         TextView suitedForTV = layout.findViewById(R.id.idTVSuitedFor);
         TextView priceTV = layout.findViewById(R.id.idTVCoursePrice);
         ImageView courseIV = layout.findViewById(R.id.idIVCourse);
         // on below line we are setting data to different views on below line.
         courseNameTV.setText(modal.getCourseName());
         courseDescTV.setText(modal.getCourseDescription());
         suitedForTV.setText("Suited for " + modal.getBestSuitedFor());
         priceTV.setText("Rs." + modal.getCoursePrice());
         Picasso.get().load(modal.getCourseImg()).into(courseIV);
         Button viewBtn = layout.findViewById(R.id.idBtnVIewDetails);
         Button editBtn = layout.findViewById(R.id.idBtnEditCourse);

         // adding on click listener for our edit button.
         editBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // on below line we are opening our EditCourseActivity on below line.
                 Intent i = new Intent(MainActivity.this, EditCourseActivity.class);
                 // on below line we are passing our course modal
                 i.putExtra("course", modal);
                 startActivity(i);
             }
         });
         // adding click listener for our view button on below line.
         viewBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // on below line we are navigating to browser
                 // for displaying course details from its url
                 Intent i = new Intent(Intent.ACTION_VIEW);
                 i.setData(Uri.parse(modal.getCourseLink()));
                 startActivity(i);
             }
         });
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         int id = item.getItemId();
         switch (id) {
             case R.id.idLogOut:
                 // displaying a toast message on user logged out inside on click.
                 Toast.makeText(this, "User Logged Out", Toast.LENGTH_SHORT).show();
                 // on below line we are signing  out our user.
                 mAuth.signOut();
                 // on below line we are opening our login activity.
                 Intent i = new Intent(MainActivity.this, LoginActivity.class);
                 startActivity(i);
                 this.finish();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
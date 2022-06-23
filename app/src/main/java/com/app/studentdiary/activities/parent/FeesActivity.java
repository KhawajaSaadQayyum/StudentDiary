package com.app.studentdiary.activities.parent;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.studentdiary.R;
import com.app.studentdiary.adapters.TypeRecyclerViewAdapter;
import com.app.studentdiary.info.Info;
import com.app.studentdiary.info.RvType;
import com.app.studentdiary.models.FeesPojo;
import com.app.studentdiary.models.Super;
import com.app.studentdiary.utils.DialogUtils;
import com.app.studentdiary.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeesActivity extends AppCompatActivity implements Info {

    RecyclerView rvAttendance;
    TypeRecyclerViewAdapter typeRecyclerViewAdapter;
    Dialog loadingDialog;


    List<Super> superList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fees);


        loadingDialog = new Dialog(this);

        DialogUtils.initLoadingDialog(loadingDialog);
        initRv();
        initData();

    }

    private void initData() {

        FirebaseDatabase.getInstance().getReference()
                .child(NODE_FEES)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        superList.clear();
                        for (DataSnapshot child : snapshot.getChildren())
                            for (DataSnapshot grandChild : child.getChildren())
                                for (DataSnapshot greatGrandChild : grandChild.getChildren()) {
                                    FeesPojo activityPojo = greatGrandChild.getValue(FeesPojo.class);
                                    if (activityPojo == null)
                                        continue;

                                    if (!activityPojo.getStudentID().equals(Utils.getCurrentUserId()))
                                        continue;

                                    superList.add(activityPojo);
                                }

                        typeRecyclerViewAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initRv() {
        rvAttendance = findViewById(R.id.rv_regs);
        superList = new ArrayList<>();
        typeRecyclerViewAdapter
                = new TypeRecyclerViewAdapter(this, superList, RvType.RV_TYPE_FEES_PARENT);
        rvAttendance.setAdapter(typeRecyclerViewAdapter);
    }

    public void back(View view) {
        finish();
    }

}
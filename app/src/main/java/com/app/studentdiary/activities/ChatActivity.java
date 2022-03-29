package com.app.studentdiary.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.studentdiary.R;
import com.app.studentdiary.adapters.TypeRecyclerViewAdapter;
import com.app.studentdiary.info.Info;
import com.app.studentdiary.models.ActivityPojo;
import com.app.studentdiary.models.MessagePojo;
import com.app.studentdiary.models.Super;
import com.app.studentdiary.models.UserModel;
import com.app.studentdiary.singletons.ActivitySingleton;
import com.app.studentdiary.singletons.CommentSingleton;
import com.app.studentdiary.utils.DialogUtils;
import com.app.studentdiary.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements Info {

    EditText etMessage;
    String strEtMessage;
    UserModel teacher;
    Dialog loadingDialog;
    List<Super> superList;
    UserModel parent;
    TextView tvUsername;
    RecyclerView recyclerView;
    TypeRecyclerViewAdapter typeRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        superList = new ArrayList<>();
        loadingDialog = new Dialog(this);
        DialogUtils.initLoadingDialog(loadingDialog);
        etMessage = findViewById(R.id.messageEditText);
        tvUsername = findViewById(R.id.tv_username);
        initRv();
        initUsers();
    }

    private void initRv() {
        recyclerView = findViewById(R.id.recyclerview);
        superList = new ArrayList<>();
        typeRecyclerViewAdapter
                = new TypeRecyclerViewAdapter(this, superList, Info.RV_TYPE_CHATS);
        recyclerView.setAdapter(typeRecyclerViewAdapter);
    }

    private void getChatHistory() {
        if (!loadingDialog.isShowing())
            loadingDialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child(NODE_CHATS)
                .child(Utils.userModel.getClassroom())
                .child(parent.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loadingDialog.dismiss();
                        superList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            MessagePojo messagePojo = child.getValue(MessagePojo.class);
                            superList.add(messagePojo);
                        }
                        Log.i(TAG, "onDataChange: " + superList);
                        typeRecyclerViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initUsers() {
        loadingDialog.show();
        if (Utils.userModel.getType().equals(TEACHER)) {
            teacher = Utils.userModel;
            FirebaseDatabase.getInstance().getReference()
                    .child(NODE_USERS)
                    .child(CommentSingleton.getInstance().getParentId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            loadingDialog.dismiss();
                            parent = snapshot.getValue(UserModel.class);
                            getChatHistory();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            return;
        }
        parent = Utils.userModel;
        FirebaseDatabase.getInstance().getReference()
                .child(NODE_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loadingDialog.dismiss();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            UserModel userModel = child.getValue(UserModel.class);
                            if (userModel != null && userModel.getClassroom().equals(Utils.userModel.getClassroom())
                                    && userModel.getType().equals(TEACHER)) {
                                teacher = userModel;
                                String te = teacher.getFirstName() + " " + teacher.getLastName();
                                tvUsername.setText(te);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        getChatHistory();
    }

    public void back(View view) {
        finish();
    }

    public void sendMessage(View view) {
        castStrings();
        if (strEtMessage.isEmpty())
            return;

        etMessage.clearFocus();
        etMessage.setText("");

        String id = String.valueOf(superList.size());

        ActivityPojo activityPojo = ActivitySingleton.getInstance();
        String activityId = "";
        String activityTitle = "";
        if (activityPojo != null) {
            activityId = activityPojo.getId();
            activityTitle = activityPojo.getTitle();
        }

        String authorId = parent.getId();
        if (Utils.userModel.getType().equals(TEACHER)) {
            authorId = teacher.getId();
        }

        MessagePojo messagePojo = new MessagePojo(id,
                "" + activityId,
                "" + activityTitle,
                "" + strEtMessage,
                "" + parent.getId(),
                "" + parent.getFirstName(),
                "" + teacher.getId(),
                "" + teacher.getFirstName(),
                "" + authorId);

        loadingDialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child(NODE_CHATS)
                .child(Utils.userModel.getClassroom())
                .child(parent.getId())
                .child(id)
                .setValue(messagePojo).
                addOnCompleteListener(task -> loadingDialog.dismiss());
    }

    private void castStrings() {
        strEtMessage = etMessage.getText().toString();
    }
}
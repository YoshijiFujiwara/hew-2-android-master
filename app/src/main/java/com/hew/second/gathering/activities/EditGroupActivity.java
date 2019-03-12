package com.hew.second.gathering.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.GroupUser;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.fragments.EventFinishFragment;
import com.hew.second.gathering.hotpepper.Budget;
import com.hew.second.gathering.views.adapters.GroupMemberAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import icepick.State;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class EditGroupActivity extends BaseActivity {

    @State
    public Group group = null;
    private GroupMemberAdapter adapter = null;
    private boolean newGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        setTitle("グループ編集");

        Intent beforeIntent = getIntent();
        if (beforeIntent != null) {
            Bundle bundle = beforeIntent.getExtras();
            if (bundle != null) {
                group = Parcels.unwrap(getIntent().getParcelableExtra("GROUP_DETAIL"));
                newGroup = bundle.getBoolean("NEW_GROUP", false);
            }
        }
        if (group == null) {
            finish();
        }

        FloatingActionButton fab = findViewById(R.id.fab_addUserToGroup);
        fab.setOnClickListener((v) -> {
            Intent intent = new Intent(getApplication(), AddGroupMemberActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("GROUP_DETAIL", Parcels.wrap(group));
            intent.putExtras(bundle);
            startActivityForResult(intent, INTENT_ADD_GROUP_MEMBER);
        });
        TextView groupName = findViewById(R.id.group_name);
        groupName.setText(group.name);
        groupName.setOnClickListener((l) -> {
            new MaterialDialog.Builder(this)
                    .title("グループ名")
                    .content("タイトル")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .inputRangeRes(1, 30, R.color.colorAccentDark)
                    .input("グループ名", groupName.getText(), (MaterialDialog dialog, CharSequence input) -> {
                        saveGroupName(input.toString());
                    })
                    .negativeText("キャンセル")
                    .show();
        });

        Button save = findViewById(R.id.save_button);
        save.setOnClickListener((l) -> {
            finish();
        });

        GridView gridView = findViewById(R.id.gridView_group);
        gridView.setEmptyView(findViewById(R.id.emptyView_edit_group));
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            switch (view.getId()) {
                case R.id.delete_group_member:
                    new MaterialDialog.Builder(this)

                            .title(groupName.getText())
                            .content(adapter.getList().get(position).username + "をグループから削除しますか？")
                            .positiveText("OK")
                            .onPositive((dialog, which) -> {
                                deleteGroupMember(adapter.getList().get(position).id);
                            })
                            .negativeText("キャンセル")
                            .show();
                    break;
            }
        });
        updateList(group);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (newGroup && adapter.getList().isEmpty()) {
            deleteGroup();
        } else {
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void fetchList() {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        ApiService service = Util.getService();
        Observable<GroupDetail> token = service.getGroupDetail(group.id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            group = list.data;
                            updateList(list.data);
                            dialog.dismiss();
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent2 = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent2);
                                }
                            }
                        }
                ));
    }

    private void updateList(Group gdi) {
        // ListView生成
        GridView gridView = findViewById(R.id.gridView_group);
        ArrayList<GroupUser> ar = new ArrayList<>(gdi.users);
        adapter = new GroupMemberAdapter(ar);
        if (gridView != null) {
            // ListViewにadapterをセット
            gridView.setAdapter(adapter);
        }
    }

    public void saveGroupName(String name) {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", name);
        Observable<GroupDetail> token = service.updateGroupName(group.id, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            group = list.data;
                            TextView groupName = findViewById(R.id.group_name);
                            groupName.setText(list.data.name);
                            dialog.dismiss();
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "グループ名を更新しました。", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.BLACK);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    private void deleteGroupMember(int id) {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        ApiService service = Util.getService();
        Completable token = service.deleteGroupUser(group.id, id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            dialog.dismiss();
                            fetchList();
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "友達を削除しました。", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.BLACK);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();

                        }, // 終了時
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (!cd.isDisposed() && throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void deleteGroup() {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        ApiService service = Util.getService();
        Completable token = service.deleteGroup(group.id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            group = null;
                            dialog.dismiss();
                            finish();
                        }, // 終了時
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "このグループを使用しているテンプレートがあるので、削除できません。", Snackbar.LENGTH_LONG);
                                    snackbar.getView().setBackgroundColor(Color.BLACK);
                                    snackbar.setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                } else if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //店検索から戻ってきた場合
            case (INTENT_ADD_GROUP_MEMBER):
                Group temp = Parcels.unwrap(data.getParcelableExtra("GROUP_DETAIL"));
                if (temp != null) {
                    this.group = temp;
                }
                updateList(group);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}

package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.R;

//
public class HistoryFragment extends Fragment {

    public HistoryFragment() {
    }

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history,container,false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        updateSessionList();
    }

//    public void updateSessionList() {
//
//        ApiService service = Util.getService();
//        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
//
//        token.subscribeOn(Schedulers.io())
//                .flatMap(result -> {
//                    LoginUser.setToken(result.access_token);
//                    return service.getSessionList(LoginUser.getToken());
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .unsubscribeOn(Schedulers.io())
//                .subscribe(
//                        list -> {
////                          表示
//                            updateList(list.data);
//
//                        },  // 成功時
//                        throwable -> {
//                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
//
//
//                        }
//                );
//    }
//
//    public void updateList(List<Session> data) {
//
//        ListView listView = getActivity().findViewById(R.id.listView_in_progress);
//
//        ArrayList<Session> sessionArrayList = new ArrayList<>();
//
//        for (Session sl : data) {
//            sessionArrayList.add(sl);
//        }
//
//        SessionAdapter adapter = new SessionAdapter(sessionArrayList);
//        listView.setAdapter(adapter);
//
//    }
}

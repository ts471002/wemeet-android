package com.example.wemeet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wemeet.pojo.Bug;
import com.example.wemeet.pojo.BugInterface;
import com.example.wemeet.util.NetworkUtil;
import com.example.wemeet.util.ReturnVO;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.WindowManager.LayoutParams;

public class ShowVirusActivity extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view;
        view = inflater.inflate(R.layout.activity_bug_content,container,false);
        Bundle bundle = getArguments();
        assert bundle !=null;
        Bug bug = (Bug) bundle.getSerializable("bug");
        assert bug != null;
        if (bug.getBugProperty().getBugContent().getType() == 4) {
            switch (bug.getVirusPoint().getStatus()){
                case 1:
                    ((TextView)view.findViewById(R.id.level)).setText("症状虫子");
                    break;
                case 2:
                    ((TextView)view.findViewById(R.id.level)).setText("疑似虫子");
                    break;
                case 3:
                    ((TextView)view.findViewById(R.id.level)).setText("确诊虫子");
                    break;
            }
            ((TextView)view.findViewById(R.id.symptoms)).append("："+bug.getVirusPoint().getSymptoms());
            if(bug.getVirusPoint().getDiseaseStartTime()!=null){
                ((TextView)view.findViewById(R.id.symptoms_start_time)).append("："+bug.getVirusPoint().getDiseaseStartTime().toString());
            }
            else{
                ((TextView)view.findViewById(R.id.symptoms_start_time)).append("：无");
            }
            ((TextView)view.findViewById(R.id.note)).append("："+bug.getVirusPoint().getDescription());
        }

        ImageView close = view.findViewById(R.id.close_button);
        close.setOnClickListener(v -> dismiss());

        Button changeLevel = view.findViewById(R.id.change_level);
        int role = bundle.getInt("role");
        if(role == 1){
            changeLevel.setVisibility(View.VISIBLE);
        }else{
            changeLevel.setVisibility(View.GONE);
        }

        changeLevel.setOnClickListener(view1 -> {
            bundle.putSerializable("virusPoint",bug.getVirusPoint());
            ChangeLevelActivity changeLevelActivity = new ChangeLevelActivity();
            changeLevelActivity.setArguments(bundle);
            assert getFragmentManager() != null;
            changeLevelActivity.show(getFragmentManager(),"changeLevel");
            dismiss();
        });

        Button deleteVirus = view.findViewById((R.id.delete_virus));
        if(role == 1){
            deleteVirus.setVisibility(View.VISIBLE);
        }else{
            if(bug.getVirusPoint().getStatus() == 1){
                deleteVirus.setVisibility(View.VISIBLE);
            }else
                deleteVirus.setVisibility(View.GONE);
        }

        deleteVirus.setOnClickListener(view1 -> {
            AlertDialog.Builder check = new AlertDialog.Builder(getActivity());
            check.setTitle("提示！")
                    .setMessage("确认删除？")
                    .setPositiveButton("确认",(dialog, witch) -> {
                        dialog.cancel();
                        NetworkUtil.getRetrofit().create(BugInterface.class)
                                .deleteBug(bug.getBugProperty().getBugID())
                                .enqueue(new Callback<ReturnVO>() {
                                    @Override
                                    public void onResponse(@NonNull Call<ReturnVO> call, @NonNull Response<ReturnVO> response) {
                                        Toast.makeText(getActivity(), "刪除成功", Toast.LENGTH_SHORT).show();
                                        reloadMap();
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<ReturnVO> call, @NonNull Throwable t) {
                                        Toast.makeText(getActivity(), "刪除失敗", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    })
                    .setNegativeButton("取消",(dialog, witch) -> dialog.dismiss()).create().show();

        });
        return view;
    }
    @Override
    public void onResume() {
        LayoutParams params = Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.WRAP_CONTENT;
        Objects.requireNonNull(getDialog().getWindow()).setAttributes(params);
        super.onResume();
    }

    private void reloadMap(){
        MainActivity mainActivity = (MainActivity)getActivity();
        assert mainActivity != null;
        mainActivity.aMap.clear();
        mainActivity.showAroundBugs(MainActivity.myLon, MainActivity.myLat, MainActivity.range);
    }
}

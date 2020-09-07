package com.example.wemeet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wemeet.pojo.BugInterface;
import com.example.wemeet.pojo.BugProperty;
import com.example.wemeet.pojo.VirusPoint;
import com.example.wemeet.util.NetworkUtil;
import com.example.wemeet.util.ReturnVO;

import java.io.File;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeLevelActivity extends DialogFragment {
    private TextView filePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.change_virus_level, container, false);
        Bundle bundle = getArguments();
        assert bundle != null;
        VirusPoint virusPoint = (VirusPoint) bundle.getSerializable("virusPoint");
        BugProperty bugProperty = (BugProperty) bundle.getSerializable("bug");

        ImageView close = view.findViewById(R.id.close_button);
        close.setOnClickListener(v -> {
            dismiss();
            ShowVirusActivity showVirusActivity = new ShowVirusActivity();
            //            bundle.putSerializable("bug",bug);
            showVirusActivity.setArguments(bundle);
            assert getFragmentManager() != null;
            showVirusActivity.show(getFragmentManager(), "virus");
        });

        Spinner changeLevel = view.findViewById(R.id.level_change);
        assert virusPoint != null;
        changeLevel.setSelection(virusPoint.getStatus() - 1);

        filePath = view.findViewById(R.id.filepath);

        Button filePost = view.findViewById(R.id.file_selected);
        filePost.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//无类型限制
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        });

        Button submitChangeLevel = view.findViewById(R.id.submit_change_level);

        submitChangeLevel.setOnClickListener(view1 -> {
            // 保存 status 到数据库
            assert bugProperty != null;
            int toLevel = changeLevel.getSelectedItemPosition() + 1;
            Long bugID = bugProperty.getBugID();
            ((VirusPoint) bugProperty.getBugContent()).setStatus(toLevel);

            //如要变更等级 需上传文件
            if (!filePath.getText().toString().equals("文件路径")) {
                File file = new File(filePath.getText().toString());
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part bodyFile = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                NetworkUtil.getRetrofit().create(BugInterface.class)
                        .uploadCredential(bugID, bodyFile)
                        .enqueue(new Callback<ReturnVO>() {
                            @Override
                            public void onResponse(@NonNull Call<ReturnVO> call, @NonNull Response<ReturnVO> response) {

                            }

                            @Override
                            public void onFailure(@NonNull Call<ReturnVO> call, @NonNull Throwable t) {

                            }
                        });
                NetworkUtil.getRetrofit().create(BugInterface.class)
                        .updateBug(bugID, bugProperty)
                        .enqueue(new Callback<ReturnVO>() {
                            @Override
                            public void onResponse(@NonNull Call<ReturnVO> call, @NonNull Response<ReturnVO> response) {

                                dismiss();
                                reloadMap();
                                Toast.makeText(getActivity(), "更改成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<ReturnVO> call, @NonNull Throwable t) {
                                t.printStackTrace();
                            }
                        });
            } else {
                Toast.makeText(getActivity(), "请选择文件", Toast.LENGTH_SHORT).show();
            }
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

    private void reloadMap() {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainActivity.aMap.clear();
        mainActivity.showAroundBugs(MainActivity.myLon, MainActivity.myLat, MainActivity.range);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            assert uri != null;
            String path;
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
                filePath.setText(path);
                //                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
                return;
            }
            //4.4以后
            path = getPath(getContext(), uri);
            filePath.setText(path);
            //            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    private String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}

package io.flutter.plugins.webviewflutter;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.database.Cursor;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;
import android.Manifest;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

public class newActivity extends Activity {
    private static ValueCallback<Uri[]> mUploadMessageArray;

    public static void getfilePathCallback(ValueCallback<Uri[]> filePathCallback) {
        mUploadMessageArray = filePathCallback;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        showBottomDialog();

    }

    private void openAblum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//任意类型文件
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//        intent.setType("*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    private void openCamera() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //系统常量， 启动相机的关键
        startActivityForResult(openCameraIntent, 2); // 参数常量为自定义的request code, 在取返回结果时有用
    }

    private void showBottomDialog() {
        //1、使用Dialog、设置style
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        //2、设置布局
        View view = View.inflate(this, R.layout.dialog_custom_layout, null);
        dialog.setContentView(view);
        //点击其他空白处，退出dialog。
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //这样可以使返回值为null。
                onActivityResult(1, 1, null);
            }
        });
        Window window = dialog.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        //设置弹出动画
        window.setWindowAnimations(R.style.main_menu_animStyle);
        //设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.show();

        dialog.findViewById(R.id.tv_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                openCamera();
            }
        });

        dialog.findViewById(R.id.tv_take_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&ContextCompat.checkSelfPermission(newActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(newActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10086);
                } else {
                    openAblum();
                }

            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                onActivityResult(1, 1, null);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10086) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAblum();
            } else {
                Toast.makeText(newActivity.this, "读写权限已拒绝，建议打开可查看相册", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //防止退出时，data没有数据，导致闪退。
        Log.i("TAG", "forResult");
        if (data != null) {
            Uri uri = data.getData();
            Log.i("TAG", "! " + data.getClass() + " * " + data);
            Log.i("TAG", "URi " + uri);

            if (uri == null) {
                //好像时部分机型会出现的问题，我的mix3就遇到了。
                //拍照返回的时候uri为空，但是data里有inline-data。
                Log.i("TAG", String.valueOf(data));
                Bundle bundle = data.getExtras();
                try {
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
                    Uri[] results = new Uri[]{uri};
                    mUploadMessageArray.onReceiveValue(results);
                } catch (Exception e) {
                    //当不拍照返回相机时，获取到uri也没数据。
                    mUploadMessageArray.onReceiveValue(null);
                }
            } else {

//                try {
//                    String[] proj = { MediaStore.Images.Media.DATA };
//                    Cursor actualimagecursor = managedQuery(uri,proj,null,null,null);
//                    int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                    actualimagecursor.moveToFirst();
//
//                    String img_path = actualimagecursor.getString(actual_image_column_index);
//                    Uri uriPath = Uri.parse(img_path);
//                    Uri[] results = new Uri[]{uriPath};
//                    Log.i("TAG","URi "+uriPath);
//                    mUploadMessageArray.onReceiveValue(results);
//                    actualimagecursor.close();
//                } catch (java.lang.Exception e) {
//                    e.printStackTrace();
//                }

                Uri[] results = new Uri[]{uri};
                mUploadMessageArray.onReceiveValue(results);
            }

        } else {
            Log.i("TAG", "onReceveValue");
            mUploadMessageArray.onReceiveValue(null);
        }
        finish();
    }


}

package io.flutter.plugins.webviewflutter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.util.List;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class newActivity extends Activity {
    private static ValueCallback<Uri[]> mUploadMessageArray;

    private String camera_path = Environment.getExternalStorageDirectory().toString() + "/Photo_YH/";//照片保存位置
    private String camera_photo_name;// 保存的名称

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
        Intent intent = new Intent(Intent.ACTION_PICK);//任意类型文件
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//        intent.setType("*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    private void openCamera() {

        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //系统常量， 启动相机的关键
        File path = new File(camera_path);
        if (!path.exists()) {
            path.mkdirs();
        }

        camera_photo_name = System.currentTimeMillis() + ".jpg";
        File file = new File(path, camera_photo_name);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

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

                XXPermissions.with(newActivity.this)
                        // 申请单个权限
                        .permission(Permission.CAMERA)
                        .request(new OnPermissionCallback() {

                            @Override
                            public void onGranted(List<String> permissions, boolean all) {
                                if (!all) {
                                    Toast.makeText(newActivity.this, "获取部分权限成功，但部分权限未正常授予", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                openCamera();
                            }

                            @Override
                            public void onDenied(List<String> permissions, boolean never) {
                                if (never) {
                                    Toast.makeText(newActivity.this, "被永久拒绝授权，请手动授予拍照权限", Toast.LENGTH_SHORT).show();
                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                    XXPermissions.startPermissionActivity(newActivity.this, permissions);
                                } else {
                                    Toast.makeText(newActivity.this, "获取拍照权限失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        dialog.findViewById(R.id.tv_take_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&ContextCompat.checkSelfPermission(newActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(newActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10086);

                XXPermissions.with(newActivity.this)
                        // 申请单个权限
                        .permission(Permission.WRITE_EXTERNAL_STORAGE)
                        // 申请多个权限
                        .permission(Permission.READ_EXTERNAL_STORAGE)
                        // 设置权限请求拦截器（局部设置）
                        //.interceptor(new PermissionInterceptor())
                        // 设置不触发错误检测机制（局部设置）
                        //.unchecked()
                        .request(new OnPermissionCallback() {

                            @Override
                            public void onGranted(List<String> permissions, boolean all) {
                                if (!all) {
                                    Toast.makeText(newActivity.this, "获取部分权限成功，但部分权限未正常授予", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                openAblum();
                            }

                            @Override
                            public void onDenied(List<String> permissions, boolean never) {
                                if (never) {
                                    Toast.makeText(newActivity.this, "被永久拒绝授权，请手动授予读写权限", Toast.LENGTH_SHORT).show();
                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                    XXPermissions.startPermissionActivity(newActivity.this, permissions);
                                } else {
                                    Toast.makeText(newActivity.this, "获取读写权限失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
//                } else {
//                }


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

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == 10086) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openAblum();
//            } else {
//                Toast.makeText(newActivity.this, "读写权限已拒绝，建议打开可查看相册", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //防止退出时，data没有数据，导致闪退。
        Log.i("TAG", "forResult");
        if (requestCode == 2) {
            String mFilePath = camera_path + "/" + camera_photo_name;
            String path = getCacheDir().getAbsolutePath();

            Luban.with(this)
                    .load(mFilePath)
                    .ignoreBy(600)
                    .setTargetDir(path)
                    .filter(new CompressionPredicate() {
                        @Override
                        public boolean apply(String path) {
                            return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                        }
                    })
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onSuccess(File file) {
                            // 获取输入流
                            Uri[] results = new Uri[]{Uri.fromFile(file)};
                            mUploadMessageArray.onReceiveValue(results);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i("TAG", e.toString());
                        }
                    }).launch();
        } else {
            if (data != null) {
                Uri uri = data.getData();
                Log.i("TAG", "! " + data.getClass() + " * " + data);
                Log.i("TAG", "URi " + uri);

                if (uri == null) {
                    //好像时部分机型会出现的问题，我的mix3就遇到了。
                    //拍照返回的时候uri为空，但是data里有inline-data。
                    Log.i("TAG", String.valueOf(data));

                } else {

                    Uri[] results = new Uri[]{uri};
                    mUploadMessageArray.onReceiveValue(results);
                }

            } else {
                Log.i("TAG", "onReceveValue");
                mUploadMessageArray.onReceiveValue(null);
            }
        }


        finish();
    }


}

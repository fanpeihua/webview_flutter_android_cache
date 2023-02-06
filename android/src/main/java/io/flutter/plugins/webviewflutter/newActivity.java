package io.flutter.plugins.webviewflutter;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.List;

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
        PictureSelector.create(newActivity.this)
                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(3)// 每行显示个数
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                .isPreviewImage(true)// 是否可预览图片
                .isCamera(false)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .isEnableCrop(false)// 是否裁剪
                .isCompress(true)// 是否压缩
                .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .rotateEnabled(false) // 裁剪是否可旋转图片
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    private void openCamera() {
        PictureSelector.create(newActivity.this)
                .openCamera(PictureMimeType.ofImage())
                .isCompress(true)// 是否压缩
                .isCamera(true)
                .forResult(PictureConfig.REQUEST_CAMERA);//结果回调onActivityResult code
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //防止退出时，data没有数据，导致闪退。
//        Log.i("TAG", "forResult");
//        if (requestCode == 2) {
//
//                            // 获取输入流
//                            Uri[] results = new Uri[]{Uri.fromFile(file)};
//                            mUploadMessageArray.onReceiveValue(results);
//
//        } else {
//            if (data != null) {
//                Uri uri = data.getData();
//                Log.i("TAG", "! " + data.getClass() + " * " + data);
//                Log.i("TAG", "URi " + uri);
//
//                if (uri == null) {
//                    //好像时部分机型会出现的问题，我的mix3就遇到了。
//                    //拍照返回的时候uri为空，但是data里有inline-data。
//                    Log.i("TAG", String.valueOf(data));
//
//                } else {
//
//                    Uri[] results = new Uri[]{uri};
//                    mUploadMessageArray.onReceiveValue(results);
//                }
//
//            } else {
//                Log.i("TAG", "onReceveValue");
//                mUploadMessageArray.onReceiveValue(null);
//            }
//        }
//
//
//        finish();
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case PictureConfig.CHOOSE_REQUEST:
                        if (data != null) {
                            List<LocalMedia> localMedia = PictureSelector.obtainMultipleResult(data);
                            LocalMedia media = localMedia.get(0);
                            Uri[] results = new Uri[]{Uri.fromFile(new File(media.getCompressPath()))};
                            mUploadMessageArray.onReceiveValue(results);
                        }
                        break;
                    case PictureConfig.REQUEST_CAMERA:
                        if (data != null) {
                            List<LocalMedia> localMedia = PictureSelector.obtainMultipleResult(data);
                            if (localMedia != null && localMedia.size() > 0) {
                                LocalMedia media = localMedia.get(0);
                                Uri[] results = new Uri[]{Uri.fromFile(new File(media.getCompressPath()))};
                                mUploadMessageArray.onReceiveValue(results);
                            }
                        }
                        break;

                }
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

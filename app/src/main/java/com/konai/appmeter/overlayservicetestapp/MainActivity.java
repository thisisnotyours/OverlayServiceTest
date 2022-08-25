
package com.konai.appmeter.overlayservicetestapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private static final String logTag = "MainActivity";
    public static WindowService windowService = null;
    public static WindowService.ServiceBinder binder;
    private Context mContext;



    //서비스클래스 연결
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.d(logTag, "window service connected");
            binder = (WindowService.ServiceBinder) iBinder;
            windowService = binder.getService();


            if (windowService == null) {
                Log.d(logTag, "window service null");
            }else {
                Log.d(logTag, "window service not null"); //y

                //다른앱위에 그리기 아이콘 띄우기
                windowService._overlaycarstate();

                if (windowService != null) {
                    windowService._showOverlay(false);
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(logTag, "window service disconnected");
            windowService = null;
        }
    };



    //서비스 시작
    void startWindowService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (windowService == null) {
                bindService(new Intent(getApplicationContext()
                                , WindowService.class)
                        , mServiceConnection
                        , Context.BIND_AUTO_CREATE);
            }
            startForegroundService(new Intent(MainActivity.this, WindowService.class));
        }else {
            Log.d(logTag+"_overlay","startService");
            _start_service();
        }
    }

    //26버전보다 높을 경우
    public void _start_service() {
        if (windowService == null) {
            bindService(new Intent(getApplicationContext()
                            , WindowService.class)
                    , mServiceConnection
                    , Context.BIND_AUTO_CREATE);
        }

        Intent service = new Intent(getApplicationContext(), WindowService.class);
//        service.setPackage("com.konai.appmeter.driver");  //패키지 필요없음?

        if (Build.VERSION.SDK_INT >= 26) {
            getApplicationContext().startForegroundService(service);
        }else {
            Log.d(logTag+"_overlay","startService below api 26 ");
            startService(service);
        }

        if (windowService == null) {
            bindService(new Intent(getApplicationContext()
                            , WindowService.class)
                    , mServiceConnection
                    , Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() { //os 화면에 pause 되어있는 앱리스트 삭제시(onDestroy) 실행
        super.onDestroy();
        Log.d("overlayViewTest","main_Destroyed");
        windowService.removeOverlayView();
    }

    @Override
    protected void onPause() {   //앱 밖에서
        if (windowService != null) {
            windowService._showOverlay(true); //overlayView 앱 밖 화면에서 보이기
        }
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //다른앱위에그리기 권한요청
        overlayPermission();
    }


    @Override
    protected void onResume() {
        super.onResume();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (windowService != null) {
            windowService._showOverlay(false);  //앱안에서 overlayView 숨김
        }else {
            Log.d(logTag+"_overlay", "null");
        }
    }




    //다른앱위에 그리기
    public void overlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("다른화면위에 그리기라는 권한이 필요합니다")
                        .setCancelable(false)
                        .setPositiveButton("확인"
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                                                , Uri.parse("package:" + getPackageName()));
                                        i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                        startActivityForResult(i, 100); //권한요청
                                    }
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else {
                startWindowService();
            }
        }else {
            startWindowService();
        }
    }



    //다른앱위에 그리기 권한 result
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (requestCode) {
                case 100:
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        finish();
                    } else {
                        startWindowService();
                    }
                    break;
            }
        }
    }





}
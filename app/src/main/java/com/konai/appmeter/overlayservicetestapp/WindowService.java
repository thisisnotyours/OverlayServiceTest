package com.konai.appmeter.overlayservicetestapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class WindowService extends Service {
    private LocationManager locationManager;
    private final IBinder m_ServiceBinder = new ServiceBinder();
    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams m_Params;
    private LinearLayout show_btn_layout, drag_move_layout;
    private ImageView increaseView, decreaseView, homeView;
    public DisplayMetrics matrix;
    private int max_x = -1
            , max_y = -1
            , prev_x
            , prev_y;
    private float start_x, start_y;
    private boolean moveV = false;


    public class ServiceBinder extends Binder {

        public WindowService getService() {
            return WindowService.this;
        }
    }

    public void _showOverlay(boolean show) {
        if (overlayView == null) {
            return;
        }
        if (show == true) {
            overlayView.setVisibility(View.VISIBLE);
        }else {
            Log.d("_showOverlay", show+"");
            overlayView.setVisibility(View.INVISIBLE);
        }
    }

    public void removeOverlayView() {
        if (overlayView == null) {
            return;
        }else {
            windowManager.removeView(overlayView);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //서비스 종료
        }
        if (windowManager != null) {
            if (overlayView != null) {
                windowManager.removeView(overlayView);
                overlayView = null;
            }
            windowManager = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return m_ServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForegroundService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "appmeter";
            final String TITLE = "AM100";

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);

            if (channel == null) {
                channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_ID + " " + TITLE,
                        NotificationManager.IMPORTANCE_LOW);

                channel.setSound(null, null);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).build();
            startForeground(1, notification);

        }else {

        }
    }//startForegroundService..


    public void _overlaycarstate() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //원도우매니저 설정
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //다른앱위에 그리기뷰 aka 미니바
        overlayView = (View) inflater.inflate(R.layout.layout_overlay_2, null);
        overlayView.setVisibility(View.INVISIBLE);

        homeView = (ImageView) overlayView.findViewById(R.id.iv_home);
        show_btn_layout = (LinearLayout) overlayView.findViewById(R.id.show_btn_layout);
        drag_move_layout = (LinearLayout) overlayView.findViewById(R.id.layout_drag_move);
        increaseView = (ImageView) overlayView.findViewById(R.id.iv_increase_overlayview);
        decreaseView = (ImageView) overlayView.findViewById(R.id.iv_decrease_overlayview);

        homeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainActivity();
            }
        });

        show_btn_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        show_btn_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("show_btn_layout","show_btn_layout touch");
                        if (max_x == -1) {
                            matrix = new DisplayMetrics();
                            windowManager.getDefaultDisplay().getMetrics(matrix);
                            max_x = matrix.widthPixels - overlayView.getWidth();
                            max_y = matrix.heightPixels - overlayView.getHeight();
                        }
                        moveV = false;
                        start_x = event.getRawX();
                        start_y = event.getRawY();
                        prev_x = m_Params.x;
                        prev_y = m_Params.y;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        moveV = true;
                        int x = (int) (event.getRawX() - start_x);
                        int y = (int) (event.getRawY() - start_y);
                        final int num = 10;
                        if ((x > -num && x < num) && (y > -num && y < num)) {
                            moveV = false;
                        }else {
                            m_Params.x = prev_x - x;
                            m_Params.y = prev_y + y;
                            windowManager.updateViewLayout(overlayView, m_Params);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });


        //(+)버튼
        increaseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showMainActivity();
                Display display = windowManager.getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);

                ViewGroup.LayoutParams m_Params = show_btn_layout.getLayoutParams();
                int new_width = m_Params.width;
                int new_height = m_Params.height;

                new_width = m_Params.width + (int) Math.floor(new_width * 0.05);
                new_height = m_Params.height + (int) Math.floor(new_height * 0.05);

                int display_width = size.x;
                int display_height = size.y;

                if (new_width < display_width && new_height < display_height) {
                    m_Params.width = new_width;
                    m_Params.height = new_height;

                    overlayView.setVisibility(View.VISIBLE);
                    overlayView.setLayoutParams(m_Params);
//                    windowManager.updateViewLayout(overlayView, m_Params);
                }
                Log.d( "_plus", m_Params.width + ", " + m_Params.height);
            }
        });

        //(-)버튼
        decreaseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showMainActivity();
                Toast.makeText(getApplicationContext(), "click view", Toast.LENGTH_SHORT).show();

                Display display = windowManager.getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);

                ViewGroup.LayoutParams m_Params = show_btn_layout.getLayoutParams();
                int new_width = m_Params.width;
                int new_height = m_Params.height;

                new_width = m_Params.width - (int) Math.floor(new_width * 0.05);
                new_height = m_Params.height - (int) Math.floor(new_height * 0.05);

                int display_width = size.x;
                int display_height = size.y;

                if (new_width < display_width && new_height < display_height) {
                    m_Params.width = new_width;
                    m_Params.height = new_height;

                    overlayView.setVisibility(View.VISIBLE);
                    overlayView.setLayoutParams(m_Params);
//                    windowManager.updateViewLayout(overlayView, m_Params);
                }

                Log.d( "_plus", m_Params.width + ", " + m_Params.height);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            m_Params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
        } else {
            m_Params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
        }
        //위치지정
        m_Params.gravity = Gravity.RIGHT | Gravity.CENTER;

        windowManager.addView(overlayView, m_Params);  //이코드를 써야 뷰가 보임
    }


    //메인화면으로 이동
    private void showMainActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

}

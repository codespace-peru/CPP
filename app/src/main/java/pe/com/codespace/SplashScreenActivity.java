package pe.com.codespace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends Activity {

    private static final long SPLASH_SCREEN_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);

        TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Intent intent = new Intent().setClass(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        };
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}

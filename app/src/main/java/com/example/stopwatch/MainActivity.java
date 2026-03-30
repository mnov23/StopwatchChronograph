package com.example.stopwatch;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // ── Direct ports of JS variables ──────────────────────────────────────
    private TextView tvHour, tvMin, tvSec;
    private TextView btnStartStop, btnReset;

    private int     liNum           = 75;
    private boolean flip            = false;
    private int     intervalCounter = 0;
    private long    currentTime     = 0;
    private boolean stop            = true;          // matches JS: var stop = true

    private static final int INTERVAL = 10;         // matches JS: var interval = 10
    private static final int TICK_COUNT = 101;      // 0-100 inclusive

    private final Handler  handler      = new Handler(Looper.getMainLooper());
    private       Runnable timerRunnable;

    // Visual tick views (analogous to JS clockLines / $('#clockline li'))
    private View[] tickViews;

    // Clock-hand rotation (analogous to JS icnClockLineDeg)
    private View    clockHand;
    private float   clockHandDeg = 180f;

    // ──────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvHour       = findViewById(R.id.tv_hour);
        tvMin        = findViewById(R.id.tv_min);
        tvSec        = findViewById(R.id.tv_sec);
        btnStartStop = findViewById(R.id.btn_start_stop);
        btnReset     = findViewById(R.id.btn_reset);
        clockHand    = findViewById(R.id.clock_hand);

        buildTickGrid();

        // matches JS: btnStartStop.on('click', sWatchMethod.startAndStop)
        btnStartStop.setOnClickListener(v -> startAndStop());
        // matches JS: btnReset.on('click', sWatchMethod.reset)
        btnReset.setOnClickListener(v -> reset());

        updateResetAlpha();
    }

    // ── sWatchMethod.timer() ──────────────────────────────────────────────
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                intervalCounter += INTERVAL;

                if (!stop) {
                    // Update display every 1000 ms  (matches JS: if((intervalCounter%1000)==0))
                    if (intervalCounter % 1000 == 0) {
                        currentTime += 1000;

                        long appendHour   = currentTime / (1000 * 60 * 60);
                        long appendMinute = currentTime % (1000 * 60 * 60) / (1000 * 60);
                        long appendSecond = currentTime % (1000 * 60) / 1000;

                        tvHour.setText(appendHour   < 10 ? "0" + appendHour   : String.valueOf(appendHour));
                        tvMin .setText(appendMinute < 10 ? "0" + appendMinute : String.valueOf(appendMinute));
                        tvSec .setText(appendSecond < 10 ? "0" + appendSecond : String.valueOf(appendSecond));
                    }

                    // ── Tick-ring animation ──────────────────────────────
                    // matches JS: var target = $('#clockline li').eq(liNum)
                    if (liNum < tickViews.length && tickViews[liNum] != null) {
                        int color = flip
                                ? ContextCompat.getColor(MainActivity.this, R.color.tick_off)
                                : ContextCompat.getColor(MainActivity.this, R.color.tick_on);
                        tickViews[liNum].setBackgroundColor(color);
                    }

                    liNum += 1;
                    if (liNum > 100) liNum = 0;          // matches JS: if(liNum>100){ liNum=0; }
                    if (liNum == 75)  flip = !flip;      // matches JS: if(liNum==75){ flip=!flip; }

                    // Rotate clock hand continuously
                    clockHandDeg += 1.8f;                // 360° over ~200 ticks (2 seconds)
                    clockHand.setRotation(clockHandDeg);
                }

                handler.postDelayed(this, INTERVAL);
            }
        };
        handler.postDelayed(timerRunnable, INTERVAL);
    }

    // ── sWatchMethod.startAndStop() ───────────────────────────────────────
    private void startAndStop() {
        // Button press animation (matches JS: addClass('sw-click') then remove after 200ms)
        btnStartStop.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100)
                .withEndAction(() ->
                        btnStartStop.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();

        stop = !stop;

        if (!stop) {
            btnStartStop.setText("STOP");
            btnStartStop.setBackgroundResource(R.drawable.btn_stop);
            // matches JS: if(!intervalCounter){ sWatchMethod.timer(); }
            if (intervalCounter == 0) {
                startTimer();
            }
        } else {
            btnStartStop.setText("START");
            btnStartStop.setBackgroundResource(R.drawable.btn_start);
        }

        updateResetAlpha();
    }

    // ── sWatchMethod.reset() ──────────────────────────────────────────────
    private void reset() {
        if (!stop) {
            stop = true;
            btnStartStop.setText("START");
            btnStartStop.setBackgroundResource(R.drawable.btn_start);
        }

        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        if (intervalCounter != 0) {
            // matches JS: reset all state vars
            currentTime     = 0;
            intervalCounter = 0;
            liNum           = 75;
            flip            = false;
            clockHandDeg    = 180f;
            clockHand.setRotation(clockHandDeg);

            tvHour.setText("00");
            tvMin .setText("00");
            tvSec .setText("00");

            // matches JS: reset all clockline li backgrounds to #fff
            for (View v : tickViews) {
                if (v != null)
                    v.setBackgroundColor(ContextCompat.getColor(this, R.color.tick_off));
            }

            // Reset button animation (matches JS: addClass('br-click'))
            btnReset.animate().scaleX(0.88f).scaleY(0.88f).setDuration(100)
                    .withEndAction(() ->
                            btnReset.animate().scaleX(1f).scaleY(1f).setDuration(400).start()
                    ).start();
        }

        updateResetAlpha();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Replicate JS: btnReset.css('opacity', 1 or 0.5) */
    private void updateResetAlpha() {
        btnReset.setAlpha(intervalCounter > 0 || !stop ? 1f : 0.5f);
    }

    /** Build the 101-item tick ring (analogous to $('#clockline li')) */
    private void buildTickGrid() {
        GridLayout grid = findViewById(R.id.tick_ring);
        tickViews = new View[TICK_COUNT];
        int offColor = ContextCompat.getColor(this, R.color.tick_off);
        int sizePx   = dpToPx(6);
        int marginPx = dpToPx(2);

        for (int i = 0; i < TICK_COUNT; i++) {
            View tick = new View(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width  = sizePx;
            lp.height = sizePx;
            lp.setMargins(marginPx, marginPx, marginPx, marginPx);
            tick.setLayoutParams(lp);
            tick.setBackgroundColor(offColor);
            tick.setTag(i);
            grid.addView(tick);
            tickViews[i] = tick;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
    }
}

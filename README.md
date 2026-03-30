# Stopwatch — Native Android (Java)
Ported from the jQuery/JS reference source.

## JS → Java mapping

| JS concept | Java equivalent |
|---|---|
| `var stop = true` | `boolean stop = true` field in Activity |
| `var intervalCounter = 0` | `int intervalCounter = 0` field |
| `var currentTime = 0` | `long currentTime = 0` |
| `var liNum = 75` | `int liNum = 75` |
| `var flip = false` | `boolean flip = false` |
| `setInterval(fn, 10)` | `Handler.postDelayed(runnable, 10)` |
| `clearInterval(time)` | `handler.removeCallbacks(timerRunnable)` |
| `$('#hr').html(...)` | `tvHour.setText(...)` |
| `$('#clockline li').eq(i).css('background',...)` | `tickViews[i].setBackgroundColor(...)` |
| `btnStartStop.on('click', fn)` | `btnStartStop.setOnClickListener(v -> startAndStop())` |
| `btnReset.on('click', fn)` | `btnReset.setOnClickListener(v -> reset())` |
| `$(el).css('opacity', 0.5/1)` | `view.setAlpha(0.5f / 1f)` |
| `.addClass('sw-click')` / remove after 200ms | `animate().scaleX(0.92f)` → restore |
| `.addClass('br-click')` | `animate().scaleX(0.88f)` → restore |

## Project structure
```
app/src/main/
  java/com/example/stopwatch/
    MainActivity.java       ← all logic (sWatchMethod ported here)
  res/
    layout/activity_main.xml
    values/colors.xml       ← tick_on #339dac, tick_off, accent
    values/strings.xml
    values/themes.xml
    drawable/btn_start.xml
    drawable/btn_stop.xml
    drawable/btn_reset.xml
    drawable/circle_dot.xml
  AndroidManifest.xml
```

## How to open
1. Open Android Studio → **Open** → select the `StopwatchApp` folder
2. Let Gradle sync
3. Run on emulator or physical device (minSdk 21 / Android 5.0+)

## Key behaviour preserved from JS source
- Timer ticks every **10 ms** (`INTERVAL = 10`) — same as JS
- Display updates only on `intervalCounter % 1000 == 0` — same as JS
- `liNum` starts at 75, wraps at 100 back to 0 — same as JS
- `flip` toggles when `liNum == 75` — same as JS
- Reset only executes if `intervalCounter != 0` — same as JS guard
- Reset button opacity 0.5 until started, then 1.0 — same as JS

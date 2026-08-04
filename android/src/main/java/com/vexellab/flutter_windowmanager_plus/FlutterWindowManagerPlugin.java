package com.vexellab.flutter_windowmanager_plus;

import android.app.Activity;
import android.os.Build;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class FlutterWindowManagerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private MethodChannel channel;
  private Activity activity;

  /**
   * Flags requested by Dart that are currently meant to be set on the Activity window.
   *
   * <p>Android drops every WindowManager.LayoutParams flag when the Activity is recreated (screen
   * rotation, multi-window resize, locale change, "don't keep activities", ...). The new Activity
   * gets a brand new Window, so flags applied to the previous one — most importantly FLAG_SECURE —
   * are silently lost. Tracking them here lets us re-apply them as soon as we are handed the new
   * Activity.
   */
  private int appliedFlags = 0;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_windowmanager_plus");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    channel = null;
  }

  @SuppressWarnings("deprecation")
  private boolean validLayoutParam(int flag) {
    switch (flag) {
      case WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON:
      case WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM:
      case WindowManager.LayoutParams.FLAG_DIM_BEHIND:
      case WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN:
      case WindowManager.LayoutParams.FLAG_FULLSCREEN:
      case WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED:
      case WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES:
      case WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON:
      case WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR:
      case WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN:
      case WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS:
      case WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE:
      case WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE:
      case WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL:
      case WindowManager.LayoutParams.FLAG_SCALED:
      case WindowManager.LayoutParams.FLAG_SECURE:
      case WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER:
      case WindowManager.LayoutParams.FLAG_SPLIT_TOUCH:
      case WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH:
      // The flags below are deprecated on current API levels, but Window#addFlags still accepts
      // them — they become a no-op rather than an error, so we must not reject them.
      case WindowManager.LayoutParams.FLAG_BLUR_BEHIND:
      case WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD:
      case WindowManager.LayoutParams.FLAG_DITHER:
      case WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED:
      case WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING:
      case WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON:
        return true;
      // The flags below genuinely do not exist below the given API level.
      case WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN:
        return (Build.VERSION.SDK_INT >= 18);
      case WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE:
      case WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION:
      case WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS:
        return (Build.VERSION.SDK_INT >= 19);
      case WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS:
        return (Build.VERSION.SDK_INT >= 21);
      case WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR:
        return (Build.VERSION.SDK_INT >= 22);
      default:
        return false;
    }
  }

  private boolean validLayoutParams(Result result, int flags) {
    for (int i = 0; i < Integer.SIZE; i++) {
      int flag = (1 << i);
      // Compare against 0, not 1: `flags & flag` yields the flag's own value, so `== 1` was only
      // ever true for bit 0 and every other flag skipped validation entirely.
      if ((flags & flag) != 0) {
        if (!validLayoutParam(flag)) {
          result.error("FlutterWindowManagerPlusPlugin","FlutterWindowManagerPlusPlugin: invalid flag specification: " + Integer.toHexString(flag), null);
          return false;
        }
      }
    }

    return true;
  }

  /** Applies {@code flags} to the current Activity window and records them for re-application. */
  private void addWindowFlags(int flags) {
    appliedFlags |= flags;
    activity.getWindow().addFlags(flags);
  }

  /** Clears {@code flags} from the current Activity window and stops tracking them. */
  private void clearWindowFlags(int flags) {
    appliedFlags &= ~flags;
    activity.getWindow().clearFlags(flags);
  }

  /**
   * Re-applies the tracked flags to the Activity we have just been (re)attached to.
   *
   * <p>Without this, a configuration change silently drops FLAG_SECURE and the app becomes
   * screenshot-able again without the Dart side ever being told.
   */
  private void reapplyFlags() {
    if (activity != null && appliedFlags != 0) {
      activity.getWindow().addFlags(appliedFlags);
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (activity == null) {
      result.error("NO_ACTIVITY", "FlutterWindowManagerPlusPlugin: ignored flag state change, current activity is null", null);
      return;
    }

    switch (call.method) {
      case "addFlags":
      case "clearFlags": {
        final Integer flags = call.argument("flags");
        if (flags == null) {
          result.error("INVALID_ARGUMENT", "Flags argument is missing", null);
          return;
        }

        if (!validLayoutParams(result, flags)) {
          return;
        }

        if (call.method.equals("addFlags")) {
          addWindowFlags(flags);
        } else {
          clearWindowFlags(flags);
        }
        result.success(true);
        break;
      }
      case "setSecure": {
        final Boolean setSecure = call.argument("setSecure");
        if (setSecure == null) {
          result.error("INVALID_ARGUMENT", "setSecure argument is missing", null);
          return;
        }

        if (setSecure) {
          addWindowFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
          clearWindowFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        result.success(true);
        break;
      }
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
    activity = activityPluginBinding.getActivity();
    reapplyFlags();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
    activity = activityPluginBinding.getActivity();
    reapplyFlags();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }
}

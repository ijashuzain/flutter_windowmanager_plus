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
        return true;
      case WindowManager.LayoutParams.FLAG_BLUR_BEHIND:
        return !(Build.VERSION.SDK_INT >= 15);
      case WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD:
        return (Build.VERSION.SDK_INT >= 5 && Build.VERSION.SDK_INT < 26);
      case WindowManager.LayoutParams.FLAG_DITHER:
        return !(Build.VERSION.SDK_INT >= 17);
      case WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS:
        return (Build.VERSION.SDK_INT >= 21);
      case WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR:
        return (Build.VERSION.SDK_INT >= 22);
      case WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN:
        return (Build.VERSION.SDK_INT >= 18);
      case WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE:
        return (Build.VERSION.SDK_INT >= 19);
      case WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED:
        return !(Build.VERSION.SDK_INT >= 27);
      case WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING:
        return !(Build.VERSION.SDK_INT >= 20);
      case WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION:
      case WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS:
        return (Build.VERSION.SDK_INT >= 19);
      case WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON:
        return !(Build.VERSION.SDK_INT >= 27);
      default:
        return false;
    }
  }

  private boolean validLayoutParams(Result result, int flags) {
    for (int i = 0; i < Integer.SIZE; i++) {
      int flag = (1 << i);
      if ((flags & flag) == 1) {
        if (!validLayoutParam(flag)) {
          result.error("FlutterWindowManagerPlusPlugin","FlutterWindowManagerPlusPlugin: invalid flag specification: " + Integer.toHexString(flag), null);
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (activity == null) {
      result.error("NO_ACTIVITY", "FlutterWindowManagerPlusPlugin: ignored flag state change, current activity is null", null);
      return;
    }

    final Integer flags = call.argument("flags");
    if (flags == null) {
      result.error("INVALID_ARGUMENT", "Flags argument is missing", null);
      return;
    }

    if (!validLayoutParams(result, flags)) {
      return;
    }

    switch (call.method) {
      case "addFlags":
        activity.getWindow().addFlags(flags);
        result.success(true);
        break;
      case "clearFlags":
        activity.getWindow().clearFlags(flags);
        result.success(true);
        break;
      case "setSecure":
        Boolean setSecure = call.argument("setSecure");
        if (setSecure == null) {
          result.error("INVALID_ARGUMENT", "setSecure argument is missing", null);
          return;
        }
        if (setSecure) {
          activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
          activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        result.success(true);
        break;
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
    activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
    activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }
}
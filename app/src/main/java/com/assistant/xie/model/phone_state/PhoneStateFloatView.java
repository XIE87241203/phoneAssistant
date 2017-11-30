package com.assistant.xie.model.phone_state;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.assistant.xie.R;
import com.assistant.xie.Utils.DisplayUtil;
import com.assistant.xie.Utils.FloatingManager;
import com.assistant.xie.service.FloatWindowService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by XIE on 2017/11/27.
 * 手机状态悬浮窗
 */

public class PhoneStateFloatView extends LinearLayout implements View.OnClickListener {
    private TextView tv_msg;
    private FloatingManager mWindowManager;
    private boolean isOpen = false;
    private boolean isHide = false;
    private List<String> switchList;

    public PhoneStateFloatView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_window_phone_state, this);
        mWindowManager = FloatingManager.getInstance(context);
        tv_msg = findViewById(R.id.tv_msg);
        switchList = new ArrayList<>();
        setOnClickListener(this);
        refreshView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //注册电池广播
        PhoneStateUtils.getInstance().registerBatteryBroadcast(getContext());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //反注册电池广播
        PhoneStateUtils.getInstance().unregisterBatteryBroadcast(getContext());
    }

    public void refreshView() {
        //从缓存中读取开关信息
        Map<String, Boolean> switchMap = PhoneStateUtils.getInstance().getSaveData(getContext());
        if (!switchMap.isEmpty()) {
            if (!switchMap.isEmpty()) {
                //如果有开了的开关
                refreshMsg();
                open();
            } else {
                close();
            }
        }
        //获取打开的tag
        List<String> saveTag = PhoneStateUtils.getInstance().getSaveTagList();
        switchList.clear();
        for (String tag : saveTag) {
            if (switchMap.get(tag)) {
                switchList.add(tag);
            }
        }
        if(isHide) isHide= false;
        if(switchList.isEmpty()){
            close();
        }else{
            open();
        }

    }

    /**
     * 刷新状态信息
     */
    public void refreshMsg() {
        if (isAttachedToWindow()) {
            String upload_speed = PhoneStateUtils.getInstance().getTxNetSpeed(FloatWindowService.REFRESH_TIME);
            String download_speed = PhoneStateUtils.getInstance().getRxNetSpeed(FloatWindowService.REFRESH_TIME);
            String ram_static = PhoneStateUtils.getInstance().getAvailMemory(getContext()) + "/" + PhoneStateUtils.getInstance().getTotalMemory(getContext());
            String battery_static = PhoneStateUtils.getInstance().getBatteryStatus();
            String battery_capacity = PhoneStateUtils.getInstance().getBattery();
            String battery_voltage = PhoneStateUtils.getInstance().getBatteryV();
            String battery_temperature = PhoneStateUtils.getInstance().getBatteryT();
            String rom_state = PhoneStateUtils.getInstance().getROMUsageStatus(getContext());
            String sdcard_rom_state = PhoneStateUtils.getInstance().getSDUsageStatus(getContext());
            //发送广播更新activity
            Intent counterIntent = new Intent();
            counterIntent.putExtra("upload_speed", upload_speed);
            counterIntent.putExtra("download_speed", download_speed);
            counterIntent.putExtra("ram_static", ram_static);
            counterIntent.putExtra("battery_static", battery_static);
            counterIntent.putExtra("battery_capacity", battery_capacity);
            counterIntent.putExtra("battery_voltage", battery_voltage);
            counterIntent.putExtra("battery_temperature", battery_temperature);
            counterIntent.putExtra("rom_state", rom_state);
            counterIntent.putExtra("sdcard_rom_state", sdcard_rom_state);
            counterIntent.setAction("com.assistant.xie.REFRESH_PHONE_STATE");
            getContext().sendBroadcast(counterIntent);
            //隐藏时保留框大小
            if (!switchList.isEmpty() && isOpen &&!isHide) {
                StringBuilder msg = new StringBuilder();
                for (String tag : switchList) {
                    switch (tag) {
                        case PhoneStateStaticConstants.SAVE_KEY_2_UPLOAD_SPEED:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_upload_speed), upload_speed));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_3_DOWNLOAD_SPEED:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_download_speed), download_speed));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_1_RAM_STATIC:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_ram_static), ram_static));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_4_BATTERY_STATIC:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_battery_static), battery_static));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_5_BATTERY_CAPACITY:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_battery_capacity), battery_capacity));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_6_BATTERY_VOLTAGE:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_battery_voltage), battery_voltage));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_7_BATTERY_TEMPERATURE:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_battery_temperature), battery_temperature));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_8_ROM_STATE:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_rom_state), rom_state));
                            break;
                        case PhoneStateStaticConstants.SAVE_KEY_9_SDCARD_ROM_STATE:
                            msg.append(String.format(getResources().getString(R.string.phone_state_float_sdcard_rom_state), sdcard_rom_state));
                            break;
                    }
                    msg.append("\n");
                }
                msg.delete(msg.length() - 1, msg.length());
                tv_msg.setText(msg.toString());
            }
            if(isHide){
                tv_msg.setText("显示");
            }
        }
    }

    private void open() {
        if(isOpen) return;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP | Gravity.START;
        params.y = DisplayUtil.getStarusBarHeight(getContext());
        //总是在窗口之上
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        mWindowManager.addView(this, params);
        isOpen = true;
    }

    public void close() {
        if(!isOpen) return;
        tv_msg.setText("");
        mWindowManager.removeView(this);
        isOpen = false;
    }

    @Override
    public void onClick(View v) {
        isHide=!isHide;
        refreshMsg();
    }
}

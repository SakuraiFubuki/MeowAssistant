package com.sakuraifubuki.meowassistant;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private CheckBox cbAppend;
    private CheckBox cbAutoStart;
    private CheckBox cbEmoticon;
    private CheckBox cbKeepAlive;
    private CheckBox cbPasswordProtect;
    private CatConfig config;
    private EditText etAppendText;
    private EditText etCustomEmoticons;
    private EditText etRules;
    private EditText etRealtimeDelay;
    private CheckBox rbPunctuation;
    private CheckBox rbRealtime;
    private TextView statusText;
    private Button toggleButton;
    private Button batteryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.config = CatConfig.load(this);
        } catch (Exception e) {
            this.config = new CatConfig();
        }
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(40, 40, 40, 80);
        root.setBackgroundColor(Color.parseColor("#FFF8E1"));
        root.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));

        TextView title = new TextView(this);
        title.setText("喵喵文本改写助手");
        title.setTextSize(24.0f);
        title.setTextColor(Color.rgb(230, 81, 0));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(17);
        title.setPadding(0, 40, 0, 8);
        root.addView(title);
        TextView subtitle = new TextView(this);
        subtitle.setText("控制面板 · 所有规则均可自定义");
        subtitle.setTextSize(14.0f);
        subtitle.setTextColor(Color.rgb(141, 110, 99));
        subtitle.setGravity(17);
        subtitle.setPadding(0, 0, 0, 24);
        root.addView(subtitle);

        this.statusText = new TextView(this);
        this.statusText.setTextSize(16.0f);
        this.statusText.setGravity(17);
        this.statusText.setPadding(24, 18, 24, 18);
        this.statusText.setBackgroundColor(-1);
        this.statusText.setTextColor(Color.rgb(51, 51, 51));
        root.addView(this.statusText);
        this.toggleButton = new Button(this);
        this.toggleButton.setTextSize(16.0f);
        this.toggleButton.setTextColor(-1);
        this.toggleButton.setPadding(32, 16, 32, 16);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, -2);
        btnLp.setMargins(0, 16, 0, 0);
        this.toggleButton.setLayoutParams(btnLp);
        this.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openAccessibilitySettings();
            }
        });
        root.addView(this.toggleButton);
        root.addView(divider());

        Button saveBtn = new Button(this);
        saveBtn.setText("保存设置");
        saveBtn.setTextSize(16.0f);
        saveBtn.setTextColor(-1);
        saveBtn.setBackgroundColor(Color.rgb(255, 111, 0));
        saveBtn.setPadding(40, 16, 40, 16);
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(-1, -2);
        saveLp.setMargins(0, 16, 0, 0);
        saveBtn.setLayoutParams(saveLp);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.saveConfig();
            }
        });
        root.addView(saveBtn);
        TextView hint = new TextView(this);
        hint.setText("提示：修改设置后请点击保存，服务下次触发时自动加载");
        hint.setTextSize(11.0f);
        hint.setTextColor(Color.rgb(161, 136, 127));
        hint.setGravity(17);
        hint.setPadding(16, 12, 16, 8);
        root.addView(hint);

        TextView modeTitle = new TextView(this);
        modeTitle.setText("处理模式");
        modeTitle.setTextSize(18.0f);
        modeTitle.setTextColor(Color.rgb(93, 64, 55));
        modeTitle.setTypeface(Typeface.DEFAULT_BOLD);
        modeTitle.setPadding(0, 16, 0, 12);
        root.addView(modeTitle);
        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(0);
        modeRow.setPadding(0, 8, 0, 8);
        this.rbPunctuation = new CheckBox(this);
        this.rbPunctuation.setText("标点触发");
        this.rbPunctuation.setTextSize(16.0f);
        this.rbPunctuation.setTextColor(Color.rgb(51, 51, 51));
        this.rbPunctuation.setChecked(CatConfig.MODE_PUNCTUATION.equals(this.config.processingMode));
        this.rbPunctuation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.m0lambda$onCreate$0$comsakuraifubukimeowassistantMainActivity(buttonView, isChecked);
            }
        });
        modeRow.addView(this.rbPunctuation);
        this.rbRealtime = new CheckBox(this);
        this.rbRealtime.setText("实时处理");
        this.rbRealtime.setTextSize(16.0f);
        this.rbRealtime.setTextColor(Color.rgb(51, 51, 51));
        this.rbRealtime.setChecked(CatConfig.MODE_REALTIME.equals(this.config.processingMode));
        this.rbRealtime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.m1lambda$onCreate$1$comsakuraifubukimeowassistantMainActivity(buttonView, isChecked);
            }
        });
        modeRow.addView(this.rbRealtime);
        root.addView(modeRow);
        TextView modeHint = new TextView(this);
        modeHint.setText("标点触发：打字时只在标点处立即处理\n实时处理：每输入一个字立即处理（体验可能较快）");
        modeHint.setTextSize(11.0f);
        modeHint.setTextColor(Color.rgb(161, 136, 127));
        modeHint.setPadding(0, 0, 0, 16);
        root.addView(modeHint);
        this.etRealtimeDelay = new EditText(this);
        this.etRealtimeDelay.setInputType(2);
        this.etRealtimeDelay.setBackgroundColor(-1);
        this.etRealtimeDelay.setPadding(16, 12, 16, 12);
        this.etRealtimeDelay.setHint("实时处理检测延迟(ms)");
        this.etRealtimeDelay.setText(String.valueOf(this.config.realtimeDelay));
        LinearLayout.LayoutParams delayLp = new LinearLayout.LayoutParams(-1, -2);
        delayLp.setMargins(0, 0, 0, 4);
        this.etRealtimeDelay.setLayoutParams(delayLp);
        root.addView(this.etRealtimeDelay);
        TextView delayHint = new TextView(this);
        delayHint.setText("实时处理检测延迟：两次检测之间的最小间隔（毫秒）");
        delayHint.setTextSize(11.0f);
        delayHint.setTextColor(Color.rgb(161, 136, 127));
        delayHint.setPadding(0, 0, 0, 16);
        root.addView(delayHint);

        TextView funcTitle = new TextView(this);
        funcTitle.setText("功能开关");
        funcTitle.setTextSize(18.0f);
        funcTitle.setTextColor(Color.rgb(93, 64, 55));
        funcTitle.setTypeface(Typeface.DEFAULT_BOLD);
        funcTitle.setPadding(0, 16, 0, 8);
        root.addView(funcTitle);
        this.cbAppend = addCheckbox(root, "断句追加", "在句号、叹号等标点分句后追加文本", this.config.enableAppend);
        this.etAppendText = new EditText(this);
        this.etAppendText.setInputType(131073);
        this.etAppendText.setBackgroundColor(-1);
        this.etAppendText.setPadding(16, 12, 16, 12);
        this.etAppendText.setHint("追加内容（默认：喵）");
        this.etAppendText.setText(this.config.appendText != null ? this.config.appendText : "喵");
        LinearLayout.LayoutParams etLp1 = new LinearLayout.LayoutParams(-1, -2);
        etLp1.setMargins(0, 0, 0, 4);
        this.etAppendText.setLayoutParams(etLp1);
        root.addView(this.etAppendText);
        this.cbEmoticon = addCheckbox(root, "句末颜文字", "在消息末尾附加随机颜文字", this.config.enableRandomEmoticon);
        this.cbPasswordProtect = addCheckbox(root, "密码保护", "密码、可见密码、数字密码框不会被读取或改写", this.config.enablePasswordProtect);
        this.cbKeepAlive = addCheckbox(root, "前台服务保活", "显示常驻通知保持服务存活，降低被系统清理的概率", this.config.enableKeepAlive);
        this.cbAutoStart = addCheckbox(root, "开机/更新后恢复", "开机或应用更新后自动恢复保活服务（需先开启前台服务保活）", this.config.enableAutoStart);
        this.batteryBtn = new Button(this);
        this.batteryBtn.setText("电池优化设置");
        this.batteryBtn.setTextSize(14.0f);
        this.batteryBtn.setTextColor(-1);
        this.batteryBtn.setPadding(40, 14, 40, 14);
        LinearLayout.LayoutParams batteryLp = new LinearLayout.LayoutParams(-1, -2);
        batteryLp.setMargins(0, 12, 0, 0);
        this.batteryBtn.setLayoutParams(batteryLp);
        this.batteryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openBatterySettings();
            }
        });
        root.addView(this.batteryBtn);
        TextView batteryHint = new TextView(this);
        batteryHint.setText("建议关闭本应用的电池优化，避免后台服务被系统限制");
        batteryHint.setTextSize(11.0f);
        batteryHint.setTextColor(Color.rgb(161, 136, 127));
        batteryHint.setPadding(0, 8, 0, 8);
        root.addView(batteryHint);

        TextView ruleTitle = new TextView(this);
        ruleTitle.setText("文本替换规则");
        ruleTitle.setTextSize(18.0f);
        ruleTitle.setTextColor(Color.rgb(93, 64, 55));
        ruleTitle.setTypeface(Typeface.DEFAULT_BOLD);
        ruleTitle.setPadding(0, 16, 0, 8);
        root.addView(ruleTitle);
        TextView ruleHint = new TextView(this);
        ruleHint.setText("每行一条，按顺序应用。格式：原词=替换词（也支持 ＝ 全角等号 / →）\n例：我=本喵 / 你＝主人 / 也支持数字等任意文本");
        ruleHint.setTextSize(12.0f);
        ruleHint.setTextColor(Color.rgb(141, 110, 99));
        ruleHint.setPadding(0, 0, 0, 12);
        root.addView(ruleHint);
        this.etRules = new EditText(this);
        this.etRules.setInputType(131073);
        this.etRules.setLines(6);
        this.etRules.setMinLines(6);
        this.etRules.setBackgroundColor(-1);
        this.etRules.setPadding(16, 12, 16, 12);
        this.etRules.setText(CatConfig.rulesToString(this.config.rules));
        root.addView(this.etRules);

        TextView emojiTitle = new TextView(this);
        emojiTitle.setText("自定义颜文字");
        emojiTitle.setTextSize(18.0f);
        emojiTitle.setTextColor(Color.rgb(93, 64, 55));
        emojiTitle.setTypeface(Typeface.DEFAULT_BOLD);
        emojiTitle.setPadding(0, 16, 0, 8);
        root.addView(emojiTitle);
        TextView emojiHint = new TextView(this);
        emojiHint.setText("每行一个颜文字，留空则使用内置库");
        emojiHint.setTextSize(12.0f);
        emojiHint.setTextColor(Color.rgb(141, 110, 99));
        emojiHint.setPadding(0, 0, 0, 12);
        root.addView(emojiHint);
        this.etCustomEmoticons = new EditText(this);
        this.etCustomEmoticons.setInputType(131073);
        this.etCustomEmoticons.setLines(4);
        this.etCustomEmoticons.setMinLines(4);
        this.etCustomEmoticons.setBackgroundColor(-1);
        this.etCustomEmoticons.setPadding(16, 12, 16, 12);
        this.etCustomEmoticons.setHint("例如: (=^w^=) 等");
        this.etCustomEmoticons.setText(joinLines(this.config.customEmoticons));
        root.addView(this.etCustomEmoticons);


        Button resetBtn = new Button(this);
        resetBtn.setText("恢复默认配置");
        resetBtn.setTextSize(14.0f);
        resetBtn.setTextColor(Color.rgb(255, 111, 0));
        resetBtn.setBackgroundColor(-1);
        resetBtn.setPadding(40, 14, 40, 14);
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(-1, -2);
        resetLp.setMargins(0, 12, 0, 0);
        resetBtn.setLayoutParams(resetLp);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.restoreDefaultConfig();
            }
        });
        root.addView(resetBtn);
        TextView githubLink = new TextView(this);
        githubLink.setText("项目地址：github.com/SakuraiFubuki/MeowAssistant");
        githubLink.setTextSize(11.0f);
        githubLink.setTextColor(Color.rgb(161, 136, 127));
        githubLink.setGravity(17);
        githubLink.setPadding(16, 0, 16, 8);
        root.addView(githubLink);
        TextView githubFree = new TextView(this);
        githubFree.setText("本软件遵循[AGPL-3.0]协议免费开源\n严禁将本软件或其任何衍生版本用于商业盈利活动等直接或间接获利行为");
        githubFree.setTextSize(11.0f);
        githubFree.setTextColor(Color.rgb(161, 136, 127));
        githubFree.setGravity(17);
        githubFree.setPadding(16, 0, 16, 8);
        root.addView(githubFree);
        TextView noRedistribute = new TextView(this);
        noRedistribute.setText("严禁将本软件进行二次分发!");
        noRedistribute.setTextSize(11.0f);
        noRedistribute.setTextColor(Color.rgb(161, 136, 127));
        noRedistribute.setGravity(17);
        noRedistribute.setPadding(16, 0, 16, 8);
        root.addView(noRedistribute);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    void m0lambda$onCreate$0$comsakuraifubukimeowassistantMainActivity(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            this.rbRealtime.setChecked(false);
        }
    }

    void m1lambda$onCreate$1$comsakuraifubukimeowassistantMainActivity(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            this.rbPunctuation.setChecked(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
        updateBatteryStatus();
        CatConfig fresh = CatConfig.load(this);
        if (fresh != null && this.config != null) {
            this.config.enableKeepAlive = fresh.enableKeepAlive;
            if (this.cbKeepAlive != null) {
                this.cbKeepAlive.setChecked(fresh.enableKeepAlive);
            }
        }
        if (this.config != null && this.config.enableKeepAlive) {
            KeepAliveService.start(this);
        }
    }

    private void updateServiceStatus() {
        if (this.statusText == null || this.toggleButton == null) {
            return;
        }
        boolean enabled = isAccessibilityServiceEnabled();
        if (enabled) {
            this.statusText.setText("服务状态：已开启");
            this.statusText.setTextColor(Color.rgb(46, 125, 50));
            this.toggleButton.setText("服务已开启");
            this.toggleButton.setEnabled(false);
            this.toggleButton.setBackgroundColor(Color.rgb(165, 214, 167));
            return;
        }
        this.statusText.setText("服务状态：未开启");
        this.statusText.setTextColor(Color.rgb(198, 40, 40));
        this.toggleButton.setText("前往开启无障碍服务");
        this.toggleButton.setEnabled(true);
        this.toggleButton.setBackgroundColor(Color.rgb(255, 111, 0));
    }

    private boolean isAccessibilityServiceEnabled() {
        try {
            AccessibilityManager am = (AccessibilityManager) getSystemService("accessibility");
            if (am == null) {
                return false;
            }
            List<AccessibilityServiceInfo> services = am.getEnabledAccessibilityServiceList(-1);
            for (AccessibilityServiceInfo info : services) {
                if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null && getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void openAccessibilitySettings() {
        try {
            Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
            intent.setFlags(268435456);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开设置", 0).show();
        }
    }

    public void openBatterySettings() {
        try {
            if (!isIgnoringBatteryOptimizations() && Build.VERSION.SDK_INT >= 23) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
        } catch (Exception e) {
        }
        try {
            startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        } catch (Exception e2) {
            Toast.makeText(this, "无法打开电池优化设置", 0).show();
        }
    }

    private boolean isIgnoringBatteryOptimizations() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                PowerManager pm = (PowerManager) getSystemService("power");
                if (pm != null) {
                    return pm.isIgnoringBatteryOptimizations(getPackageName());
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private void updateBatteryStatus() {
        if (this.batteryBtn == null) {
            return;
        }
        if (isIgnoringBatteryOptimizations()) {
            this.batteryBtn.setText("电池优化已关闭（点击可恢复）");
            this.batteryBtn.setBackgroundColor(Color.rgb(165, 214, 167));
        } else {
            this.batteryBtn.setText("前往关闭电池优化");
            this.batteryBtn.setBackgroundColor(Color.rgb(255, 111, 0));
        }
    }

    private void applyKeepAlive() {
        if (this.config.enableKeepAlive) {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            KeepAliveService.start(this);
        } else {
            KeepAliveService.stop(this);
        }
    }

    public void restoreDefaultConfig() {
        try {
            CatConfig defaults = new CatConfig();
            this.config = defaults;
            this.cbAppend.setChecked(defaults.enableAppend);
            this.etAppendText.setText(defaults.appendText);
            this.cbEmoticon.setChecked(defaults.enableRandomEmoticon);
            this.cbPasswordProtect.setChecked(defaults.enablePasswordProtect);
            this.cbKeepAlive.setChecked(defaults.enableKeepAlive);
            this.cbAutoStart.setChecked(defaults.enableAutoStart);
            this.rbPunctuation.setChecked(CatConfig.MODE_PUNCTUATION.equals(defaults.processingMode));
            this.rbRealtime.setChecked(CatConfig.MODE_REALTIME.equals(defaults.processingMode));
            this.etRealtimeDelay.setText(String.valueOf(defaults.realtimeDelay));
            this.etRules.setText(CatConfig.rulesToString(defaults.rules));
            this.etCustomEmoticons.setText(joinLines(defaults.customEmoticons));
            defaults.save(this);
            applyKeepAlive();
            Toast.makeText(this, "已恢复默认配置", 0).show();
        } catch (Exception e) {
            Toast.makeText(this, "恢复失败: " + e.getMessage(), 0).show();
        }
    }

    private CheckBox addCheckbox(LinearLayout linearLayout, String title, String desc, boolean checked) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(0);
        row.setPadding(0, 8, 0, 8);
        row.setGravity(16);
        CheckBox cb = new CheckBox(this);
        cb.setChecked(checked);
        row.addView(cb, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(1);
        textCol.setPadding(12, 0, 0, 0);
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(16.0f);
        tvTitle.setTextColor(Color.rgb(51, 51, 51));
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(tvTitle);
        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextSize(12.0f);
        tvDesc.setTextColor(Color.rgb(136, 136, 136));
        textCol.addView(tvDesc);
        row.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout.addView(row);
        return cb;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(Color.rgb(221, 221, 221));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 2);
        lp.setMargins(0, 24, 0, 8);
        v.setLayoutParams(lp);
        return v;
    }

    private String joinLines(String[] arr) {
        if (arr == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            if (s == null) {
                continue;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(t);
        }
        return sb.toString();
    }

    public void saveConfig() {
        try {
            this.config.enableAppend = this.cbAppend.isChecked();
            String append = this.etAppendText.getText().toString().trim();
            this.config.appendText = append.isEmpty() ? "喵" : append;
            this.config.enableRandomEmoticon = this.cbEmoticon.isChecked();
            this.config.enablePasswordProtect = this.cbPasswordProtect.isChecked();
            this.config.enableKeepAlive = this.cbKeepAlive.isChecked();
            this.config.enableAutoStart = this.cbAutoStart.isChecked();
            this.config.processingMode = this.rbRealtime.isChecked() ? CatConfig.MODE_REALTIME : CatConfig.MODE_PUNCTUATION;
            String delayText = this.etRealtimeDelay.getText() == null ? "" : this.etRealtimeDelay.getText().toString().trim();
            long delay = CatConfig.DEFAULT_REALTIME_DELAY;
            if (!delayText.isEmpty()) {
                try {
                    long parsed = Long.parseLong(delayText);
                    if (parsed >= 0L) {
                        delay = parsed;
                    }
                } catch (NumberFormatException e) {
                }
            }
            this.config.realtimeDelay = delay;

            ArrayList<CatConfig.Rule> rules = new ArrayList<>();
            String rulesText = this.etRules.getText() == null ? "" : this.etRules.getText().toString();
            for (String line : rulesText.split("\n")) {
                CatConfig.Rule r = CatConfig.parseRule(line);
                if (r != null) {
                    rules.add(r);
                }
            }
            this.config.rules = rules;

            ArrayList<String> list = new ArrayList<>();
            String customText = this.etCustomEmoticons.getText() == null ? "" : this.etCustomEmoticons.getText().toString().trim();
            if (!customText.isEmpty()) {
                for (String raw : customText.split("\n")) {
                    String t = raw.trim();
                    if (!t.isEmpty()) {
                        list.add(t);
                    }
                }
            }
            this.config.customEmoticons = list.toArray(new String[0]);
            this.config.save(this);
            applyKeepAlive();
            Toast.makeText(this, "设置已保存", 0).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), 0).show();
        }
    }




}

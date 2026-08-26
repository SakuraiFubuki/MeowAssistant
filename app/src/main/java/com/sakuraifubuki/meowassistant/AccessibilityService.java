package com.sakuraifubuki.meowassistant;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    private static final String ID_INPUT = "com.tencent.mobileqq:id/input";
    private static final String ID_SEND = "com.tencent.mobileqq:id/send_btn";
    private CatConfig cachedConfig;
    private String userOriginal = "";
    private String lastSet = "";
    private boolean processing = false;
    private long lastWriteTime = 0;
    private long appliedTimeout = -1;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        String pkg = e.getPackageName() != null ? e.getPackageName().toString() : "";
        if (getPackageName().equals(pkg)) {
            return;
        }
        int type = e.getEventType();
        if (type == 32) {
            // 窗口变化只重载配置，不清空 userOriginal/lastSet：
            // 全局生效后，输入法/系统等其它应用的窗口事件会频繁触发本分支，
            // 若在此清空状态，实时处理打字中途会丢失增量基准，导致兜底剥离误删规则产物
            // （如规则“我=本喵”时“本喵”里的“喵”被剔成“本爱喵”）或重复追加。
            // 跨窗口切换由 raw 前缀不匹配时的兜底剥离自然重建原文，无需显式清空。
            this.cachedConfig = CatConfig.load(this);
            applyServiceInfo(this.cachedConfig);
            return;
        }
        if (type == 1) {
            AccessibilityNodeInfo src = e.getSource();
            if (src != null) {
                String id = src.getViewIdResourceName();
                if (ID_SEND.equals(id)) {
                    doProcess(true);
                }
                src.recycle();
                return;
            }
            return;
        }
        if (type == 16) {
            CatConfig cfg = this.cachedConfig;
            if (cfg == null) {
                cfg = CatConfig.load(this);
                this.cachedConfig = cfg;
            }
            String mode = cfg.processingMode != null ? cfg.processingMode : CatConfig.MODE_PUNCTUATION;
            if (CatConfig.MODE_REALTIME.equals(mode)) {
                doProcess(false);
                return;
            }
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) {
                return;
            }
            AccessibilityNodeInfo inp = findNodeById(root, ID_INPUT);
            if (inp == null) {
                inp = findEditable(root);
            }
            root.recycle();
            if (inp == null) {
                return;
            }
            if (cfg.enablePasswordProtect && inp.isPassword()) {
                inp.recycle();
                return;
            }
            CharSequence cs = inp.getText();
            inp.recycle();
            if (cs == null || cs.length() == 0) {
                return;
            }
            String raw = cs.toString().trim();
            if (!raw.isEmpty() && isPunctuationEnding(raw)) {
                doProcess(false);
            }
        }
    }

    private boolean isPunctuationEnding(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        char last = s.charAt(s.length() - 1);
        return last == 12290 || last == 65281 || last == '!' || last == 65311 || last == '?' || last == ' ';
    }

    private void doProcess(boolean isSendClick) {
        if (this.processing) {
            return;
        }
        this.processing = true;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            this.processing = false;
            return;
        }
        AccessibilityNodeInfo inp = findNodeById(root, ID_INPUT);
        if (inp == null) {
            inp = findEditable(root);
        }
        if (inp == null) {
            root.recycle();
            this.processing = false;
            return;
        }
        CatConfig cfg = this.cachedConfig;
        if (cfg == null) {
            cfg = CatConfig.load(this);
            this.cachedConfig = cfg;
        }
        if (cfg.enablePasswordProtect && inp.isPassword()) {
            inp.recycle();
            root.recycle();
            this.processing = false;
            return;
        }
        CharSequence cs = inp.getText();
        if (cs == null || cs.length() == 0) {
            inp.recycle();
            root.recycle();
            this.processing = false;
            this.userOriginal = "";
            this.lastSet = "";
            return;
        }
        String raw = cs.toString().trim();
        if (raw.isEmpty()) {
            inp.recycle();
            root.recycle();
            this.processing = false;
            this.userOriginal = "";
            this.lastSet = "";
            return;
        }
        long now = System.currentTimeMillis();
        long j = this.lastWriteTime;
        if (j > 0 && now - j < 600 && raw.equals(this.lastSet)) {
            this.lastWriteTime = 0L;
            inp.recycle();
            root.recycle();
            this.processing = false;
            return;
        }
        boolean isRealtime = CatConfig.MODE_REALTIME.equals(cfg.processingMode);
        if (!isRealtime && this.lastSet.isEmpty()) {
            this.userOriginal = stripAll(raw, cfg);
        } else if (this.lastSet.isEmpty() || !raw.startsWith(this.lastSet)) {
            if (this.lastSet.isEmpty()) {
                this.userOriginal = stripAll(raw, cfg);
            } else {
                this.userOriginal = stripAll(raw, cfg);
            }
        } else {
            String added = raw.substring(this.lastSet.length());
            this.userOriginal += added;
        }
        if (this.userOriginal.isEmpty()) {
            inp.recycle();
            root.recycle();
            this.processing = false;
            return;
        }
        CatConfig effectiveCfg = cfg;
        if (isRealtime && cfg.enableRandomEmoticon && !isSendClick) {
            effectiveCfg = cloneConfigWithoutEmoticon(cfg);
        }
        String target = TextProcessor.process(this.userOriginal, effectiveCfg);
        if (!target.equals(raw)) {
            boolean ok = setText(inp, target);
            if (ok) {
                this.lastSet = target;
                this.lastWriteTime = System.currentTimeMillis();
            }
            inp.recycle();
            root.recycle();
            this.processing = false;
            return;
        }
        this.lastSet = target;
        inp.recycle();
        root.recycle();
        this.processing = false;
    }

    private CatConfig cloneConfigWithoutEmoticon(CatConfig src) {
        CatConfig c = new CatConfig();
        c.enableAppend = src.enableAppend;
        c.appendText = src.appendText;
        c.enableRandomEmoticon = false;
        c.processingMode = src.processingMode;
        c.customEmoticons = src.customEmoticons;
        c.rules = src.rules;
        return c;
    }

    private String stripAll(String text, CatConfig cfg) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String result = text;
        String[] emotes = cfg.getActiveEmoticons();
        if (emotes.length == 0) {
            emotes = CatConfig.BUILTIN_EMOTICONS;
        }
        Arrays.sort(emotes, new Comparator() {
            @Override
            public int compare(Object obj, Object obj2) {
                return AccessibilityService.lambda$stripAll$0((String) obj, (String) obj2);
            }
        });
        for (String em : emotes) {
            if (em == null || em.isEmpty()) {
                continue;
            }
            int idx;
            while ((idx = result.indexOf(em)) >= 0) {
                int st;
                if (idx <= 0 || result.charAt(idx - 1) != ' ') {
                    st = idx;
                } else {
                    st = idx - 1;
                }
                result = result.substring(0, st) + result.substring(idx + em.length());
            }
        }
        // 实时处理兜底剥离：先把被替换规则改写的文本逆向还原（尽力而为），
        // 再剔除此前“断句追加”写回的追加文本（默认“喵”）。
        // 顺序必须是“先还原规则、后剔追加文本”：若先剔追加，规则产物里含追加文本时会被误删
        // （如规则“我=本喵”的“本喵”被剔成“本”，原文变成“本爱”而不是“我爱”）。
        // 追加文本只可能出现在“追加位置”（分隔符之前或句末），因此仅剔除这些位置的出现；
        // 句中其它同名文本（如用户手打的“喵喵助手”）是原文，必须保留。
        if (CatConfig.MODE_REALTIME.equals(cfg.processingMode)) {
            if (cfg.rules != null) {
                for (int i = cfg.rules.size() - 1; i >= 0; i--) {
                    CatConfig.Rule r = cfg.rules.get(i);
                    if (r == null || r.to == null || r.to.isEmpty()) {
                        continue;
                    }
                    result = result.replace(r.to, r.from);
                }
            }
            if (cfg.enableAppend && cfg.appendText != null && !cfg.appendText.isEmpty()) {
                result = result.replaceAll(Pattern.quote(cfg.appendText) + "(?=[，,。！!？?\\s]|$)", "");
            }
        }
        return result.replaceAll("\\s*[\\p{S}\\p{So}\\p{Sm}\\p{Sk}\\p{P}]{3,}\\s*", " ").trim();
    }

    static  int lambda$stripAll$0(String a, String b) {
        return b.length() - a.length();
    }

    private AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo n, String id) {
        if (n == null || id == null) {
            return null;
        }
        if (id.equals(n.getViewIdResourceName())) {
            return AccessibilityNodeInfo.obtain(n);
        }
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo c = n.getChild(i);
            if (c != null) {
                AccessibilityNodeInfo r = findNodeById(c, id);
                c.recycle();
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private AccessibilityNodeInfo findEditable(AccessibilityNodeInfo n) {
        if (n == null) {
            return null;
        }
        if (n.isEditable()) {
            return AccessibilityNodeInfo.obtain(n);
        }
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo c = n.getChild(i);
            if (c != null) {
                AccessibilityNodeInfo r = findEditable(c);
                c.recycle();
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private boolean setText(AccessibilityNodeInfo n, String t) {
        if (n == null) {
            return false;
        }
        try {
            Bundle b = new Bundle();
            b.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", t);
            boolean ok = n.performAction(2097152, b);
            if (ok) {
                Bundle a = new Bundle();
                a.putInt("ACTION_ARGUMENT_SELECTION_START_INT", t.length());
                a.putInt("ACTION_ARGUMENT_SELECTION_END_INT", t.length());
                n.performAction(131072, a);
            }
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onInterrupt() {
        this.processing = false;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        CatConfig cfg = CatConfig.load(this);
        this.cachedConfig = cfg;
        this.appliedTimeout = -1L;
        applyServiceInfo(cfg);
    }

    private void applyServiceInfo(CatConfig cfg) {
        long delay = (cfg != null && cfg.realtimeDelay >= 0L) ? cfg.realtimeDelay : CatConfig.DEFAULT_REALTIME_DELAY;
        if (delay == this.appliedTimeout) {
            return;
        }
        AccessibilityServiceInfo i = new AccessibilityServiceInfo();
        i.eventTypes = 49;
        i.feedbackType = 16;
        i.flags = 81;
        i.notificationTimeout = delay;
        setServiceInfo(i);
        this.appliedTimeout = delay;
    }
}

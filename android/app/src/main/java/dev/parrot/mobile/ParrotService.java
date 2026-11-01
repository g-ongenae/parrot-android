package dev.parrot.mobile;
import android.accessibilityservice.AccessibilityService;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import android.graphics.PixelFormat;
import java.util.*;
public final class ParrotService extends AccessibilityService {
    private WindowManager manager;
    private LinearLayout panel;
    private TextView result;
    private Speaker speaker;
    private String captured="";
    @Override protected void onServiceConnected() {
        if(panel!=null) return;
        speaker=new Speaker(this); manager=(WindowManager)getSystemService(WINDOW_SERVICE);
        panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(12,12,12,12); panel.setBackgroundColor(0xfff0fff4);
        Button bubble=new Button(this); bubble.setText("🦜"); bubble.setContentDescription("Parrot: read current screen"); panel.addView(bubble);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.VERTICAL); actions.setVisibility(View.GONE); panel.addView(actions);
        result=new TextView(this); result.setTextColor(0xff123025); result.setTextSize(16); result.setTextIsSelectable(true);
        ScrollView scroll=new ScrollView(this); scroll.addView(result);
        bubble.setOnClickListener(v->{ if(actions.getVisibility()==View.VISIBLE) {actions.setVisibility(View.GONE); return;} captured=capture(); result.setText(captured.isEmpty()?"No readable text found. Try sharing or pasting text into Parrot.":"Captured visible screen text. Choose an action. Summaries select key sentences on device."); actions.setVisibility(View.VISIBLE); panel.post(this::keepOnScreen); });
        for(String action:new String[]{"Read aloud","Summarize","Summarize & read","Stop","Close"}) {
            Button b=new Button(this); b.setText(action); actions.addView(b);
            b.setOnClickListener(v->{ if(action.equals("Stop")) {speaker.stop();return;} if(action.equals("Close")) {speaker.stop();actions.setVisibility(View.GONE);captured="";result.setText("");return;} if(captured.isEmpty()) return; String output=action.equals("Read aloud")?captured:TextTools.summarize(captured); result.setText(output); if(!action.equals("Summarize")) speaker.speak(output); });
        }
        actions.addView(scroll,new LinearLayout.LayoutParams((int)(Math.min(300, getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density - 48) * getResources().getDisplayMetrics().density), (int)(180 * getResources().getDisplayMetrics().density)));
        WindowManager.LayoutParams params=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.END; params.y=120;
        bubble.setOnTouchListener(new View.OnTouchListener() {
            float x,y; int startX,startY; boolean dragged;
            public boolean onTouch(View view, android.view.MotionEvent event) {
                switch(event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: x=event.getRawX(); y=event.getRawY(); startX=params.x; startY=params.y; dragged=false; return true;
                    case MotionEvent.ACTION_MOVE:
                        if(Math.abs(event.getRawX()-x)+Math.abs(event.getRawY()-y)>12) dragged=true;
                        if(dragged) { params.x=Math.max(0,Math.min(getResources().getDisplayMetrics().widthPixels-bubble.getWidth(), startX-(int)(event.getRawX()-x))); params.y=Math.max(0,Math.min(getResources().getDisplayMetrics().heightPixels-bubble.getHeight(),startY+(int)(event.getRawY()-y))); manager.updateViewLayout(panel,params); } return true;
                    case MotionEvent.ACTION_UP: if(!dragged) view.performClick(); return true;
                    default: return true;
                }
            }
        });
        manager.addView(panel,params);
    }
    private void keepOnScreen() {
        if(panel==null || !panel.isAttachedToWindow()) return;
        WindowManager.LayoutParams params=(WindowManager.LayoutParams)panel.getLayoutParams();
        params.x=Math.min(params.x, Math.max(0,getResources().getDisplayMetrics().widthPixels-panel.getWidth()));
        params.y=Math.min(params.y, Math.max(0,getResources().getDisplayMetrics().heightPixels-panel.getHeight()-80));
        manager.updateViewLayout(panel,params);
    }
    private String capture() {
        for(AccessibilityWindowInfo w:getWindows()) {
            if(w.getType()==AccessibilityWindowInfo.TYPE_APPLICATION && w.isActive()) {
                AccessibilityNodeInfo root=w.getRoot();
                if(root==null) continue;
                try { if(getPackageName().contentEquals(root.getPackageName()==null?"":root.getPackageName())) return ""; StringBuilder text=new StringBuilder(); walk(root,text,0,new int[]{0}); return text.toString().trim(); }
                finally { root.recycle(); }
            }
        }
        return "";
    }
    private void walk(AccessibilityNodeInfo node,StringBuilder out,int depth,int[] count) {
        if(depth>60 || ++count[0]>5000 || out.length()>=100000 || !node.isVisibleToUser() || node.isPassword() || node.isEditable()) return;
        CharSequence value=node.getText();
        if(value==null || value.toString().trim().isEmpty()) value=node.getContentDescription();
        if(value!=null && !value.toString().trim().isEmpty()) out.append(value.toString().trim()).append('\n');
        for(int i=0;i<node.getChildCount();i++) { AccessibilityNodeInfo child=node.getChild(i); if(child!=null) {try {walk(child,out,depth+1,count);} finally {child.recycle();}} }
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {} // No background text collection.
    @Override public void onInterrupt() { if(speaker!=null) speaker.stop(); }
    @Override public void onDestroy() { if(panel!=null) manager.removeView(panel); if(speaker!=null) speaker.close(); super.onDestroy(); }
}

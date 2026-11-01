package dev.parrot.mobile;
import android.app.*;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.widget.*;
public final class MainActivity extends Activity {
    private Speaker speaker;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); speaker=new Speaker(this);
        LinearLayout page=new LinearLayout(this); page.setOrientation(1); page.setPadding(32,48,32,24);
        ScrollView scroll=new ScrollView(this); scroll.addView(page); setContentView(scroll);
        TextView title=new TextView(this); title.setText("🦜 Parrot\nToo long? Do listen."); title.setTextSize(28); page.addView(title);
        TextView help=new TextView(this); help.setText("Read, summarize, or both.\n\nEnable the floating reader, open another app, then tap 🦜. Parrot reads only the text currently exposed on screen; it does not scroll the page.\n\nPrivacy: text stays on this device. Password and editable fields are skipped. Summaries select three key sentences locally; they are not AI rewrites. Some apps do not expose readable text.\n"); page.addView(help);
        Button enable=new Button(this); enable.setText("Enable floating reader"); page.addView(enable);
        enable.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Allow access to screen text?").setMessage("Parrot needs Accessibility access to show its floating button and retrieve visible text when you tap it. Text is processed locally, never stored or uploaded. Enable Parrot on the next screen. You can disable it there at any time.").setNegativeButton("Cancel",null).setPositiveButton("Open Accessibility settings",(d,w)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).show());
        EditText input=new EditText(this); input.setHint("Paste text, or share text from another app"); input.setMinLines(6); input.setGravity(48); page.addView(input);
        if(Intent.ACTION_SEND.equals(getIntent().getAction())) input.setText(getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT));
        TextView result=new TextView(this); result.setTextIsSelectable(true); result.setTextSize(18);
        for(String action:new String[]{"Read aloud","Summarize","Summarize & read","Stop"}) {
            Button b=new Button(this); b.setText(action); page.addView(b);
            b.setOnClickListener(v->{ if(action.equals("Stop")) {speaker.stop();return;} String text=input.getText().toString().trim(); if(text.isEmpty()) {input.setError("Add some text first");return;} String output=action.equals("Read aloud")?text:TextTools.summarize(text); result.setText(output); if(!action.equals("Summarize")) speaker.speak(output); });
        }
        page.addView(result);
    }
    @Override protected void onDestroy() { speaker.close(); super.onDestroy(); }
}

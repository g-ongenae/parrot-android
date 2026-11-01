package dev.parrot.mobile;
import android.content.Context;
import android.speech.tts.TextToSpeech;
public final class Speaker {
    private TextToSpeech tts;
    private boolean ready;
    private String pending;
    public Speaker(Context context) {
        tts = new TextToSpeech(context, status -> { ready=status==TextToSpeech.SUCCESS; if(ready && pending!=null) { String text=pending; pending=null; speak(text); } });
    }
    public void speak(String text) {
        stop();
        if(!ready) { pending=text; return; }
        int limit=Math.min(3000,TextToSpeech.getMaxSpeechInputLength());
        for(int start=0;start<text.length();) {
            int end=Math.min(text.length(),start+limit);
            if(end<text.length()) { int space=text.lastIndexOf(' ',end); if(space>start) end=space; }
            tts.speak(text.substring(start,end),TextToSpeech.QUEUE_ADD,null,"parrot-"+start); start=end;
        }
    }
    public void stop() { pending=null; if(tts!=null) tts.stop(); }
    public void close() { stop(); if(tts!=null) tts.shutdown(); }
}

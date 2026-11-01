package dev.parrot.mobile;
import java.util.*;
public final class TextTools {
    private TextTools() {}
    public static String summarize(String text) {
        String[] sentences = text.trim().split("(?<=[.!?。！？])\\s+|\\n+");
        if (sentences.length <= 3) return text.trim();
        Map<String,Integer> frequencies = new HashMap<>();
        for (String s : sentences) for (String w : words(s)) if(w.length()>3) frequencies.merge(w,1,Integer::sum);
        List<Integer> indices = new ArrayList<>();
        for(int i=0;i<sentences.length;i++) indices.add(i);
        indices.sort((a,b)->Double.compare(score(sentences[b],frequencies), score(sentences[a],frequencies)));
        List<Integer> selected = new ArrayList<>(indices.subList(0,Math.min(3,indices.size())));
        Collections.sort(selected);
        StringBuilder out = new StringBuilder();
        for(int i:selected) { if(out.length()>0) out.append("\n\n"); out.append(sentences[i].trim()); }
        return out.toString();
    }
    private static String[] words(String s) { return s.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"); }
    private static double score(String s, Map<String,Integer> f) {
        String[] w=words(s); double n=0; for(String word:w) if(word.length()>3) n+=f.getOrDefault(word,0);
        return n/Math.sqrt(Math.max(1,w.length));
    }
}

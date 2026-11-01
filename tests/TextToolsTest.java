package dev.parrot.mobile;
public final class TextToolsTest {
    public static void main(String[] args) {
        check(TextTools.summarize("").equals(""), "empty");
        check(TextTools.summarize("Bonjour. Hello.").equals("Bonjour. Hello."), "short text");
        String source="Oranges grow on trees. Trees need water. Water helps trees grow. The train leaves tomorrow. Trees have green leaves.";
        String summary=TextTools.summarize(source);
        check(summary.contains("Trees need water."), "topic selection");
        check(!summary.contains("The train leaves tomorrow."), "discard unrelated sentence");
        check(summary.indexOf("Trees need water.") < summary.indexOf("Water helps trees grow."), "source order");
        check(TextTools.summarize("Un\nDeux\nTrois\nQuatre").split("\n\n").length==3, "line-based capture");
        System.out.println("TextTools checks passed");
    }
    private static void check(boolean ok,String name) {if(!ok) throw new AssertionError(name);}
}

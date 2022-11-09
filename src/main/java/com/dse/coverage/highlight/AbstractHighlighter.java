package com.dse.coverage.highlight;

public abstract class AbstractHighlighter implements IHighlighter {
    protected String coverage;

    public static final String lineSignalStart = "<b style=\"color: grey;\">";

    public static final String trueMarker ="<b style=\"background-color:#3D9970;color:white\";>&nbsp;T&nbsp;</b>&nbsp;";
    public static final String falseMarker ="<b style=\"background-color:#FF4136;color:white\";>&nbsp;F&nbsp;</b>&nbsp;";

    public static final String highlightSignalStartForNormalStatement = "<b style=\"background-color:yellow;color:black\";>";
    public static final String highlightSignalStartForConditionalStatement = highlightSignalStartForNormalStatement;

    public static final String highlightSignalEnd = "</b>";

    protected static String addPre(String src) {
        return "<pre>" + src + "</pre>";
    }


    public static String addLineNumber(String src){
        String[] lines = src.split("\n");
        int max = 1;
        int tmpLength = lines.length;
        while (tmpLength / 10 > 0) {
            max++;
            tmpLength /= 10;
        }
        for (int i = 0; i < lines.length; i++) {
            int tmpI = i;
            int count = 1;
            while (tmpI / 10 > 0) {
                count++;
                tmpI /= 10;
            }

            String buffer = "";
            while (count < max) {
                buffer += " ";
                count++;
            }
            lines[i] = lineSignalStart + (i + 1) + buffer + "    </b>" + lines[i]; // start line index from 1
        }

        src = String.join("\n", lines);
        return src;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getCoverage() {
        return coverage;
    }
}

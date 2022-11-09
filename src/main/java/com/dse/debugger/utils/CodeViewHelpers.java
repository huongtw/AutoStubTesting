package com.dse.debugger.utils;

import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class CodeViewHelpers {

    private CodeViewHelpers() {
    }

    public static boolean addParagraphStyle(StyleClassedTextArea area, int paraIndex, String style) {
        try {
            Collection<String> styles = new ArrayList<>(area.getParagraph(paraIndex).getParagraphStyle());
            styles.add(style);
            area.setParagraphStyle(paraIndex, styles);
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean tryRemoveParagraphStyle(StyleClassedTextArea area, int paraIndex, String style) {
        if (paraIndex <= 0) return false;
        try {
            java.util.List<String> styles = area.getParagraph(paraIndex).getParagraphStyle()
                    .stream().filter(st -> !st.equals(style)).collect(Collectors.toList());
            area.setParagraphStyle(paraIndex, styles);
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
}
package com.dse.report.element;

public interface IElement {
    String STYLES_PATH = "src/main/resources/css/report_style.css";

    class COLOR {
        public static String DARK = "bg-dark";

        public static String MEDIUM = "bg-medium";

        public static String LIGHT = "bg-light";

        public static String GREEN = "bg-green";

        public static String RED = "bg-red";

        public static String YELLOW = "bg-yellow";

        public static String WHITE = "";
    }

    class TEXT_STYLE {
        public static String BOLD = "bold";

        public static String ITALIC = "italic";

        public static String NORMAL = "";
    }

    class TEXT_ALIGN {
        public static String LEFT = "";

        public static String CENTER = "center";

        public static String RIGHT = "right";
    }

    String toHtml();
}

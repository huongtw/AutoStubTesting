package com.dse.user_code.envir.type;

import com.dse.config.WorkspaceConfig;

@Deprecated
public class HeaderEnvirUserCode extends AbstractEnvirUserCode {

//    private static HeaderEnvirUserCode instance;

    public HeaderEnvirUserCode() {
        setContent(TEMPLATE);
    }

//    public static HeaderEnvirUserCode getInstance() {
//        if (instance == null) {
//            instance = new HeaderEnvirUserCode();
//            instance.setContent(TEMPLATE);
//        }
//        return instance;
//    }

    @Override
    public void save() {
//        if (path == null)
//            path = new WorkspaceConfig().fromJson().getHeaderUserCodePath();

        super.save();
    }

    private static final String TEMPLATE =
            "#ifndef AKA_HEADER_USER_CODE\n" +
            "#define AKA_HEADER_USER_CODE\n" +
            "/* your code here */\n" +
            "#endif";
}

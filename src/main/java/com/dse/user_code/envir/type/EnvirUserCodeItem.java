package com.dse.user_code.envir.type;

import com.dse.config.WorkspaceConfig;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.util.SpecialCharacter;

import java.io.File;

public class EnvirUserCodeItem extends AbstractEnvirUserCode {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        String normalize = name
                .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE)
                .toUpperCase();

        String templateContent = String.format(TEMPLATE, normalize, normalize);

        setContent(templateContent);
    }

    @Override
    public void save() {
        if (path == null)
            path = new WorkspaceConfig().fromJson().getEnvirUserCodeDirectory()
                    + File.separator + name + EnvironmentUserCode.FILE_EXT;

        super.save();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvirUserCodeItem that = (EnvirUserCodeItem) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }

    private static final String TEMPLATE =
            "#ifndef AKA_DATA_USER_CODE_%s\n" +
            "#define AKA_DATA_USER_CODE_%s\n" +
            "/* your code here */\n" +
            "#endif";
}

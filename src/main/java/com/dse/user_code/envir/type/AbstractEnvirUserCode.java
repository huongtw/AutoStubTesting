package com.dse.user_code.envir.type;

import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.io.File;

public abstract class AbstractEnvirUserCode implements IEnvirUserCode {

    protected String path;
    protected String content = SpecialCharacter.EMPTY;

    @Override
    public void read() {
        if (path == null || !new File(path).exists())
            content = SpecialCharacter.EMPTY;
        else
            content = Utils.readFileContent(path);
    }

    @Override
    public void save() {
        Utils.writeContentToFile(content, path);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }
}

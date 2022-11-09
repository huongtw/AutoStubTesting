package com.dse.user_code.envir.type;

public interface IEnvirUserCode {

    String getPath();

    void setPath(String path);

    String getContent();

    void setContent(String content);

    void save();

    void read();
}

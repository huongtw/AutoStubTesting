package com.dse.user_code.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractUserCode {
    private String content;
    private String name;
    private int id;
    private final List<String> includePaths = new ArrayList<>();

    public AbstractUserCode () {
        super();
        id = new Random().nextInt(1000000);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }
}

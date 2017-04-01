package com.geomslayer.ytranslate.storage;

import io.realm.RealmObject;

public class Language extends RealmObject {

    public interface Field {
        String name = "name";
        String simpleName = "simpleName";
    }

    private String name;
    private String simpleName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

}

package com.geomslayer.ytranslate.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Language {

    @Id
    private String code;

    private String name;

    @Generated(hash = 1582408161)
    public Language(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Generated(hash = 1478671802)
    public Language() {
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

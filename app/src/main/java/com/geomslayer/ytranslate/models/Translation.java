package com.geomslayer.ytranslate.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;

@Entity
public class Translation {

    @Id(autoincrement = true)
    private Long id;

    private String sourceText;
    private String translatedText;

    private String sourceCode;
    private String targetCode;

    @ToOne(joinProperty = "sourceCode")
    private Language source;

    @ToOne(joinProperty = "targetCode")
    private Language target;

    private boolean inHistory;
    private boolean inFavorites;

    private Date moment;

    public Translation emptyInstance() {
        Translation res = new Translation();
        res.setSourceText("");
        res.setTranslatedText("");
        res.setSourceCode(getSourceCode());
        res.setTargetCode(getTargetCode());
        return res;
    }

    public Translation getReversed() {
        Translation res = new Translation();
        res.setSourceText(getTranslatedText());
        res.setTranslatedText(getSourceText());
        res.setSourceCode(getSourceCode());
        res.setTargetCode(getTargetCode());
        return res;
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 664316826)
    private transient TranslationDao myDao;

    @Generated(hash = 814612108)
    public Translation(Long id, String sourceText, String translatedText, String sourceCode,
            String targetCode, boolean inHistory, boolean inFavorites, Date moment) {
        this.id = id;
        this.sourceText = sourceText;
        this.translatedText = translatedText;
        this.sourceCode = sourceCode;
        this.targetCode = targetCode;
        this.inHistory = inHistory;
        this.inFavorites = inFavorites;
        this.moment = moment;
    }

    @Generated(hash = 321689573)
    public Translation() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceText() {
        return this.sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getTranslatedText() {
        return this.translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public boolean isInHistory() {
        return this.inHistory;
    }

    public void setInHistory(boolean inHistory) {
        this.inHistory = inHistory;
    }

    public boolean isInFavorites() {
        return this.inFavorites;
    }

    public void setInFavorites(boolean inFavorites) {
        this.inFavorites = inFavorites;
    }

    public Date getMoment() {
        return this.moment;
    }

    public void setMoment(Date moment) {
        this.moment = moment;
    }

    public Language peekSource() {
        return this.source;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 901930782)
    public Language getSource() {
        String __key = this.sourceCode;
        if (source__resolvedKey == null || source__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LanguageDao targetDao = daoSession.getLanguageDao();
            Language sourceNew = targetDao.load(__key);
            synchronized (this) {
                source = sourceNew;
                source__resolvedKey = __key;
            }
        }
        return source;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1203056913)
    public void setSource(Language source) {
        synchronized (this) {
            this.source = source;
            sourceCode = source == null ? null : source.getCode();
            source__resolvedKey = sourceCode;
        }
    }

    @Generated(hash = 1883880398)
    private transient String source__resolvedKey;

    @Generated(hash = 1937181792)
    private transient String target__resolvedKey;

    public Language peekTarget() {
        return this.target;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1805091483)
    public Language getTarget() {
        String __key = this.targetCode;
        if (target__resolvedKey == null || target__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LanguageDao targetDao = daoSession.getLanguageDao();
            Language targetNew = targetDao.load(__key);
            synchronized (this) {
                target = targetNew;
                target__resolvedKey = __key;
            }
        }
        return target;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1062154316)
    public void setTarget(Language target) {
        synchronized (this) {
            this.target = target;
            targetCode = target == null ? null : target.getCode();
            target__resolvedKey = targetCode;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    public String getSourceCode() {
        return this.sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getTargetCode() {
        return this.targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public boolean getInHistory() {
        return this.inHistory;
    }

    public boolean getInFavorites() {
        return this.inFavorites;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "id=" + id +
                ", sourceText='" + sourceText + '\'' +
                ", translatedText='" + translatedText + '\'' +
                ", sourceCode='" + sourceCode + '\'' +
                ", targetCode='" + targetCode + '\'' +
                ", source=" + source +
                ", target=" + target +
                ", inHistory=" + inHistory +
                ", inFavorites=" + inFavorites +
                ", moment=" + moment +
                '}';
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 618685332)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTranslationDao() : null;
    }
}
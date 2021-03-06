package com.lidroid.xutils.db.table;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.FinderLazyLoader;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: wyouflf
 * Date: 13-9-10
 * Time: 下午7:43
 */
public class Finder extends Column {

    public DbUtils db;

    private String valueColumnName;

    private String targetColumnName;

    protected Finder(Class<?> entityType, Field field) {
        super(entityType, field);

        com.lidroid.xutils.db.annotation.Finder finder = field.getAnnotation(com.lidroid.xutils.db.annotation.Finder.class);
        this.valueColumnName = finder.valueColumn();
        this.targetColumnName = finder.targetColumn();
    }

    public Class<?> getTargetEntityType() {
        return ColumnUtils.getFinderTargetEntityType(this);
    }

    @Override
    public void setValue2Entity(Object entity, String valueStr) {
        Object value = null;
        Class<?> columnType = columnField.getType();
        Object finderValue = TableUtils.getColumnOrId(entity.getClass(), this.valueColumnName).getColumnValue(entity);
        if (columnType.equals(FinderLazyLoader.class)) {
            value = new FinderLazyLoader(this, finderValue);
        } else if (columnType.equals(List.class)) {
            try {
                value = new FinderLazyLoader(this, finderValue).getAllFromDb();
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
        } else {
            try {
                value = new FinderLazyLoader(this, finderValue).getFirstFromDb();
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
        }

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, value);
            } catch (Exception e) {
                LogUtils.e(e.getMessage(), e);
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, value);
            } catch (Exception e) {
                LogUtils.e(e.getMessage(), e);
            }
        }
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getColumnValue(Object entity) {
        return null;
    }

    public Object getFieldValue(Object entity) {
        Object valueObj = null;
        if (entity != null) {
            if (getMethod != null) {
                try {
                    valueObj = getMethod.invoke(entity);
                } catch (Exception e) {
                    LogUtils.e(e.getMessage(), e);
                }
            } else {
                try {
                    this.columnField.setAccessible(true);
                    valueObj = this.columnField.get(entity);
                } catch (Exception e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
        return valueObj;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String getColumnDbType() {
        return "";
    }
}

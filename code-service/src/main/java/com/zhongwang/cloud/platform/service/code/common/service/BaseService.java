package com.zhongwang.cloud.platform.service.code.common.service;

import com.zhongwang.cloud.platform.bamboo.common.exception.NotFoundException;
import com.zhongwang.cloud.platform.service.code.common.entity.BaseEntity;
import com.zhongwang.cloud.platform.service.code.common.repository.BaseRepository;
import com.zhongwang.cloud.platform.service.code.common.util.BeanCopys;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.SYS_COMMON_STATUS.*;
import static java.util.UUID.randomUUID;

/**
 * 服务抽象基类
 */
public abstract class BaseService<T extends BaseEntity> {

    @Autowired
    protected BaseRepository<T> repository;

    /**
     * 处理公共属性信息
     *
     * @param baseEntity
     * @return
     */
    private final <M> M addInfo(BaseEntity baseEntity) {
        Date curDate = new Date();

        if (null == baseEntity.getFlagStatus()) {
            baseEntity.setFlagStatus(VALID);
        }
        if (null == baseEntity.getFlagSort()) {
            baseEntity.setFlagSort(D_FLAG_SORT);
        }
        if (null == baseEntity.getFlagDelete()) {
            baseEntity.setFlagDelete(NOT_DELETE);
        }
        if (null == baseEntity.getMakeTime()) {
            baseEntity.setMakeTime(curDate);
        }
        baseEntity.setModifyTime(curDate);
        return (M) baseEntity;
    }

    /**
     * 新增
     *
     * @param t
     * @return
     */
    protected T insert(T t) {
        return repository.saveAndFlush(addInfo(t));
    }

    /**
     * 修改
     *
     * @param t
     * @return
     * @throws NotFoundException
     */
    protected T update(T t) throws NotFoundException {
        T t_db = repository.findOne(t.getPkid());
        if (null == t_db || t_db.getFlagDelete() == DELETE.shortValue()) {
            throw new NotFoundException("can not found this item");
        }
        t.setMakeUser(t_db.getMakeUser());
        t.setMakeTime(t_db.getMakeTime());
        t.setFlagVersion(t_db.getFlagVersion());
        return repository.saveAndFlush(addInfo(t));
    }

    /**
     * 选择性更新（带用户上下文信息，只更新不为空的字段）
     *
     * @param t
     * @return
     * @throws NotFoundException
     */
    protected T updateBySelective(T t) throws NotFoundException {
        T t_db = repository.findOne(t.getPkid());
        if (null == t_db || t_db.getFlagDelete() == DELETE.shortValue()) {
            throw new NotFoundException("can not found this item");
        }
        BeanCopys.copyPropertiesIgnoreNull(t, t_db);
        return repository.save((T) addInfo(t_db));
    }

    /**
     * 逻辑删除
     *
     * @param pkid
     * @param operator
     * @throws NotFoundException
     */
    protected void deleteLogically(String pkid, String operator) throws NotFoundException {
        T t_db = repository.findOne(pkid);
        if (null == t_db || t_db.getFlagDelete() == DELETE.shortValue()) {
            throw new NotFoundException("");
        }
        t_db.setFlagDelete(DELETE);
        t_db.setFlagDeleteToken(randomUUID().toString().replaceAll("-", ""));
        t_db.setModifyUser(operator);
        repository.saveAndFlush(addInfo(t_db));
    }

    /**
     * 逻辑删除
     *
     * @param obj
     * @throws NotFoundException
     */
    protected void deleteLogically(T obj, String operator) throws NotFoundException {
        this.deleteLogically(obj.getPkid(), operator);
    }

    /**
     * 逻辑删除
     *
     * @throws NotFoundException
     */
    protected void deleteLogically(List<String> pkids, final String operator) throws NotFoundException {
        for (String pkid : pkids) {
            this.deleteLogically(pkid, operator);
        }
    }

}

package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.mapper.TActivityRemarkMapper;
import com.bjpowernode.model.TActivity;
import com.bjpowernode.model.TActivityRemark;
import com.bjpowernode.model.TUser;
import com.bjpowernode.query.ActivityRemarkQuery;
import com.bjpowernode.query.BaseQuery;
import com.bjpowernode.service.ActivityRemarkService;
import com.bjpowernode.util.JWTUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ActivityRemarkServiceImpl implements ActivityRemarkService {

    @Resource
    private TActivityRemarkMapper tActivityRemarkMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveActivityRemark(ActivityRemarkQuery activityRemarkQuery) {
        TActivityRemark tActivityRemark = new TActivityRemark();

        //把ActivityRemarkQuery对象里面的属性数据复制到TActivityRemark对象里面去(复制要求：两个对象的属性名相同，属性类型要相同，这样才能复制)
        BeanUtils.copyProperties(activityRemarkQuery, tActivityRemark);

        tActivityRemark.setCreateTime(new Date()); //创建时间

        //登录人的id
        Integer loginUserId = JWTUtils.parseUserFromJWT(activityRemarkQuery.getToken()).getId();
        tActivityRemark.setCreateBy(loginUserId); //创建人

        return tActivityRemarkMapper.insertSelective(tActivityRemark);
    }

    @Override
    public PageInfo<TActivityRemark> getActivityRemarkByPage(Integer current, ActivityRemarkQuery activityRemarkQuery) {
        // 1.设置PageHelper
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        // 2.查询
        List<TActivityRemark> list = tActivityRemarkMapper.selectActivityRemarkByPage(activityRemarkQuery);
        // 3.封装分页数据到PageInfo
        PageInfo<TActivityRemark> info = new PageInfo<>(list);
        return info;
    }

    @Override
    public TActivityRemark getActivityRemarkById(Integer id) {
        return tActivityRemarkMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateActivityRemark(ActivityRemarkQuery activityRemarkQuery) {
        TActivityRemark tActivityRemark = new TActivityRemark();

        //把ActivityRemarkQuery对象里面的属性数据复制到TActivityRemark对象里面去(复制要求：两个对象的属性名相同，属性类型要相同，这样才能复制)
        BeanUtils.copyProperties(activityRemarkQuery, tActivityRemark);

        tActivityRemark.setEditTime(new Date()); //编辑时间

        //登录人的id
        Integer loginUserId = JWTUtils.parseUserFromJWT(activityRemarkQuery.getToken()).getId();
        tActivityRemark.setEditBy(loginUserId); //创建人

        return tActivityRemarkMapper.updateByPrimaryKeySelective(tActivityRemark);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int delActivityRemarkById(Integer id) {
        //逻辑删除：不删数据，只是修改一下数据的状态，数据依然还在表里面；
        //物理删除：真正的把数据从表里面删掉
        TActivityRemark tActivityRemark = new TActivityRemark();
        tActivityRemark.setId(id);
        tActivityRemark.setDeleted(1); //删除状态（null或者0正常，1删除）
        return tActivityRemarkMapper.updateByPrimaryKeySelective(tActivityRemark);
    }
}

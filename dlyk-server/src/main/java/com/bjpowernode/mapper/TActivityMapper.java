package com.bjpowernode.mapper;

import com.bjpowernode.commons.DataScope;
import com.bjpowernode.model.TActivity;
import com.bjpowernode.query.ActivityQuery;
import com.bjpowernode.query.BaseQuery;

import java.util.List;

public interface TActivityMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(TActivity record);

    int insertSelective(TActivity record);

    TActivity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TActivity record);

    int updateByPrimaryKey(TActivity record);

    @DataScope(tableAlias = "ta", tableField = "owner_id")
    List<TActivity> selectActivityByPage(ActivityQuery query);

    TActivity selectDetailByPrimaryKey(Integer id);

    List<TActivity> selecOngoingActivity();

    Integer selectByCount();
}
package com.bjpowernode.mapper;

import com.bjpowernode.model.TRole;

import java.util.List;

public interface TRoleMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(TRole record);

    int insertSelective(TRole record);

    TRole selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TRole record);

    int updateByPrimaryKey(TRole record);

    List<TRole> selectByUserId(Integer userId);
}
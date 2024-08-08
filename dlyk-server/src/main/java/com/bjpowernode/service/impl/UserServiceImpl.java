package com.bjpowernode.service.impl;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.manager.RedisManager;
import com.bjpowernode.mapper.TPermissionMapper;
import com.bjpowernode.mapper.TRoleMapper;
import com.bjpowernode.mapper.TUserMapper;
import com.bjpowernode.model.TActivityRemark;
import com.bjpowernode.model.TPermission;
import com.bjpowernode.model.TRole;
import com.bjpowernode.model.TUser;
import com.bjpowernode.query.BaseQuery;
import com.bjpowernode.query.UserQuery;
import com.bjpowernode.service.UserService;
import com.bjpowernode.util.CacheUtils;
import com.bjpowernode.util.JWTUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private TUserMapper tUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private TRoleMapper tRoleMapper;

    @Resource
    private RedisManager redisManager;

    @Resource
    private TPermissionMapper tPermissionMapper;

    /**
     * 登录查询
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        TUser tUser = tUserMapper.selectByLoginAct(username);
        if (tUser == null) {
            throw new UsernameNotFoundException("登录账号不存在");
        }

        //查询一下当前用户的角色
        List<TRole> tRoleList = tRoleMapper.selectByUserId(tUser.getId());
        //字符串的角色列表
        List<String> stringRoleList = new ArrayList<>();
        tRoleList.forEach(tRole -> {
            stringRoleList.add(tRole.getRole());
        });
        tUser.setRoleList(stringRoleList); //设置用户的角色

        //查询一下该用户有哪些菜单权限
        List<TPermission> menuPermissionList = tPermissionMapper.selectMenuPermissionByUserId(tUser.getId());
        tUser.setMenuPermissionList(menuPermissionList);

        //查询一下该用户有哪些功能权限
        List<TPermission> buttonPermissionList = tPermissionMapper.selectButtonPermissionByUserId(tUser.getId());
        List<String> stringPermissionList = new ArrayList<>();
        buttonPermissionList.forEach(tPermission -> {
            stringPermissionList.add(tPermission.getCode());//权限标识符
        });
        tUser.setPermissionList(stringPermissionList);//设置用户的权限标识符

        return tUser;
    }

    @Override
    public PageInfo<TUser> getUserByPage(Integer current) {
        // 1.设置PageHelper
        PageHelper.startPage(current, Constants.PAGE_SIZE);
        // 2.查询
        List<TUser> list = tUserMapper.selectUserByPage(BaseQuery.builder().build());
        // 3.封装分页数据到PageInfo
        PageInfo<TUser> info = new PageInfo<>(list);
        return info;
    }

    @Override
    public TUser getUserById(Integer id) {
        return tUserMapper.selectDetailById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveUser(UserQuery userQuery) {

        TUser tUser = new TUser();

        //把UserQuery对象里面的属性数据复制到TUser对象里面去(复制要求：两个对象的属性名相同，属性类型要相同，这样才能复制)
        BeanUtils.copyProperties(userQuery, tUser);

        tUser.setLoginPwd(passwordEncoder.encode(userQuery.getLoginPwd())); //密码加密
        tUser.setCreateTime(new Date()); //创建时间

        //登录人的id
        Integer loginUserId = JWTUtils.parseUserFromJWT(userQuery.getToken()).getId();
        tUser.setCreateBy(loginUserId); //创建人

        return tUserMapper.insertSelective(tUser);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUser(UserQuery userQuery) {
        TUser tUser = new TUser();

        //把UserQuery对象里面的属性数据复制到TUser对象里面去(复制要求：两个对象的属性名相同，属性类型要相同，这样才能复制)
        BeanUtils.copyProperties(userQuery, tUser);

        if (StringUtils.hasText(userQuery.getLoginPwd())) {
            tUser.setLoginPwd(passwordEncoder.encode(userQuery.getLoginPwd())); //密码加密
        }

        tUser.setEditTime(new Date()); //编辑时间

        //登录人的id
        Integer loginUserId = JWTUtils.parseUserFromJWT(userQuery.getToken()).getId();
        tUser.setEditBy(loginUserId); //创建人

        return tUserMapper.updateByPrimaryKeySelective(tUser);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int delUserById(Integer id) {
        return tUserMapper.deleteByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelUserIds(List<String> idList) {
        return tUserMapper.deleteByIds(idList);
    }

    @Override
    public List<TUser> getOwnerList() {
        //1、从redis查询
        //2、redis查不到，就从数据库查询，并且把数据放入redis（5分钟过期）
        return CacheUtils.getCacheData(() -> {
            //生产，从缓存redis查询数据
            return (List<TUser>)redisManager.getValue(Constants.REDIS_OWNER_KEY);
        },
        () -> {
            //生产，从mysql查询数据
            return (List<TUser>)tUserMapper.selectByOwner();
        },
        (t) -> {
            //消费，把数据放入缓存redis
            redisManager.setValue(Constants.REDIS_OWNER_KEY, t);
        }
       );
    }
}

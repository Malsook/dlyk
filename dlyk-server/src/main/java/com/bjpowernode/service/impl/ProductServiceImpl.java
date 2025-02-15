package com.bjpowernode.service.impl;

import com.bjpowernode.mapper.TProductMapper;
import com.bjpowernode.model.TProduct;
import com.bjpowernode.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private TProductMapper tProductMapper;

    @Override
    public List<TProduct> getAllOnSaleProduct() {
        return tProductMapper.selectAllOnSaleProduct();
    }
}

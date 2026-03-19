package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 根据类型查询商品
     */
//    @Select("SELECT * FROM product WHERE product_type = #{productType} AND status = 1 AND stock_quantity > 0 ORDER BY create_time DESC")
//    List<Product> selectByType(@Param("productType") Integer productType);
}
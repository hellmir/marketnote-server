package com.personal.marketnote.product.port.out.productoption;

import com.personal.marketnote.product.domain.option.ProductOption;

import java.util.List;

public interface FindProductOptionPort {

    List<ProductOption> findByProductIdAndIds(Long productId, List<Long> ids);
}

package com.comonon.mall.search.service;

import com.comonon.mall.search.client.ProductClient;
import com.comonon.mall.search.client.dto.SpuIndexDto;
import com.comonon.mall.search.document.ProductDocument;
import com.comonon.mall.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexService {

    private final ProductSearchRepository repository;
    private final ProductClient productClient;

    public void indexSpu(Long spuId) {
        SpuIndexDto dto = productClient.fetchForIndex(spuId);
        if (dto == null || dto.getMinPrice() == null) {
            deleteSpu(spuId);
            return;
        }
        repository.save(toDoc(dto));
        log.info("indexed spu id={}", spuId);
    }

    public void deleteSpu(Long spuId) {
        repository.deleteById(spuId);
        log.info("removed spu from index id={}", spuId);
    }

    /** 手动全量重建：先清空再导入。 */
    public int rebuildAll() {
        repository.deleteAll();
        return loadAllOnSale();
    }

    /** 首次建索引时使用，不先清空已有数据。 */
    public int loadAllOnSale() {
        int page = 1;
        int size = 100;
        int indexed = 0;
        while (true) {
            List<SpuIndexDto> batch = productClient.fetchOnSalePage(page, size);
            if (batch.isEmpty()) {
                break;
            }
            List<ProductDocument> docs = new ArrayList<>();
            for (SpuIndexDto dto : batch) {
                if (dto.getMinPrice() != null) {
                    docs.add(toDoc(dto));
                }
            }
            repository.saveAll(docs);
            indexed += docs.size();
            if (batch.size() < size) {
                break;
            }
            page++;
        }
        log.info("index load done count={}", indexed);
        return indexed;
    }

    private ProductDocument toDoc(SpuIndexDto dto) {
        ProductDocument doc = new ProductDocument();
        doc.setId(dto.getId());
        doc.setTitle(dto.getTitle());
        doc.setSubtitle(dto.getSubtitle());
        doc.setCategoryId(dto.getCategoryId());
        doc.setProductType(dto.getProductType());
        doc.setMainImage(dto.getMainImage());
        doc.setMinPrice(dto.getMinPrice());
        doc.setMaxPrice(dto.getMaxPrice());
        doc.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        return doc;
    }
}

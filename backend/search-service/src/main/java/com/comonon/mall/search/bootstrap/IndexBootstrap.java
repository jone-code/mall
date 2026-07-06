package com.comonon.mall.search.bootstrap;

import com.comonon.mall.search.document.ProductDocument;
import com.comonon.mall.search.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * 仅在 ES 中尚不存在商品索引时做首次全量导入；后续重启依赖 ES 持久化数据 + 商品上下架增量同步。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexBootstrap implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductIndexService indexService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            if (indexOps.exists()) {
                log.info("search index already exists, skip bootstrap");
                return;
            }
            log.info("search index not found, first-time bootstrap load...");
            indexService.loadAllOnSale();
        } catch (Exception e) {
            log.warn("search index bootstrap skipped: {}", e.getMessage());
        }
    }
}

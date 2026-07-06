package com.comonon.mall.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.comonon.mall.search.document.ProductDocument;
import com.comonon.mall.search.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchQueryService {

    private final ElasticsearchOperations elasticsearchOperations;

    public PageResult<PageResult.ProductHit> search(String keyword, Long categoryId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);

        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (StringUtils.hasText(keyword)) {
            bool.must(Query.of(q -> q.multiMatch(MultiMatchQuery.of(m -> m
                    .query(keyword.trim())
                    .fields("title^3", "subtitle")))));
        } else {
            bool.must(Query.of(q -> q.matchAll(m -> m)));
        }
        if (categoryId != null) {
            bool.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(bool.build()))
                .withPageable(PageRequest.of(p - 1, s))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        List<PageResult.ProductHit> list = new ArrayList<>();
        for (SearchHit<ProductDocument> hit : hits) {
            ProductDocument doc = hit.getContent();
            PageResult.ProductHit vo = new PageResult.ProductHit();
            vo.setId(doc.getId());
            vo.setTitle(doc.getTitle());
            vo.setSubtitle(doc.getSubtitle());
            vo.setMainImage(doc.getMainImage());
            vo.setProductType(doc.getProductType());
            vo.setMinPrice(doc.getMinPrice());
            vo.setMaxPrice(doc.getMaxPrice());
            list.add(vo);
        }
        return PageResult.of(list, p, s, hits.getTotalHits());
    }
}

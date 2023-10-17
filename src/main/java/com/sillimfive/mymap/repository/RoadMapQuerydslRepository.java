package com.sillimfive.mymap.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sillimfive.mymap.domain.roadmap.RoadMap;
import com.sillimfive.mymap.web.dto.roadmap.RoadMapResponseDto;
import com.sillimfive.mymap.web.dto.roadmap.RoadMapSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sillimfive.mymap.domain.QCategory.category;
import static com.sillimfive.mymap.domain.QImage.image;
import static com.sillimfive.mymap.domain.QUser.user;
import static com.sillimfive.mymap.domain.roadmap.QRoadMap.roadMap;
import static com.sillimfive.mymap.domain.roadmap.QRoadMapNode.roadMapNode;

@Repository
@RequiredArgsConstructor
public class RoadMapQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * fetch join for RoadMapNodeList
     * Caution : no fetch join with tag list
     *
     * @param id - roadMapId
     * @return
     */
    public Optional<RoadMap> findByIdWithNode(Long id) {
        RoadMap result = queryFactory
                .selectFrom(roadMap)
                .join(roadMap.roadMapNodes, roadMapNode).fetchJoin()
                .join(roadMap.user, user).fetchJoin()
                .join(roadMap.creator).fetchJoin()
                .join(roadMap.category, category).fetchJoin()
                .join(roadMap.image, image).fetchJoin()
                .where(roadMap.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    public PageImpl<RoadMapResponseDto> searchList(RoadMapSearch conditions, Pageable pageable) {
        List<RoadMapResponseDto> dtoList = queryFactory
                .selectFrom(roadMap)
                .join(roadMap.category, category).fetchJoin()
                .join(roadMap.image, image).fetchJoin()
                .where(categoryIdEq(conditions.getCategoryId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream().map(RoadMapResponseDto::new)
                .collect(Collectors.toList());

        Long totalCount = queryFactory
                .select(roadMap.count())
                .from(roadMap)
                .join(roadMap.category, category)
                .join(roadMap.image, image)
                .where(categoryIdEq(conditions.getCategoryId()))
                .fetchOne();

        return new PageImpl<>(dtoList, pageable, totalCount);

    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId == null ? null : roadMap.category.id.eq(categoryId);
    }
}

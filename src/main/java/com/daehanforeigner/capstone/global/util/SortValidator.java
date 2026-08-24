package com.daehanforeigner.capstone.global.util;

import com.daehanforeigner.capstone.global.exception.CustomException;
import com.daehanforeigner.capstone.global.exception.ErrorCode;
import org.springframework.data.domain.Pageable;

import java.util.Set;

// 정렬 파라미터 검증.
//
// @Query를 쓰는 조회 메서드는 Spring Data가 JPQL 뒤에 order by를 그대로 덧붙이고,
// 없는 필드면 Hibernate가 UnknownPathException을 던진다.
// 이건 InvalidDataAccessApiUsageException으로 감싸져 500이 되는데,
// 그 예외를 전역에서 잡으면 정렬과 무관한 JPA 오용까지 400으로 바뀌어 진짜 버그를 감춘다.
//
// 그래서 쿼리를 실행하기 전에 허용된 필드인지 먼저 확인한다
public final class SortValidator {

    private SortValidator() {
    }

    // 허용 목록에 없는 필드로 정렬을 요청하면 400으로 응답한다
    public static void validate(Pageable pageable, Set<String> allowedProperties) {
        pageable.getSort().forEach(order -> {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new CustomException(ErrorCode.INVALID_SORT_PROPERTY);
            }
        });
    }
}

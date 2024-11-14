## 실행

```shell
./gradlew bootRun --args='--spring.profiles.active=init-data'
```

## 빌드

```shell
./gradlew build
```

## 테스트

```shell
./gradlew test
```

integration test 는 [IntegrationTest.kt](src/test/kotlin/com/github/jyc228/musinsa/IntegrationTest.kt) 를 상속했습니다.

## openapi

http://localhost:8080/swagger-ui/index.html

## h2-console

http://localhost:8080/h2-console

## 구현

api 실패 처리 : [MusinsaExceptionHandler.kt](src/main/kotlin/com/github/jyc228/musinsa/MusinsaExceptionHandler.kt)

### 구현 1) - 카테고리 별 최저가격 브랜드와 상품 가격, 총액을 조회하는 API

- 상품이 없는 카테고리 요구사항이 없음 : 응답에 포함 안되도록 했습니다.
- 동일 가격 상품 : 예제랑 동일하게 나오게 하기 위하여 뒤쪽에 배치된 브랜드 상품이 나오도록 했습니다.

[StatisticsController.kt](src/main/kotlin/com/github/jyc228/musinsa/domain/statistics/StatisticsController.kt)
`/api/statistics/category-cheaper-product`

### 구현 2) - 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격에 판매하는 브랜드와 카테고리의 상품가격, 총액을 조회하는 API

- 상품이 없는 카테고리 요구사항이 없음 : 모든 카테고리가 있는 브랜드만 최저 가격 계산했습니다.

[StatisticsController.kt](src/main/kotlin/com/github/jyc228/musinsa/domain/statistics/StatisticsController.kt)
`/api/statistics/brand-cheaper-product`

### 구현 3) - 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격을 조회하는 API

- 상품이 없는 카테고리 요구사항이 없음 : 카테고리에 상품이 없는 경우, 에러를 응답하도록 했습니다.

[StatisticsController.kt](src/main/kotlin/com/github/jyc228/musinsa/domain/statistics/StatisticsController.kt)
`/api/statistics/category-product/{categoryName}`

### 구현 4) - 브랜드 및 상품을 추가 / 업데이트 / 삭제하는 API

- 상품 수정 상세한 요구사항 없음 : 카테고리, 브랜드, 가격 전부 수정 가능하다고 보고 개발했습니다.

[BrandController.kt](src/main/kotlin/com/github/jyc228/musinsa/domain/brand/BrandController.kt)
[ProductController.kt](src/main/kotlin/com/github/jyc228/musinsa/domain/product/ProductController.kt)
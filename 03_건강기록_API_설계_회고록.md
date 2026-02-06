# 건강 기록 API 설계 회고록

## 프로젝트 개요

건강 기록 데이터를 저장하는 REST API를 설계하면서 대용량 데이터 처리와 인증 방식에 대한 고민과 결정 과정을 정리한 회고록입니다.

---

## 1. 인증 아키텍처 설계

### 선택: OAuth2 인증 서버 분리

**구조:**
```
Client → Auth Server (회원가입/로그인) → JWT 발급
Client → Service Servers (Record, etc.) → JWT 검증만 수행
```

**의도:**
- 인증 로직을 중앙화하여 각 서비스의 책임 분리
- 인증 서버에서 JWT 토큰을 발급하고, 각 서비스는 검증만 담당
- MSA 구조에서 확장성과 유지보수성 향상

**결과:**
- ✅ 각 서비스가 인증 로직 없이 토큰 검증만으로 경량화
- ✅ 인증 정책 변경 시 Auth Server만 수정하면 됨
- ⚠️ 네트워크 레이턴시 추가 (서비스 간 통신)

**배운 점:**
- OAuth2 + JWT 조합은 분산 시스템에서 효과적
- 토큰 검증 방식 (public key 공유 vs API 호출)에 따라 성능 차이 발생
- Refresh Token 전략이 중요함

---

## 2. 대용량 데이터 처리 시도

### 문제 상황

**요구사항:**
- 한 번의 API 요청으로 1,000건 이상의 건강 기록 데이터 저장
- 메모리 효율적인 처리 방식 필요

### 시도 1: Stream 방식 도입 검토

**초기 계획:**
```java
// JSON을 스트리밍으로 읽으면서 배치 처리
JsonParser parser = factory.createParser(request.getInputStream());
while (parser.nextToken() != null) {
    // 100건씩 끊어서 처리
}
```

**의도:**
- 전체 JSON을 메모리에 올리지 않고 청크 단위로 처리
- 메모리 사용량: 10MB → 100KB 절감 기대

### 시도 2: JSON 구조와의 충돌

**요청 데이터 구조:**
```json
{
  "recordkey": "...",
  "lastUpdate": "...",
  "data": {
    "memo": "...",
    "source": {...},     // Source 먼저 저장 필요
    "entries": [...]     // Record는 Source 참조 필요
  }
}
```

**문제 발견:**
1. **순서 보장 불가**: JSON 객체의 필드 순서는 보장되지 않음
2. **의존성 문제**: `entries` 처리 전에 `source`가 필요하지만, `entries`가 먼저 올 수 있음
3. **해결 시도**: 전체를 메모리에 올려서 순서 무관하게 처리
   ```java
   Map<String, Object> rootMap = objectMapper.readValue(
       request.getInputStream(),
       new TypeReference<>() {}
   );
   ```

**결론:**
- 결국 전체 JSON을 메모리에 올려야 함
- Stream 방식의 메모리 이점이 사라짐
- `@RequestBody`와 메모리 사용량 동일

---

## 3. 최종 결정: 단순한 방식 선택

### @RequestBody 방식으로 회귀

```java
@PostMapping("/records")
public ApiResponse<Void> save(
    @AuthenticationPrincipal Jwt jwt,
    @RequestBody RecordRequestDto dto
) {
    // 1. Source 저장
    Source source = prepareSource(dto);
    
    // 2. Entries 배치 처리
    List<Record> batch = new ArrayList<>(100);
    for (Entry entry : dto.data().entries()) {
        batch.add(createRecord(entry, source));
        
        if (batch.size() >= 100) {
            recordService.saveAllInNewTransaction(batch);
            batch.clear();
        }
    }
}
```

**선택 이유:**
1. **1,000건 정도는 메모리 문제 없음** (~1-2MB)
2. **코드 복잡도 감소** (100줄 → 30줄)
3. **타입 안전성**: DTO 사용으로 컴파일 타임 체크
4. **유지보수 용이**: 가독성과 디버깅 편의성
5. **Validation 가능**: `@Valid` 사용 가능

---

## 4. 남은 고민: 이벤트 기반 처리

### 현재 방식: 동기 배치 처리

```java
// 100건씩 묶어서 동기로 저장
if (batch.size() >= 100) {
    recordService.saveAllInNewTransaction(batch);
}
```

**장점:**
- ✅ 간단하고 직관적
- ✅ 트랜잭션 관리 명확
- ✅ 에러 처리 즉각 가능

**단점:**
- ⚠️ API 응답 시간 증가 (1,000건 처리 동안 대기)
- ⚠️ 데이터베이스 부하 집중

### 대안: 이벤트 기반 비동기 처리

```java
// 1. 즉시 응답
eventPublisher.publishEvent(new RecordSaveEvent(dto));
return ApiResponse.success(jobId);

// 2. 백그라운드에서 처리
@EventListener
public void handleRecordSave(RecordSaveEvent event) {
    // 비동기로 배치 저장
}
```

**예상 장점:**
- ✅ API 응답 시간 단축
- ✅ 부하 분산 가능
- ✅ 재시도 로직 구현 용이

**예상 단점:**
- ⚠️ 복잡도 증가 (이벤트 처리, 상태 관리)
- ⚠️ 에러 처리 복잡 (사용자에게 실패 알림 방법)
- ⚠️ 모니터링 필요 (Job 상태 추적)

**결론:**
- 현재는 동기 방식 유지
- 데이터량이 10,000건 이상으로 증가하거나, 응답 시간 문제 발생 시 비동기 전환 고려

---

## 5. Stream 방식이 유효한 경우

회고를 통해 Stream 방식이 의미 있는 상황을 정리:

### ✅ Stream이 효과적인 경우

**1. 배열이 루트에 있는 경우:**
```json
[
  {"id": 1, "data": "..."},
  {"id": 2, "data": "..."},
  ...
]
```

**2. 메타데이터와 배열이 명확히 분리된 경우:**
```json
{
  "metadata": {...},  // 순서 보장
  "entries": [...]    // 스트리밍 처리
}
```

**3. 데이터가 10,000건 이상:**
- 메모리 압박이 실제로 발생하는 수준

**4. 비동기 Job 구조:**
- S3 업로드 → 백그라운드 처리 → 상태 조회

### ❌ Stream이 불필요한 경우

- JSON 객체에 순서 의존적 필드가 섞여 있는 경우
- 데이터량이 적당한 경우 (수백~수천 건)
- 요구사항보다 구현 복잡도가 높은 경우

---

## 6. 핵심 교훈

### 기술 선택

1. **"최신 기술"보다 "적합한 기술"**
   - Stream 방식이 멋져 보여도, 구조상 맞지 않으면 의미 없음
   - 단순한 `@RequestBody`가 정답일 수 있음

2. **조기 성능 최적화의 위험**
   - 1,000건은 충분히 빠름
   - 실제 병목이 확인된 후 최적화해도 늦지 않음

3. **JSON 구조의 중요성**
   - API 설계 시 처리 방식을 고려한 구조 설계 필요
   - 순서 의존성이 있다면 구조 자체를 변경해야 함

### 아키텍처 설계

1. **인증/인가는 중앙화가 효과적**
   - OAuth2 + JWT 조합은 MSA에서 검증됨
   - 각 서비스는 비즈니스 로직에만 집중

2. **동기 → 비동기 전환은 필요시점에**
   - 초기엔 단순한 동기 방식으로 시작
   - 성능 문제 확인 후 단계적 개선

3. **트레이드오프 명확히 하기**
   - 복잡도 vs 성능
   - 응답 속도 vs 처리 완료 보장
   - 개발 속도 vs 확장성

---

## 7. 다음 단계

### 단기 (1-2개월)

- [x] OAuth2 인증 서버 안정화
- [x] 배치 저장 로직 구현
- [ ] API 응답 시간 모니터링 추가
- [ ] 실패한 배치 DLQ(Dead Letter Queue) 처리 검증

### 중기 (3-6개월)

- [ ] 데이터량 증가 추이 모니터링
- [ ] 10,000건 이상 처리 시나리오 성능 테스트
- [ ] 필요 시 비동기 Job 구조로 전환 검토

### 장기 (6개월 이후)

- [ ] 메시징 시스템 도입 (Kafka, RabbitMQ)
- [ ] CQRS 패턴 적용 검토
- [ ] 이벤트 소싱 도입 가능성 검토

---

## 8. 마치며

> "완벽한 설계는 없다. 상황에 맞는 선택이 있을 뿐이다."

이번 프로젝트에서 Stream 방식을 시도하고 포기한 과정은 실패가 아니라 학습이었습니다. 

**중요한 것:**
- 기술 스택 선택 시 실제 문제에 집중
- 복잡도와 이득의 균형 유지
- 필요할 때 개선하는 단계적 접근

**앞으로:**
- 실제 사용자 데이터 패턴 분석
- 병목 지점 모니터링
- 데이터 기반 의사결정

---

**작성일**: 2026-02-06  
**작성자**: Healthcare API Team  
**다음 리뷰**: 2026-05-06

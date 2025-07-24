# Claude Code Session Memory

## Reservation 프로젝트 목적 및 방향성

### 프로젝트 목적
- **학습 목적**: Kotlin/Java, Spring MVC/WebFlux 간의 비교 학습을 위한 교육용 프로젝트
- **코드 품질**: 운영 서비스로 발전시키지는 않지만, **운영 서비스급의 코드 품질**을 추구
- **비교 학습**: 동일한 기능을 다양한 기술 스택으로 구현하여 실제 차이점과 장단점을 체험
- **실무 패턴**: 프로덕션 환경에서 사용되는 엔터프라이즈 패턴과 베스트 프랙티스 적용

### 기술 비교 목표
1. **Kotlin vs Java**
   - 언어적 특성과 문법 차이
   - 개발 생산성과 코드 가독성
   - Null Safety, Extension Functions, Data Classes 등
   - Coroutines vs Virtual Threads

2. **Spring MVC vs WebFlux**
   - Blocking I/O vs Non-blocking I/O
   - Thread-per-request vs Event Loop
   - Traditional Testing vs Reactive Testing
   - Error Handling 패턴 차이
   - Backpressure와 성능 특성

### 구현 방향
- 각 기능을 **4가지 조합**(Java MVC, Java WebFlux, Kotlin MVC, Kotlin WebFlux)으로 구현
- 동일한 비즈니스 로직을 다른 패러다임으로 표현하여 **실제 차이점 체험**
- 프로덕션 환경의 요구사항(보안, 캐싱, 모니터링, 테스팅)을 모두 포함
- 각 구현별 성능, 가독성, 유지보수성 비교 문서화

### 학습 우선순위
1. **Phase 1**: Repository 계층, Service 계층, Error Handling, Testing
2. **Phase 2**: Security, Caching, Event Architecture, Monitoring  
3. **Phase 3**: API Documentation, Performance Testing, CI/CD

이 프로젝트는 **학습을 위한 고품질 레퍼런스 구현**으로, 실제 운영보다는 **기술 비교와 패턴 학습**에 중점을 둡니다.

### 작업 방식
- **순서대로 작업**: Phase 1 → Phase 2 → Phase 3 순서로 진행
- **세밀한 커밋**: 각 기능별, 기술 스택별로 세분화된 커밋
- **자동 푸시**: 커밋 후 자동으로 GitHub에 푸시 (별도 확인 불필요)
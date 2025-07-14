# Spring MCP
---

- Spring AI에서 제공하는 **Context-Aware Prompting** 메커니즘으로, 모델이 단순한 프롬프트 처리만 하는 것이 아니라, 외부 문맥 정보 (Ex. 벡터 검색 결과)를 포함해서 프롬프트를 구성할 수 있도록 하는 즉, **Prompt + Context + Options을 하나의 구조로 묶는** 표준화된 구조이다.
- Model Context Protocol (MCP)은 LLM 애플리케이션과 외부 데이터 소스 및 도구들 간의 원활한 통합을 가능하게 하는 개방형 프로토콜입니다. AI 기반 IDE, 채팅 인터페이스, 커스텀 AI 워크플로우 등에서 LLM이 필요한 컨텍스트와 연결하기 위한 표준화된 방법을 제공합니다.
- RAG를 구현할 때, context-aware 모델 요청을 쉽게 만들 수 있습니다.
- **주요 구성 요소**
    - Spring AI에서 제공하는 VectorStore, RetrievalAugmentor, ModelRequest, ModelResponse 등이 그 기반을 이룹니다.
    - **Prompt**
        - 질문과 기본 입력을 포함한 텍스트
    - **Context**
        - 외부 지식 소스에서 검색된 문서들
    - **ModelRequest**
        - Prompt + Context + 옵션 설정 포함
    - **ModelResponse**
        - 모델 응답, 사용된 문맥 포함 가능
- **대표적인 사용 시나리오**
    - **RAG (검색 기반 응답 생성)**
        - 질문을 벡터로 변환하여 관련 문서를 찾고 그 문서를 프롬프트에 포함시켜 응답을 생성하는 구조
        - 예: SSG API 문서 벡터화 → 검색 → GPT 응답에 삽입
    - **ChatBot with 기억 기능**
        - 과거 대화 내용을 문맥으로 전달하여 일관성 있는 답변 제공
    - **문서 요약/분석**
        - 특정 문서나 섹션을 프롬프트에 삽입하여 요약 요청

### Step 0: MCP 연동

---

- **Claude 설치 및 설정**
    - Claude Desktop 설치
        - https://claude.ai/download
    - 로그인 → 설정 → 개발자 → 설정 편집
        - claude_desktop_config.json 편집 및 저장 후 Claude 재실행

            ```json
            // 이 내용으로 저장
            {
            	"mcpServers": {
            		"jetbrains": {
            			"command": "npx",
            			"args": ["-y", "@jetbrains/mcp-proxy"]
            		}
            	}
            }
            ```

        - Jetbrains 연결 확인

      ![스크린샷 2025-04-01 오후 6.10.09.png](attachment:db3f2db1-b380-4016-a226-8ae5ebde415a:스크린샷_2025-04-01_오후_6.10.09.png)

- **Jetbrains 설정**
    - Spring Boot 프로젝트 생성
    - Dependencies 추가 시 Model Context Protocol Server 검색 후 추가 (추후 Plugins로 추가 가능)

      ![스크린샷 2025-04-01 오후 6.12.22.png](attachment:f8eb958c-3a64-479e-b11d-d6903373bf94:스크린샷_2025-04-01_오후_6.12.22.png)

    - Plugins → MCP Server 검색 후 설치

      ![스크린샷 2025-04-01 오후 6.17.37.png](attachment:a779a3f9-be6e-4c3c-9836-58ced2456fc0:스크린샷_2025-04-01_오후_6.17.37.png)

- Claude에 “**IDE에서 내 프로젝트를 확인하고 프로젝트에서 지원하는 모든 API를 알려주세요.**” 질문 후 **이 채팅 허용**

  ![스크린샷 2025-04-01 오후 6.22.20.png](attachment:696087d3-eb53-41ad-8eed-ab3465528abc:스크린샷_2025-04-01_오후_6.22.20.png)

- 이렇게 되면 Claude와 Jetbrains와의 연동이 완료된다.

### Step 1: 기본 URL 단축 서비스 구현

---

- 해당 요청 사항을 그대로 Claude에 복사하여 요청한다.

    ```
    urlShortener 서비스 생성
    POST /api/shorten 구현
    문자와 숫자로만 된, 문자로 시작하는 랜덤 6자리 문자열 키를 생성해서 in-memory hashmap에 저장하고 200 OK와 함께 단축 URL을 반환
    GET /api/shorten/{shortKey} 구현
    Endpoint로 단축 키를 받으면 해당하는 원본 URL 반환
    Controller, Service, Repository 레이어로 구조화
    편의성 기능 추가
    수동으로 테스트할 수 있는 HTTP-Client 파일 생성
    주요 흐름에 Info 로그 추가
    로깅은 io.github.oshai:kotlin-logging-jvm dependency 사용
    ```

- Dependencies가 추가되었으니 Gradle을 리프레시 해준다.
- Build 문제가 있을 경우 해당 오류 내용을 다시 질문하여 자동으로 수정한다.

### Step 2: env 별로 데이터 저장소 분리

---

- 해당 요청 사항을 그대로 Claude에 복사하여 요청한다.

    ```
    local env 일 때는 지금의 로직을 유지
    local_postgres env 일 때는 exposed를 쓰고 싶으니 dependency 추가 명령
    local_postgres env에 쓰일 DB는 이미 docker-compose로 띄워져 있으며, 접속 정보는 docker/docker-compose.yml에 있음
    src/main/resources/application.yml에 env 추가
    local env: 현재 in-memory hashmap 사용 유지
    local_postgres env: Exposed를 사용하여 PostgresSQL DB 연결
    docker/docker-compose.yml의 PostgresSQL 접속 정보 활용
    ```

- Dependencies가 추가되었으니 Gradle을 리프레시 해준다.
- Build 문제가 있을 경우 해당 오류 내용을 다시 질문하여 자동으로 수정한다.

### 실행 방법

#### 1. 로컬에서 실행

```bash
./gradlew bootRun
```

#### 2. Docker Compose로 실행 (권장)

**사전 준비:**

1.  Docker Desktop 설치 및 실행
2.  `docker-compose.yml` 파일의 `SPRING_AI_OPENAI_API_KEY`를 실제 OpenAI API 키로 교체

**실행 명령어:**

```bash
docker compose up --build
```

애플리케이션은 `http://localhost:8080`에서 실행됩니다. MySQL은 `localhost:3306`, ChromaDB는 `localhost:8000`에서 접근 가능합니다.

### 로깅 설정

애플리케이션 로그는 `logback-spring.xml` 설정을 통해 JSON 형식으로 출력됩니다. 이는 로그 분석 도구에서 쉽게 파싱하고 활용할 수 있도록 합니다.

### 메트릭 수집 (Metrics)

Spring Boot Actuator와 Micrometer를 사용하여 애플리케이션 메트릭을 수집합니다. Prometheus와 같은 모니터링 시스템과 연동하여 활용할 수 있습니다.

메트릭은 `/actuator/prometheus` 엔드포인트에서 접근할 수 있습니다.

### 분산 트레이싱 (Distributed Tracing)

Spring Cloud Sleuth를 사용하여 애플리케이션 내의 요청 흐름을 추적하고, Zipkin과 같은 분산 트레이싱 시스템으로 트레이스 정보를 전송합니다.

트레이스 정보는 `http://localhost:9411` (기본 Zipkin 서버 주소)로 전송됩니다.

### 메시징 시스템 (RabbitMQ)

비동기 처리를 위해 RabbitMQ 메시지 브로커를 사용합니다.

RabbitMQ 관리 UI는 `http://localhost:15672`에서 접근할 수 있습니다. (기본 사용자명: `guest`, 비밀번호: `guest`)

#### 이벤트 발행/구독 모델 확장

`MessageProducer`와 `MessageConsumer`를 통해 RabbitMQ를 활용한 간단한 이벤트 발행/구독 모델을 구현했습니다. `test-queue`로 메시지를 보내고 받을 수 있습니다.

#### Dead Letter Queue (DLQ) 설정

메시지 처리 실패 시 메시지가 `test-queue.dlq`로 이동하도록 DLQ를 설정했습니다. 이는 메시지 유실을 방지하고 실패한 메시지를 재처리할 수 있도록 합니다.

### 데이터베이스 마이그레이션 (Flyway)

Flyway를 사용하여 데이터베이스 스키마 변경 이력을 관리합니다. 마이그레이션 스크립트는 `src/main/resources/db/migration` 디렉토리에 위치합니다.

### 통합 테스트 (Integration Tests)

Testcontainers를 사용하여 통합 테스트 환경을 구축합니다. 이를 통해 MySQL, RabbitMQ, ChromaDB와 같은 실제 서비스들을 Docker 컨테이너로 실행하여 테스트를 수행합니다.

### RAG 파이프라인 고도화 및 최적화

#### 하이브리드 검색 (Hybrid Search)

벡터 검색과 함께 간단한 키워드 검색을 통합하여 RAG 답변의 정확도를 높였습니다. `longUrl` 필드에서 키워드 검색을 수행합니다.

#### 문서 재랭킹 (Re-ranking)

검색된 문서들(벡터 검색 및 키워드 검색 결과)의 중복을 제거하여 재랭킹을 수행합니다. 이는 LLM에 전달되는 정보의 품질을 향상시킵니다.

#### 질의 확장 (Query Expansion)

미리 정의된 동의어 목록을 사용하여 사용자 질의를 확장합니다. 이는 검색 범위를 넓혀 더 많은 관련 문서를 찾을 수 있도록 돕습니다.

### JWT 인증 (JWT Authentication)

JWT(JSON Web Token) 기반 인증을 도입하여 API 보안을 강화했습니다.

#### 토큰 발급

`POST /auth/login` 엔드포인트를 사용하여 사용자명(`user`)과 비밀번호(`password`)로 토큰을 발급받을 수 있습니다.

```json
{
  "username": "user",
  "password": "password"
}
```

응답으로 받은 JWT 토큰을 사용하여 인증이 필요한 API 요청을 수행할 수 있습니다.

#### 토큰 사용

인증이 필요한 API 요청 시 `Authorization` 헤더에 `Bearer <YOUR_JWT_TOKEN>` 형식으로 토큰을 포함해야 합니다.

예시:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### API Rate Limiting

Resilience4j의 Rate Limiter를 사용하여 API 요청 속도를 제한합니다. 현재 `RAG API`에 기본 정책이 적용되어 있습니다.

기본 정책:
- `limitForPeriod`: 10 (1초당 허용되는 요청 수)
- `limitRefreshPeriod`: 1s (제한이 재설정되는 주기)

### 컨테이너 오케스트레이션 (Kubernetes/Helm) 준비

애플리케이션을 Kubernetes 환경에 배포하기 위한 Manifest 파일들이 `k8s/` 디렉토리에 준비되어 있습니다.

- `mysql-deployment.yaml`: MySQL 데이터베이스 배포 및 서비스
- `rabbitmq-deployment.yaml`: RabbitMQ 메시지 브로커 배포 및 서비스
- `chromadb-deployment.yaml`: ChromaDB 벡터 데이터베이스 배포 및 서비스
- `app-deployment.yaml`: Spring Boot 애플리케이션 배포 및 서비스

이 파일들을 사용하여 `kubectl apply -f k8s/` 명령으로 Kubernetes 클러스터에 배포할 수 있습니다.

#### 문맥 기반 청킹 (Contextual Chunking)

`SemanticChunker`를 사용하여 문서의 의미론적 경계를 기반으로 청킹을 수행합니다. 이는 RAG 파이프라인에 더 관련성 높은 문맥을 제공하여 답변 품질을 향상시킵니다.

### 성능 최적화 (Performance Optimization)

#### 데이터베이스 인덱싱

`url_entry` 테이블의 `short_url` 컬럼에 인덱스를 추가하여 조회 성능을 향상시켰습니다.

#### 연결 풀 (HikariCP) 튜닝

`application.yml`에서 HikariCP 연결 풀 설정을 조정하여 애플리케이션의 부하에 맞는 최적의 연결 풀 크기 및 타임아웃을 설정했습니다.

### 회복성 패턴 (Circuit Breaker)

Resilience4j의 Circuit Breaker를 사용하여 외부 서비스 호출 시 발생할 수 있는 장애가 전체 시스템으로 확산되는 것을 방지합니다. 현재 `Chat API`에 `externalService` 정책이 적용되어 있습니다.

기본 정책:
- `failureRateThreshold`: 50% (실패율 임계값)
- `slowCallRateThreshold`: 100% (느린 호출 비율 임계값)
- `slowCallDurationThreshold`: 2s (느린 호출 시간 임계값)

### 웹 UI (Web UI)

간단한 URL 단축 웹 UI가 제공됩니다. 애플리케이션 실행 후 `http://localhost:8080/`으로 접속하여 사용할 수 있습니다.

### API Endpoints

- **GET /hello**
  - "Hello, World!"를 반환합니다.

### Step 3: Kotlin Multiplatform 관리자 화면 구현

---

- 해당 요청 사항을 그대로 Claude에 복사하여 요청한다.

    ```
    기능 구현
    URL 입력 폼: 긴 URL 입력 필드와 “단축하기” 버튼
    URL 목록 표시: 단축 키, 원본 URL. 생성 시간 표시
    삭제 기능: 각 URL 항목에 삭제 버튼 추가
    추가 API 엔드포인트
    GET /api/shorten/urls
    모든 단축 URL 목록 조회
    DELETE /api/shorten/{shortKey}
    특정 단축 URL 삭제
    동작 흐름
    POST, DELETE 작업 완료 후 자동으로 URL 목록 갱신
    페이지 로드 시 초기 URL 목록 로드
    ```

- Dependencies가 추가되었으니 Gradle을 리프레시 해준다.
- Build 문제가 있을 경우 해당 오류 내용을 다시 질문하여 자동으로 수정한다.
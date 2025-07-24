# Spring MCP - AI-Powered Microservice Platform

[![CI/CD](https://github.com/SongJunSub/SpringMCP/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/SongJunSub/SpringMCP/actions/workflows/ci-cd.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=spring-mcp&metric=alert_status)](https://sonarcloud.io/dashboard?id=spring-mcp)
[![Coverage](https://codecov.io/gh/SongJunSub/SpringMCP/branch/master/graph/badge.svg)](https://codecov.io/gh/SongJunSub/SpringMCP)

Spring MCP는 **Model Context Protocol (MCP)**을 기반으로 한 고도화된 AI 통합 마이크로서비스 플랫폼입니다. URL 단축 서비스와 AI 기능을 결합하여 현대적인 클라우드 네이티브 아키텍처를 구현한 종합적인 레퍼런스 애플리케이션입니다.

## 🚀 주요 기능

### Core Services
- **URL Shortener**: 고성능 URL 단축 서비스
- **AI Chat**: GPT-4 기반 지능형 챗봇
- **RAG (Retrieval-Augmented Generation)**: 문서 기반 AI 응답 시스템
- **Document Processing**: PDF/다양한 형식 문서 처리 및 벡터화
- **Semantic Search**: AI 기반 의미론적 검색

### Enterprise Features
- **JWT 인증/인가**: Spring Security 기반 보안
- **Rate Limiting**: Resilience4j 기반 API 제한
- **Circuit Breaker**: 장애 전파 방지
- **Distributed Tracing**: Zipkin 기반 분산 추적
- **Comprehensive Metrics**: Prometheus + Grafana 모니터링

### Cloud-Native Architecture
- **Kubernetes 배포**: Helm 차트 지원
- **Docker 컨테이너화**: 멀티 스테이지 빌드
- **CI/CD Pipeline**: GitHub Actions 자동화
- **Multi-Environment**: Development, Staging, Production

## 🛠 기술 스택

### Backend
- **Java 17** + **Spring Boot 3.2.5**
- **Spring AI** - OpenAI GPT-4 통합
- **Spring Security** - JWT 인증/인가
- **Spring Data JPA** - 데이터 접근 계층
- **Resilience4j** - 회복성 패턴 (Circuit Breaker, Rate Limiter)

### AI & ML
- **OpenAI GPT-4** - 언어 모델
- **ChromaDB** - 벡터 데이터베이스
- **Spring AI** - AI 통합 프레임워크
- **Semantic Search** - 벡터 기반 검색

### Infrastructure
- **MySQL 8.0** - 관계형 데이터베이스
- **RabbitMQ** - 메시지 브로커
- **Redis** - 캐싱 (선택사항)
- **Docker** + **Docker Compose** - 컨테이너화

### Monitoring & Observability
- **Prometheus** - 메트릭 수집
- **Grafana** - 시각화 대시보드
- **Zipkin** - 분산 추적
- **Micrometer** - 애플리케이션 메트릭

### DevOps & Deployment
- **Kubernetes** - 컨테이너 오케스트레이션
- **Helm** - Kubernetes 패키지 매니저
- **GitHub Actions** - CI/CD 파이프라인
- **OWASP Dependency Check** - 보안 스캔

## 🚀 빠른 시작

### 전제 조건
- Java 17+
- Docker & Docker Compose
- OpenAI API 키

### 1. 프로젝트 클론
```bash
git clone https://github.com/SongJunSub/SpringMCP.git
cd SpringMCP
```

### 2. 환경 설정
```bash
# OpenAI API 키 설정
export OPENAI_API_KEY="your-openai-api-key"

# Docker Compose 설정 파일 업데이트
cp docker-compose.yml docker-compose.yml.bak
sed -i 's/YOUR_API_KEY/your-openai-api-key/g' docker-compose.yml
```

### 3. 애플리케이션 실행
```bash
# 인프라 서비스 시작
docker compose up -d mysql rabbitmq chroma prometheus grafana zipkin

# 애플리케이션 빌드 및 실행
./gradlew bootRun
```

### 4. 서비스 접근
- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator 헬스**: http://localhost:8080/actuator/health
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Zipkin**: http://localhost:9411

## 📋 API 엔드포인트

### URL Shortener
```http
POST /api/shorten
GET /api/shorten/{shortKey}
```

### AI Services
```http
GET /api/chat?message=hello
POST /api/chat/template
GET /api/chat/stream
```

### RAG (Retrieval-Augmented Generation)
```http
GET /api/rag?message=what is spring ai
POST /api/rag/documents
GET /api/rag/search
```

### Document Processing
```http
POST /api/documents/upload/pdf
POST /api/documents/upload/document
POST /api/documents/process/text
POST /api/documents/process/url
```

### Semantic Search
```http
GET /api/semantic/search
POST /api/semantic/search/advanced
POST /api/semantic/search/compare
```

### Metrics & Monitoring
```http
GET /api/metrics/summary
GET /api/metrics/ai
GET /api/metrics/urls
GET /actuator/prometheus
```

## 🔧 설정 및 환경변수

### 필수 환경변수
```bash
OPENAI_API_KEY=your-openai-api-key
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/spring_mcp_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
```

### 선택적 환경변수
```bash
SPRING_AI_VECTORSTORE_CHROMA_URL=http://localhost:8000
SPRING_RABBITMQ_HOST=localhost
SPRING_ZIPKIN_BASE_URL=http://localhost:9411
```

## 🐳 Docker 배포

### Docker Compose 전체 스택
```bash
docker compose up --build
```

### 개별 서비스 실행
```bash
# 인프라만 실행
docker compose up -d mysql rabbitmq chroma

# 모니터링 스택만 실행
docker compose up -d prometheus grafana zipkin
```

## ☸️ Kubernetes 배포

### Helm을 사용한 배포
```bash
# 네임스페이스 생성
kubectl create namespace spring-mcp

# Helm 배포
helm install spring-mcp ./helm/spring-mcp \
  --namespace spring-mcp \
  --set config.openai.apiKey=your-api-key \
  --set ingress.host=spring-mcp.example.com
```

### 배포 확인
```bash
kubectl get pods -n spring-mcp
kubectl get services -n spring-mcp
kubectl get ingress -n spring-mcp
```

## 📊 모니터링 및 메트릭

### Prometheus 메트릭
- `url_shortened_total` - 단축된 URL 총 개수
- `ai_chat_requests_total` - AI 채팅 요청 수
- `ai_rag_requests_total` - RAG 요청 수
- `documents_processed_total` - 처리된 문서 수
- `search_requests_total` - 검색 요청 수

### Grafana 대시보드
미리 구성된 대시보드가 `monitoring/grafana-dashboard.json`에 포함되어 있습니다.

### 분산 추적
Zipkin UI에서 요청 흐름을 추적할 수 있습니다.

## 🧪 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### 통합 테스트 실행
```bash
./gradlew integrationTest
```

### 코드 커버리지 확인
```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### 보안 스캔
```bash
./gradlew dependencyCheckAnalyze
```

## 🔐 보안

### 인증 및 인가
JWT 토큰 기반 인증이 구현되어 있습니다.

```bash
# 토큰 발급
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'

# 인증이 필요한 API 호출
curl -X GET http://localhost:8080/api/chat?message=hello \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 보안 헤더
기본 보안 헤더들이 Spring Security에 의해 자동으로 추가됩니다.

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 제공됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## 📞 지원

- **Issues**: [GitHub Issues](https://github.com/SongJunSub/SpringMCP/issues)
- **Discussions**: [GitHub Discussions](https://github.com/SongJunSub/SpringMCP/discussions)

---

## 🔧 MCP 연동 (Claude Desktop)

### Claude 설치 및 설정
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

### 설정 외부화

- `application.properties` 파일에 `app.base-url` 속성을 추가하여 애플리케이션의 기본 URL을 설정할 수 있도록 변경했습니다.
- `UrlShortenerController`에서 `@Value` 어노테이션을 사용하여 `app.base-url` 값을 주입받아 사용합니다.
- `UrlShortenerApi` 클래스의 생성자를 통해 `baseUrl`을 주입받도록 수정하여, API 클라이언트 사용 시 기본 URL을 동적으로 설정할 수 있도록 변경했습니다.

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
2.  `docker-compose.yml` 파일의 `SPRING_AI_OPENAI_API_KEY`를 실제 OpenAI API 키로 교체하거나, `.env` 파일을 생성하여 `OPENAI_API_KEY=YOUR_API_KEY` 형식으로 환경 변수를 설정합니다.

**실행 명령어:**

```bash
docker compose up --build
```

애플리케이션은 `http://localhost:8080`에서 실행됩니다. MySQL은 `localhost:3306`, ChromaDB는 `localhost:8000`에서 접근 가능합니다.

### 로깅 설정

애플리케이션 로그는 `logback-spring.xml` 설정을 통해 JSON 형식으로 출력됩니다. `logstash-logback-encoder` 의존성을 추가하여 Logstash와 같은 중앙 집중식 로깅 시스템으로 로그를 전송할 수 있도록 준비되었습니다. 이는 로그 분석 도구에서 쉽게 파싱하고 활용할 수 있도록 합니다.

### 메트릭 수집 (Metrics)

Spring Boot Actuator와 Micrometer를 사용하여 애플리케이션 메트릭을 수집합니다. Prometheus와 같은 모니터링 시스템과 연동하여 활용할 수 있습니다.

메트릭은 `/actuator/prometheus` 엔드포인트에서 접근할 수 있습니다.

#### 경고 시스템 (Alerting System)

Prometheus Alertmanager와 같은 도구를 사용하여 특정 메트릭(예: 에러율, 응답 시간)이 임계값을 초과할 경우 Slack, PagerDuty 등으로 알림을 보내는 경고 시스템을 구축할 수 있습니다. 이는 문제 발생 시 즉각적인 대응을 가능하게 합니다.

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

#### 프롬프트 관리 시스템

프롬프트 템플릿을 외부 파일(`src/main/resources/prompts/rag-prompt.st`)로 분리하여 관리합니다. 이를 통해 프롬프트 변경 및 실험이 용이해졌습니다.

#### 하이브리드 검색 (Hybrid Search)

벡터 검색과 함께 간단한 키워드 검색을 통합하여 RAG 답변의 정확도를 높였습니다. `longUrl` 필드에서 키워드 검색을 수행합니다.

#### 문서 재랭킹 (Re-ranking)

검색된 문서들(벡터 검색 및 키워드 검색 결과)의 중복을 제거하여 재랭킹을 수행합니다. 이는 LLM에 전달되는 정보의 품질을 향상시킵니다.

#### 질의 확장 (Query Expansion)

미리 정의된 동의어 목록을 사용하여 사용자 질의를 확장합니다. 이는 검색 범위를 넓혀 더 많은 관련 문서를 찾을 수 있도록 돕습니다.

### JWT 인증 (JWT Authentication)

JWT(JSON Web Token) 기반 인증을 도입하여 API 보안을 강화했습니다.

#### CORS 설정

웹 UI와의 통신을 위해 CORS(Cross-Origin Resource Sharing)를 허용하도록 Spring Security에 설정이 추가되었습니다. 현재는 모든 출처를 허용하고 있으며, 운영 환경에서는 특정 출처만 허용하도록 변경해야 합니다.

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

#### CI/CD 파이프라인 구축

GitHub Actions, Jenkins, GitLab CI/CD 등과 같은 도구를 사용하여 CI/CD 파이프라인을 구축할 수 있습니다. 이를 통해 코드 푸시 시 자동으로 빌드, 테스트, 코드 품질 검사를 수행하고, Docker 이미지를 빌드하여 컨테이너 레지스트리에 푸시하며, Kubernetes 환경에 자동으로 배포할 수 있습니다.

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

### 국제화 (Internationalization - i18n) 지원

애플리케이션의 메시지 및 UI 텍스트를 여러 언어로 제공합니다. `messages.properties` 파일을 통해 기본 메시지를 정의하고, `messages_ko.properties`, `messages_en.properties`와 같은 파일을 통해 언어별 메시지를 제공합니다.

`lang` 파라미터를 사용하여 언어를 변경할 수 있습니다. (예: `http://localhost:8080/?lang=ko`)

### 웹 UI (Web UI)

간단한 URL 단축 웹 UI가 제공됩니다. 애플리케이션 실행 후 `http://localhost:8080/`으로 접속하여 사용할 수 있습니다.

### 세부적인 예외 처리 (Detailed Exception Handling)

Spring AI `ChatClient` 호출 시 발생할 수 있는 AI 서비스 관련 예외를 `GlobalExceptionHandler`에서 중앙 집중적으로 처리하도록 개선했습니다. 이를 통해 애플리케이션의 안정성과 예외 처리의 일관성을 높였습니다.

- `org.springframework.ai.retry.NonTransientAiException`: API 키 오류, 잘못된 요청 등 재시도해도 성공할 가능성이 없는 예외를 `HTTP 400 Bad Request`로 처리합니다.
- `org.springframework.ai.retry.TransientAiException`: 일시적인 네트워크 오류, API 서버 과부하 등 재시도하면 성공할 가능성이 있는 예외를 `HTTP 503 Service Unavailable`로 처리합니다.

`ChatController`에서는 더 이상 `fallbackMethod`를 사용하지 않고, 예외 처리를 `GlobalExceptionHandler`로 위임합니다.

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

### 웹 관리자 UI

- Kotlin Multiplatform과 Jetpack Compose를 사용하여 웹 관리자 화면을 구현했습니다.
- **기능**
    - **URL 단축:** 긴 URL을 입력하고 단축 URL을 생성할 수 있습니다. (사용자 정의 키 옵션 제공)
    - **URL 목록:** 생성된 단축 URL 목록을 조회할 수 있습니다. (단축 키, 원본 URL, 생성 시간 표시)
    - **URL 삭제:** 각 URL 항목을 삭제할 수 있습니다.
- **구현**
    - `web-admin/src/commonMain/kotlin/com/example/springmcp/webadmin/App.kt` 파일에 UI 및 비즈니스 로직을 구현했습니다.
    - `UrlShortenerApi`를 사용하여 백엔드 API와 통신합니다.
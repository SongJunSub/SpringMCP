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
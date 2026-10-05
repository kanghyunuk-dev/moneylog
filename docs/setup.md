# 새 컴퓨터에서 시작할 때 체크리스트

git으로 코드/문서/DB 설계는 전부 그대로 넘어오지만, "그 컴퓨터의 로컬 상태"(실제 DB, 환경변수)는 git이 처리 못 하는 영역이라 매번 새로 준비해야 함. 아래 순서대로 확인.

1. **환경 설치 확인**: Java 21, Node.js/npm, MySQL, Docker Desktop, Git이 설치되어 있는지.
2. **저장소 clone**: `git clone https://github.com/kanghyunuk-dev/moneylog.git`
3. **DB 생성 + 스키마 적용** (git으로 안 넘어오는 부분, 직접 실행 필요):
   ```
   mysql -u root -p -e "CREATE DATABASE moneylog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   mysql -u root -p --default-character-set=utf8mb4 moneylog < docs/db-schema.sql
   ```
   (예약어 충돌 등으로 이미 실행 검증까지 마친 파일이라 그대로 실행하면 됨 — 상세는 `docs/troubleshooting.md`의 "DB 스키마 실행 검증" 참고.)
4. **Redis 컨테이너 실행** (④단계부터 필요, git으로 안 넘어오는 로컬 인프라):
   ```
   docker run -d --name moneylog-redis -p 6379:6379 redis:latest
   ```
   `docker ps`로 `moneylog-redis`가 `Up` 상태인지 확인. 컴퓨터를 껐다 켠 뒤엔 컨테이너가 `Exited`로 남아 자동으로 안 켜질 수 있으니(실제로 확인됨) 개발 시작 전에 `docker start moneylog-redis`로 켤 것 — 안 켜도 앱은 뜨지만 Redis 장애 폴백 경로로 동작해 요청마다 경고 로그와 약 0.5초 지연이 생김.
5. **환경변수 등록** (`MYSQL_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`): Windows라면 `setx MYSQL_PASSWORD 실제비밀번호`처럼 OS 환경변수로 직접 등록. `backend/build.gradle`에 dotenv 관련 플러그인이 없어 `.env` 파일을 만들어도 `./gradlew`는 자동으로 안 읽음(`.env`는 8번의 Docker Compose 실행에서만 유효) — backend의 `application.properties`가 `${...}`로 참조하는 값이라 환경변수가 하나라도 없으면 앱 실행과 `./gradlew test`가 실패함.
6. **백엔드 실행 확인**: `cd backend && ./gradlew test` — `BUILD SUCCESSFUL`이면 DB 연결까지 정상.
7. **프론트 실행 확인**: `cd frontend && npm install && npm run dev` — 기본 화면 뜨면 정상.

8. **(선택) Docker Compose로 전체 스택 한 번에 실행** (배포와 동일한 구성으로 검증할 때 — 위 1~7번은 기능 하나씩 개발할 때 쓰는 개별 실행 방식, 이 방식과 별개):
   ```
   cp .env.example .env   # MYSQL_ROOT_PASSWORD/MYSQL_APP_PASSWORD/JWT_SECRET/GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET 채우기
   docker compose up --build
   ```
   mysql → redis → backend → nginx 순서로 `healthy`가 될 때까지 자동으로 기다림 — `http://localhost`로 접속해 확인. `docs/db-schema.sql` 초기화는 MySQL 볼륨이 비어있을 때 1회만 실행되므로, 스키마·시드 데이터를 바꾼 뒤엔 `docker compose down -v`로 볼륨을 지우고 다시 실행해야 함(기존 데이터도 같이 사라짐). 구글 로그인 테스트는 구글 콘솔 리디렉션 URI에 `http://localhost/login/oauth2/code/google`(포트 없음)이 등록되어 있어야 함.
9. **CI/CD**: `develop`/`main` push나 PR마다 GitHub Actions(`ci.yml`)가 위 6~7번과 같은 테스트/lint/build를 자동으로 돌림. `deploy.yml`(main push 시 EC2 자동 배포)도 실동작 중 — 상세는 10번 참고.
10. **운영 서버(EC2) 접속/재배포**: `main`에 push하면 GitHub Actions가 자동으로 EC2에 재배포함. 직접 접속해서 확인/조치할 때:
    ```
    ssh -i <키페어.pem 경로> ec2-user@<Elastic IP>
    cd ~/moneylog
    docker compose ps                          # 컨테이너 상태 확인
    docker compose logs <서비스명> --tail 50   # 에러 확인
    docker compose up --build -d               # 수동 재배포(CD 실패 시 등)
    ```
    EC2의 `.env`(DB 비밀번호, JWT_SECRET 등)와 `.pem` 키 파일은 git으로 안 넘어오는 로컬 상태라, 분실하면 재발급/재작성 필요(`.pem`은 재발급 자체가 불가능하니 안전하게 보관).

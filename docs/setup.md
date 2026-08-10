# 새 컴퓨터에서 시작할 때 체크리스트

git으로 코드/문서/DB 설계는 전부 그대로 넘어오지만, "그 컴퓨터의 로컬 상태"(실제 DB, 환경변수)는 git이 처리 못 하는 영역이라 매번 새로 준비해야 함. 아래 순서대로 확인.

1. **환경 설치 확인**: Java 21, Node.js/npm, MySQL, Git이 설치되어 있는지.
2. **저장소 clone**: `git clone https://github.com/kanghyunuk-dev/moneylog.git`
3. **DB 생성 + 스키마 적용** (git으로 안 넘어오는 부분, 직접 실행 필요):
   ```
   mysql -u root -p -e "CREATE DATABASE moneylog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   mysql -u root -p --default-character-set=utf8mb4 moneylog < docs/db-schema.sql
   ```
   (예약어 충돌 등으로 이미 실행 검증까지 마친 파일이라 그대로 실행하면 됨 — 상세는 `docs/troubleshooting.md`의 "DB 스키마 실행 검증" 참고.)
4. **환경변수 `MYSQL_PASSWORD` 등록**: Windows라면 `setx MYSQL_PASSWORD 실제비밀번호` 또는 `.env` 파일을 새로 생성(`MYSQL_PASSWORD=...`, 이 파일은 `.gitignore`에 있어 git으로 안 넘어옴). backend의 `application.properties`(`spring.datasource.password=${MYSQL_PASSWORD}`)가 이 값을 참조하므로 필수.
5. **백엔드 실행 확인**: `cd backend && ./gradlew test` — `BUILD SUCCESSFUL`이면 DB 연결까지 정상.
6. **프론트 실행 확인**: `cd frontend && npm install && npm run dev` — 기본 화면 뜨면 정상.

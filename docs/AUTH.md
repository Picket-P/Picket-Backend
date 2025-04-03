## Auth PR 관련 알아야 할 내용

### 구현 완료 된 API
- 유저 회원가입
- 디렉터 회원가입
- 관리자 회원가입
- 로그인
- 로그아웃


### AuthInterceptor
- JWT 필터와 비슷한 역할이라 생각
- 인터셉터를 통해 세선이 필요 없는 URL, 세션이 필요한 URL을 거름
  - 세션이 필요한 URL 요청이 들어왔는데, 세션이 없는 경우 예외 발생

### AuthUserArgumentResolver
- 기존 resolver를 가져와 수정
- AuthUser 클래스여아만 @Auth 달 수 있음.
- HttpServletRequest에서 session을 가져와 존재할 경우 AuthUser return
- 존재하지 않는다면 null값 return

**아직 적용한 곳이 없으므로 적용 이후 에러 발생시 수정 예정**

### AuthUser
- 기존 AuthUser 가져와 사용

### Const
- Const에 WHITE_LIST URL 담을 수 있는 곳 추가. 이후 필요할 때 수정 가능


### Flow
유저가 회원 가입 -> 유저 정보를 DB에 저장 -> 회원 가입 성공 -> 로그인 요청 -> 이메일과 비밀번호 검사 -> 존재하면 session에 authUser 생성하여 setAttribute -> response 반환

Postman에서 위 flow가 성공적으로 실행 되면, 우측 중단에 파란색 글씨 Cookies 클릭 -> JSESSIONID가 존재 -> 클릭 해보면 ID 값이 response 값과 일치한 것을 알 수 있음.

로그아웃 요청(Postman에 해당 세션 ID 값이 자동으로 들어가는듯?) -> 만약 session이 없다면 알아서 interceptor에서 걸러줌 -> 로그인한 상태라면 session 해제 -> 성공


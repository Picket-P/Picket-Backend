### 구현 완료 된 API
- 티켓 생성
- 티켓 다건 조회
- 티켓 단건 조회
- 티켓 삭제

### TicketServiceAspect
- 티켓 생성 시 User만 할 수 있도록 제한하는 AOP (따로 어노테이션을 생성하지는 않았습니다)

### createTicket
- 다음과 같은 순서로 진행됩니다.
- 로그인 된 사용자 UserRole 검증 
- 예매 시간 검증
- 좌석 검증
- ShowDateRemainCount 감소
- 티켓 생성

### getTickets, getTicket
- 다건 조회는 로그인 된 사용자가 예매한 티켓만 조회될 수 있도록 하였습니다.
- 다건 조회는 페이지 처리 하였습니다. 정렬 기준은 createdAt 내림차순입니다.


### deleteTicket
- 다음과 같은 순서로 진행됩니다.
- 로그인된 사용자 Id와 티켓의 userId가 같은지 검증
- 현재 시간이 공연날짜 전인지 검증 (공연 날짜 or 공연 시작 시간 으로 할지 논의는 필요해보입니다.)
- CANCLED 상태로 업데이트
- 환불 처리
- EXPIRED, deletedAt 업데이트

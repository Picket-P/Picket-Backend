### 구현 완료 된 API
- 티켓 생성
- 티켓 다건 조회
- 티켓 단건 조회
- 티켓 삭제

### createTicket
- 다음과 같은 순서로 진행됩니다.
- User, 예매하려는 Seat, Seat와 연관된 Show-ShowDate, Seat의 price를 불러옵니다.
- 티켓 생성 가능한 시간인지 검증합니다.
- User가 가진 티켓 개수가 제한을 초과하지 않는지 검증합니다.
- Seat의 Status가 AVAILABLE인지 검증합니다.
- ShowDate의 availableSeatCount, reservedSeatCount를 업데이트합니다.
- Seat의 Status를 RESERVED로 업데이트합니다.
- 티켓을 생성합니다.

### getTickets, getTicket
- 다건 조회는 로그인 된 사용자가 예매한 티켓만 조회될 수 있도록 하였습니다.
- 다건 조회는 페이지 처리 하였습니다. 정렬 기준은 createdAt 내림차순입니다.


### deleteTicket
- 다음과 같은 순서로 진행됩니다.
- 삭제하려는 Ticket, 연관된 ShowDate-Seat를 불러옵니다.
- 티켓 삭제 가능한 시간인지 검증합니다.
- 삭제하려는 Ticket의 Status가 CREATED인지 검증합니다.
- 로그인된 userId와 티켓의 userId가 같은지 검증합니다.
- Ticket의 Status를 CANCELED로 업데이트 합니다.
- ShowDate의 availableSeatCount, reservedSeatCount를 업데이트합니다.
- Seat의 Status를 AVAILABLE로 업데이트 합니다.
- Ticket의 Status를 EXPIRED로 업데이트 합니다.
- Ticket의 deletedAt을 현재시간으로 업데이트 합니다.

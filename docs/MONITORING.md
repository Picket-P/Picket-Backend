## 로컬 환경 설정 튜토리얼

### 1. Docker-Compose 실행

```text
docker-compose up -d
```
- 프로젝트 루트 경로에서 터미널을 이용하여 해당 명령어를 실행합니다.

### 2. 설치 완료

```text
[+] Running 5/5
 ✔ Network picket-backend_default           Created             0.3s 
 ✔ Volume "picket-backend_grafana_data"     Created             0.0s 
 ✔ Volume "picket-backend_prometheus_data"  Created             0.0s 
 ✔ Container grafana                        Started             1.5s 
 ✔ Container prometheus                     Started             1.5s 
```
- 해당 형태의 메세지가 출력된다면 설치가 완료된 것입니다.

### 3. 연결 확인

- 먼저 Spring Boot App 을 실행해줍니다.
- Spring Boot App 실행이 완료되었다면 localhost:3000을 입력하여 Grafana에 접속합니다.
- 아래 이미지처럼 화면이 나온다면 연결이 정상적으로 처리된 것입니다.

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/%EA%B7%B8%EB%9D%BC%ED%8C%8C%EB%82%98_%EC%B4%88%EA%B8%B0%ED%99%94%EB%A9%B4.png)

### 4. Grafana 초기 로그인

- 초기 ID, Password 는 모두 admin 입니다.

### 5. 로그인 성공 후 비밀번호 재설정

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/%EA%B7%B8%EB%9D%BC%ED%8C%8C%EB%82%98_%EB%A1%9C%EA%B7%B8%EC%9D%B8_%EC%84%B1%EA%B3%B5%ED%9B%84_%EB%B9%84%EB%B0%80%EB%B2%88%ED%98%B8_%EC%9E%AC%EC%84%A4%EC%A0%95.png)

- 로그인에 설정하면 비밀번호 재설정에 대한 화면이 나오게됩니다.
- 빨간 박스 안에 Skip 을 누르셔도 정상적으로 사용이 가능하고, 비밀번호 재설정을 해주셔도 상관없습니다.

### 6. Datasource 추가 확인

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/%EA%B7%B8%EB%9D%BC%ED%8C%8C%EB%82%98_%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%86%8C%EC%8A%A4_%ED%99%95%EC%9D%B8.png)

- 제일 먼저 빨간 박스로 감싸져있는 메뉴로 들어가서 정상적으로 Prometheus가 추가되어있는지 확인합니다.
- 정상적으로 추가 되어있다면 8번 단계(Dashboard JSON 데이터 Import 하기)로 넘어가시면 됩니다.



### 7. Datasource 수동으로 추가하기

<details>
<summary>Datasource 수동으로 튜토리얼</summary>

#### 1단계
![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/datasources/%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%86%8C%EC%8A%A4_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_1%EB%8B%A8%EA%B3%84.png)

- 상단에 Add new data source 버튼을 클릭해줍니다.

<br/>

#### 2단계

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/datasources/%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%86%8C%EC%8A%A4_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_2%EB%8B%A8%EA%B3%84.png)

- Prometheus 를 선택해줍니다.
- 만약 화면이 저와 다르다면 Prometheus를 검색 후 이름이 정확히 일치하는 것을 클릭해줍니다.

<br/>

#### 3단계(중요!!!!)

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/datasources/%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%86%8C%EC%8A%A4_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_3%EB%8B%A8%EA%B3%84.png)

- 빨간 박스 내부에 스크린샷과 동일한 URL을 입력해줍니다.
- 연결 테스트시에 정상적으로 동작하지 않았다면 해당 URL 설정이 잘못된것입니다.

<br/>

#### 4단계

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/datasources/%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%86%8C%EC%8A%A4_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_4%EB%8B%A8%EA%B3%84.png)

- 3단계 화면에서 제일 밑으로 스크롤하면 Save&Test 버튼을 눌러 정상적으로 연결이 되었는지 확인합니다.

<br/>

#### 5단계

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/%EA%B7%B8%EB%9D%BC%ED%8C%8C%EB%82%98_%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%86%8C%EC%8A%A4_%ED%99%95%EC%9D%B8.png)

- 다시 Datasource 탭으로 돌아와 정상적으로 Prometheus가 추가되었는지 확인합니다.
</details>

### 8. Dashboard JSON 데이터 Import 하기

<details>
<summary>Dashboard JSON 데이터 Import 하기 튜토리얼</summary>

#### 1단계

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/dashboard/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_1%EB%8B%A8%EA%B3%84.png)

- 왼쪽에 Dashboards 메뉴를 클릭합니다.
- 오른쪽 상단에 New 버튼을 클릭 후 스크린샷처럼 Import 버튼을 클릭합니다.

<br/>

#### 2단계

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/dashboard/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_2%EB%8B%A8%EA%B3%84.png)

- 프로젝트의 monitoring 폴더 내부에 json 파일들이 있습니다.
- 해당 json 파일 내부의 코드를 빨간 박스 내부에 복사 붙여넣기 한 후에, Load 버튼을 클릭합니다.

<br/>

#### 3단계

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/dashboard/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_3%EB%8B%A8%EA%B3%84.png)

- 해당 화면에서 Prometheus를 선택해주시고 Import 버튼을 클릭합니다.

<br/>

#### 4단계

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/dashboard/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C_%EC%B6%94%EA%B0%80%ED%95%98%EA%B8%B0_4%EB%8B%A8%EA%B3%84.png)

- 정상적으로 추가되었다면 해당 화면으로 넘어오게 됩니다.
- json 파일이 2개인데 동일한 방법으로 남은 Dashboard Template도 Import 해주시면 됩니다.
</details>

### 9. 설치 완료

![Image](https://sparta-plus.s3.ap-northeast-2.amazonaws.com/monitoring/dashboard/%EB%8C%80%EC%8B%9C%EB%B3%B4%EB%93%9C_%EC%B5%9C%EC%A2%85%ED%99%94%EB%A9%B4.png)

- Dashboards 메뉴로 다시 돌아오면 정상적으로 Dashboard가 2개 추가된 것을 확인할 수 있습니다.
- 해당 Dashboard를 클릭하면 앱의 상태를 확인할 수 있는 화면이 나오게 되며 모니터링 도구 설정이 완료됩니다.












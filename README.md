## V1
동시성 해결을 위해서 분산락으로 걸었을 떄 
생각보다 처리량이 떨어짐
![스크린샷 2024-10-30 오후 2 44 41](https://github.com/user-attachments/assets/419fe908-63ac-463f-963d-ec78b3d3f481)
![image](https://github.com/user-attachments/assets/64c0e15f-30b8-4375-9f98-365a6cb3091e)
![스크린샷 2024-10-30 오후 1 56 14](https://github.com/user-attachments/assets/aa8647a9-ca08-4679-8718-0d4a702a819f)

## V2
락을 걸지 않고 처리를 했을 때 처리량은 높음.
처리 속도는 올라갔지만 정해진 수량인 600개를 초과하는 동시성 문제가 발생함.
![image](https://github.com/user-attachments/assets/e3a9e179-053e-4d6c-b17b-58c6dae402ca)
![image](https://github.com/user-attachments/assets/4cf756f1-288b-489f-b94b-322c334696d3)

## V3
Redis 스크립트를 통한 해결 방안.
처리 속도도 락을 걸지 않은 상태랑 비슷하고 결과는 락을 걸어서 처리한 것 처럼 정해진 수량 만큰만 발급되는 것을 확인함.
![image](https://github.com/user-attachments/assets/cebd0e0d-f327-4d0b-ab2a-faf4484b87b9)
![image](https://github.com/user-attachments/assets/fe6a710d-28c3-432f-bff8-709357cd34dc)

**그럼 왜 V2, V3에서 차이가 나는 것일까?** <br>
- 원자적 실행: Lua 스크립트는 Redis 서버 내에서 한 번의 요청으로 실행되므로, 네트워크 왕복 시간이 줄어들어 성능이 개선됨
- 병렬 처리 지원: 여러 명령을 하나의 스크립트로 처리하므로, 추가적인 락 해제나 재시도가 필요 없어 오버헤드가 줄어듬



## 흐름

1. 쿠폰에 발급 요청이 들어온다.
2. 쿠폰 발급이 가능한지 검증한다.
   1. 검증할 때 로컬 캐시를 먼저 조회하여 현재 요청한 유저가 해당 쿠폰을 발급 할 수 있는지 검증한다.
   2. 로컬 캐시가 없다면 Redis 캐시를 통해서 데이터를 가져와서 검증한다.
   3. Redis 캐시가 없다면 DB에서 정보를 가져와서 검증한다.
3. 검증 내용으로는 발급 가능한 수량인지, 발급 가능한 일자인지, 기존에 발급한 이력이 있는지를 검사한다.
4. 검증이 완료가 되면 쿠폰 발급 대기 큐에 넣어준다.

1. 1초에 1번씩 발급 대기 큐에 있는 데이터를 뽑아서 쿠폰을 발급해주는 스케쥴러가 실행이 된다.
2. 큐에 있는 데이터를 뽑고 쿠폰을 발급해준다.
3. 발급이 완료되면 로컬 캐시와 Redis 캐시를 업데이트 한다. -> 발급 가능 수량 업데이트 

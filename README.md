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

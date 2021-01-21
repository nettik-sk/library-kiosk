# 도서 대여 서비스
    
# 서비스 시나리오
 
 기능적 요구사항
 1. 사용자가 도서를 예약한다.
 1. 도서 예약 시 결제가 완료되어야 한다.
 1. 사용자가 예약 중인 도서를 대여한다.
 1. 사용자가 대여 중인 도서를 반납한다.
 1. 사용자가 예약을 취소할 수 있다.
 1. 도서 예약 취소 시에는 결제가 취소된다.
 1. 사용자가 예약/대여 상태를 확인할 수 있다.
 1. 사용자가 Kiosk에서 예약중인 도서를 Self 대여한다. (개인과제)
 1. 사용자가 Kiosk에서 대여 중인 도서를 Self 반납한다. (개인과제)
 
 비기능적 요구사항
 1. 트랜잭션
     1. 결제가 되지 않은 경우 예약할 수 없다 (Sync 호출)
     1. Kiosk에서 대여 시 도서반납 처리가 완료 되지 않은 경우 반납 처리할 수 없다 (Sync 호출) (개인과제)
 1. 장애격리
     1. 도서관리 기능이 수행되지 않더라도 대여/예약은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
     1. 결제 시스템이 과중되면 사용자를 잠시동안 받지 않고 예약을 잠시후에 하도록 유도한다  Circuit breaker, fallback
     1. 도서관리 기능이 수행되지 않더라도 Kiosk 반납은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency (개인과제)
     1. 도서관리 시스템이 과중되면 Kiosk 사용자를 잠시동안 받지 않고 Kiosk 대여을 잠시후에 하도록 유도한다  Circuit breaker, fallback (개인과제)
 1. 성능
     1. 사용자는 MyPage에서 본인 예약 및 대여 도서의 목록과 상태를 확인할 수 있어야한다 CQRS
     1. 사용자는 ReservationList에서 본인 예약 및 대여 도서의 목록과 상태를 확인할 수 있어야한다 CQRS (개인과제)
 
 
 # 분석/설계
 
## Event Storming 결과
 * MSAEz 로 모델링한 이벤트스토밍 결과: (팀과제)
 ![이벤트스토밍_팀과제](https://user-images.githubusercontent.com/75237785/105280923-51f09f00-5bee-11eb-8f96-7b9ef62c3fd0.jpg)

* MSAEz 로 모델링한 이벤트스토밍 결과: (개인과제)
 ![이벤트스토밍](https://user-images.githubusercontent.com/75237785/105280920-50bf7200-5bee-11eb-878d-bd4f03ad9e20.jpg)


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8085 이다)

```
cd book
mvn spring-boot:run

cd mypage
mvn spring-boot:run 

cd payment
mvn spring-boot:run  

cd rental
mvn spring-boot:run

cd kiosk
mvn spring-boot:run
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 kiosk 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 

```
package library;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Kiosk_table")
public class Kiosk {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long rentalId;
    private String bookStatus;
    private Long bookId;
    private Long memberId;
    private Long kioskNo;

    @PostPersist
    public void onPostPersist(){
        SelfRentaled selfRentaled = new SelfRentaled();
        BeanUtils.copyProperties(this, selfRentaled);
        selfRentaled.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){

        if(  bookStatus.equals("kioskreturn")) {
            SelfReturned selfReturned = new SelfReturned();
            BeanUtils.copyProperties(this, selfReturned);
            selfReturned.publishAfterCommit();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
    }
    public String getBookStatus() {
        return bookStatus;
    }

    public void setBookStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public Long getKioskNo() {
        return kioskNo;
    }

    public void setKioskNo(Long kioskNo) {
        this.kioskNo = kioskNo;
    }
}
```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package library;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface KioskRepository extends PagingAndSortingRepository<Kiosk, Long>{
}
```

- 적용 후 REST API 의 테스트 시나리오

1. SAGA
2. CQRS
3. Correlation

```
# 사용자가 도서를 예약한다
http POST http://20.194.7.119:8080/rentals memberId=1 bookId=1
```

 ![image](https://user-images.githubusercontent.com/53402465/105122171-10da8b00-5b19-11eb-8ed0-664590206d60.png)

```
#Rental 내역 확인
http GET http://20.194.7.119:8080/rentals
```

 ![image](https://user-images.githubusercontent.com/53402465/105122172-10da8b00-5b19-11eb-8fcb-afa4002c7a42.png)

```
# 사용자 예약 후 결제확인
http GET http://20.194.7.119:8080/payments
```

![image](https://user-images.githubusercontent.com/53402465/105122173-11732180-5b19-11eb-843a-44b1c61fdbd2.png)

```
# 사용자 예약한 책 상태 확인
http GET http://20.194.7.119:8080/books
```

![image](https://user-images.githubusercontent.com/53402465/105122175-11732180-5b19-11eb-819d-6a8f95dd2036.png)

```
# 사용자 도서 예약취소
http PATCH http://20.194.7.119:8080/rentals/1 reqState="cancel" 
```

![image](https://user-images.githubusercontent.com/53402465/105122166-0f10c780-5b19-11eb-8693-f5626f980855.png)

```
# 결제취소 확인
http GET http://20.194.7.119:8080/rentals/1
```

![image](https://user-images.githubusercontent.com/53402465/105122169-1041f480-5b19-11eb-99c9-d4597c9fe0a8.png)

```
# 사용자 예약 취소한 책 상태 확인
http GET http://20.194.7.119:8080/books
```

![image](https://user-images.githubusercontent.com/53402465/105122170-1041f480-5b19-11eb-9496-20c40fcfeffb.png)

```
#마이페이지 확인
http GET http://20.194.7.119:8080/mypages/1
```

![image](https://user-images.githubusercontent.com/75401893/105123042-b4786b00-5b1a-11eb-9f8c-0b0b20a7e8d9.png)

```
# 사용자 도서 예약
http POST http://20.194.7.119:8080/rentals memberId=1 bookId=1 
```

![image](https://user-images.githubusercontent.com/53402465/105122636-ee953d00-5b19-11eb-8147-d9e68ba72f74.png)

```
# 사용자 도서 대여
http PATCH http://20.194.7.119:8080/rentals/2 reqState="rental" 
```

![image](https://user-images.githubusercontent.com/53402465/105122637-ee953d00-5b19-11eb-89f2-002a644ebcf0.png)

```
# 사용자 대여한 책 상태 확인
http GET http://20.194.7.119:8080/books/
```

![image](https://user-images.githubusercontent.com/53402465/105122640-efc66a00-5b19-11eb-9ca2-3671c7b3af80.png)

```
# 사용자 도서 반납
http PATCH http://20.194.7.119:8080/rentals/2 reqState="return" 
```

![image](https://user-images.githubusercontent.com/53402465/105122633-ed641000-5b19-11eb-8568-93f892300c96.png)

```
# 사용자 반납한 책 상태 확인
http GET http://20.194.7.119:8080/books
```

![image](https://user-images.githubusercontent.com/53402465/105122635-edfca680-5b19-11eb-81c2-5a3b8876ede8.png)

```
#마이페이지 확인
http GET http://20.194.7.119:8080/mypages/2
```

![image](https://user-images.githubusercontent.com/75401893/105123049-b8a48880-5b1a-11eb-833e-a44ac80e983a.png)



# 개인과제 테스트 시나리오

## 도서 예약
![1](https://user-images.githubusercontent.com/75237785/105281034-8c5a3c00-5bee-11eb-8272-46de9ab95b18.jpg)

## Rental 예약 내역 확인
![2](https://user-images.githubusercontent.com/75237785/105281037-8d8b6900-5bee-11eb-95c7-98771276d78c.jpg)

## ReservationList 확인
![3](https://user-images.githubusercontent.com/75237785/105281038-8d8b6900-5bee-11eb-8e98-b161538e7ee1.jpg)

## kiosk 대여
![4](https://user-images.githubusercontent.com/75237785/105281039-8e23ff80-5bee-11eb-9098-9830d500f16b.jpg)

## kiosk 대여 내역 확인
![5](https://user-images.githubusercontent.com/75237785/105281041-8ebc9600-5bee-11eb-9389-576f1d2443c6.jpg)

## kiosk 대여 후 책 상태 확인
![6](https://user-images.githubusercontent.com/75237785/105281042-8ebc9600-5bee-11eb-90e9-ba84b0cb1aaa.jpg)

## kiosk 대여 후 Rental 상태 확인
![7](https://user-images.githubusercontent.com/75237785/105281043-8f552c80-5bee-11eb-98b9-aa66f39fc60c.jpg)

## kiosk 반납
![8](https://user-images.githubusercontent.com/75237785/105281044-8f552c80-5bee-11eb-8df2-19386402d48a.jpg)

## kiosk 반납 내역 확인
![9](https://user-images.githubusercontent.com/75237785/105281046-8fedc300-5bee-11eb-919c-c7d6b8d71c44.jpg)

## kiosk 반납 후 책 상태 확인
![10](https://user-images.githubusercontent.com/75237785/105281047-90865980-5bee-11eb-8903-61b3fa96fb97.jpg)

## kiosk 반납 후 Rental 상태 확인
![11](https://user-images.githubusercontent.com/75237785/105281048-90865980-5bee-11eb-9c8a-80d1437765d3.jpg)



4. Request / Response



## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 대여(rental)->결제(payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (rental) PaymentService.java 내용중

@FeignClient(name="payment", url="${api.payment.url}")
public interface PaymentService {

    @RequestMapping(method= RequestMethod.POST, path="/payments")//, fallback = PaymentServiceFallback.class)
    public void payship(@RequestBody Payment payment);

}

```

- 예약 이후(@PostPersist) 결제를 요청하도록 처리
```
# Rental.java

    @PostPersist
    public void onPostPersist(){
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publishAfterCommit();


        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.
        library.external.Payment payment = new library.external.Payment();
        // mappings goes here
        payment.setId(this.id);
        payment.setMemberId(this.memberId);
        payment.setBookId(this.bookId);
        payment.setReqState("reserve");

        RentalApplication.applicationContext.getBean(library.external.PaymentService.class)
            .payship(payment);
    }
```
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


# 결제 (payment) 서비스를 잠시 내려놓음
```
#주문처리
http http://localhost:8081/rentals memberId=1 bookId=1  #Fail 
```
:
    ![image](https://user-images.githubusercontent.com/53402465/105120797-3e720500-5b16-11eb-8b2f-d51aea5def12.PNG)

```
#결제서비스 재기동
cd payment
mvn spring-boot:run

#주문처리
http http://localhost:8081/rentals memberId=1 bookId=1   #Success
```
:
    ![image](https://user-images.githubusercontent.com/53402465/105120799-3f0a9b80-5b16-11eb-883e-51588b5d6804.PNG)


# 개인 과제 부분

```
# Rental.java


    @PostUpdate
    public void onPostUpdate(){
        if (this.reqState.equals("cancel") ) {
            Cancelled cancelled = new Cancelled();
            BeanUtils.copyProperties(this, cancelled);
            cancelled.publishAfterCommit();
            System.out.println("cancelled" + cancelled.toJson());
        }  else if (this.reqState.equals("rental") ) {
            Rentaled rentaled = new Rentaled();
            BeanUtils.copyProperties(this, rentaled);
            rentaled.publishAfterCommit();
            System.out.println("rentaled" + rentaled.toJson());
        }  else if (this.reqState.equals("return") ) {
            Returned returned = new Returned();
            BeanUtils.copyProperties(this, returned);
            returned.publishAfterCommit();
            System.out.println("returned" + returned.toJson());
        }  else if (this.reqState.equals("kioskrental") ) {
            KioskRentaled kioskRentaled = new KioskRentaled();
            BeanUtils.copyProperties(this, kioskRentaled);
            kioskRentaled.publishAfterCommit();

            //Following code causes dependency to external APIs
            // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

            library.external.Kiosk kiosk = new library.external.Kiosk();
            // mappings goes here
            kiosk.setId(this.id);
            kiosk.setRentalId(this.id);
            kiosk.setMemberId(this.memberId);
            kiosk.setBookId(this.bookId);
            kiosk.setBookStatus("kioskrental");
            kiosk.setKioskNo(this.kioskNo);
            kiosk.setKioskId(this.kioskId);

            RentalApplication.applicationContext.getBean(library.external.KioskService.class)
                    .selfRental(kiosk);

            System.out.println("kioskRentaled" + kioskRentaled.toJson());
        }  else if (this.reqState.equals("kioskreturn") ) {
            KioskReturned kioskReturned = new KioskReturned();
            BeanUtils.copyProperties(this, kioskReturned);
            kioskReturned.publishAfterCommit();
            System.out.println("kioskReturned" + kioskReturned.toJson());
        }
    }
```
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, Kiosk 시스템이 장애가 나면 Self 대여는 못받는다는 것을 확인:


## kiosk 서비스 장애 시

    ![1](https://user-images.githubusercontent.com/75237785/105282885-a564ec00-5bf2-11eb-8cc6-1545f0485495.jpg)



## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

결제 이후 도서관리(book)시스템으로 결제 완료 여부를 알려주는 행위는 비 동기식으로 처리하여 도서관리 시스템의 처리로 인해 결제주문이 블로킹 되지 않도록 처리한다.
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인(paid)이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
# Payment.java

@Entity
@Table(name="Payment_table")
public class Payment {

 ...
    @PostPersist
    public void onPostPersist(){
        Paid paid = new Paid();
        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();
 ...
}
```
- 도서관리 서비스는 결제완료 이벤트를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
# PolicyHandler.java (book)
...

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_(@Payload Paid paid){
        // 결제완료(예약)
        if(paid.isMe()){
            Book book = new Book();
            book.setId(paid.getBookId());
            book.setMemberId(paid.getMemberId());
            book.setRendtalId(paid.getId());
            book.setBookStatus("reserved");

            bookRepository.save(book);
        }
    }
}

```


도서관리 시스템은 대여/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 도서관리시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# 도서관리 서비스 (book) 를 잠시 내려놓음

#주문처리
http http://localhost:8081/rentals memberId=1 bookId=1  #Success  


```
#주문상태 확인

:
    ![image](https://user-images.githubusercontent.com/53402465/105119392-96f3d300-5b13-11eb-99b0-f9a79bdde8b7.PNG)

``` 

#상점 서비스 기동
cd book
mvn spring-boot:run

#주문상태 확인
http localhost:8080/rentals     # 모든 주문의 상태가 "reserved"으로 확인
```
:
    ![image](https://user-images.githubusercontent.com/53402465/105119394-978c6980-5b13-11eb-8159-65886bee3a81.PNG)


# 개인 과제 부분

## kiosk 대여 상태에서 book 서비스를 삭제하고 kiosk 반납을 진행
![1](https://user-images.githubusercontent.com/75237785/105284000-f1189500-5bf4-11eb-9e35-2a1316d0a7e6.jpg)

## book 서비스 장애 확인
![2](https://user-images.githubusercontent.com/75237785/105284003-f249c200-5bf4-11eb-9bd3-ef49cb7cfffe.jpg)

## book 서비스 기동 시 정상 반납 처리 확인
![3](https://user-images.githubusercontent.com/75237785/105284005-f2e25880-5bf4-11eb-9ce0-8374c4db880c.jpg)



# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 cloudbuild.yml 에 포함되었다.


6. Deploy / Pipeline

![1](https://user-images.githubusercontent.com/75237785/105319507-4ca04e00-5c08-11eb-9613-25940eb55d9c.jpg)




## 동기식 호출 / 서킷 브레이킹 / 장애격리

7. Circuit Breaker

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 대여(rental)-->결제(payment) 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml

hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

- 피호출 서비스(결제:payment) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
```
# Payment.java 

    @PostPersist
    public void onPostPersist(){  //결제이력을 저장한 후 적당한 시간 끌기

        ...
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
$ siege -c100 -t60S -v --content-type "application/json" 'http://rental:8080/rentals POST {"memberId":1, "bookId":1}'
```
 
![image](https://user-images.githubusercontent.com/53402465/105115586-6d837900-5b0c-11eb-81be-448a9d34edea.jpg)
   
![image](https://user-images.githubusercontent.com/53402465/105115589-6f4d3c80-5b0c-11eb-9819-36eab2df1a12.jpg)

- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 
- 약 97%정도 정상적으로 처리되었음.

# 개인 과제 부분

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 키오스크 대여(kioskRental)-->셀프 대여(SelfRental) 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml

hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

- 피호출 서비스(Kiosk) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
```
# Kiosk.java 

    @PostPersist
    public void onPostPersist(){  //결제이력을 저장한 후 적당한 시간 끌기

        ...
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 200명
- 60초 동안 실시

```
siege -c200 -t60S -v --content-type "application/json" 'http://rental:8080/rentals/2 PATCH {"reqState": "kioskrental", "kioskNo": 1}'
```





8. Autoscale (HPA)
### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 


- kiosk의 deployment.yml에 컨테이너 리소스 제한을 설정한다.
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kiosk
  labels:
    app: kiosk
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kiosk
  template:
    metadata:
      labels:
        app: kiosk
    spec:
      containers:
        - name: kiosk
          image: skcc002.azurecr.io/kiosk:latest
          ports:
            - containerPort: 8080
          resources:
            limits:
              cpu: 500m
            requests:
              cpu: 200m
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```            

- 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy kiosk --min=1 --max=10 --cpu-percent=15
```

- HPA에서 CPU 사용율이 모니터링 되고 부하 정도에따라 Replicaset이 10개까지 늘어나는 것을 확인할 수 있다



9. Zero-downtime deploy (readiness probe)
## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
- 새버전으로의 배포 시작

```
kubectl set image ...
```

- readiness 설정

![image](https://user-images.githubusercontent.com/53402465/105119450-b25ede00-5b13-11eb-947b-a2d6da8de334.jpg)

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인

![image](https://user-images.githubusercontent.com/53402465/105119446-b1c64780-5b13-11eb-9af5-c28364c8870c.jpg)

배포기간중 Availability 가 평소 100%에서 97% 대로 떨어지는 것을 확인. 
원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

- readiness 설정 수정

![image](https://user-images.githubusercontent.com/53402465/105119444-b12db100-5b13-11eb-9143-04f44194eb64.jpg)

```
kubectl apply -f kubernetes/deployment.yaml
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:

![image](https://user-images.githubusercontent.com/53402465/105119438-af63ed80-5b13-11eb-981e-bb5b1c754cea.jpg)

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.

package library;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Rental_table")
public class Rental {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;         //예약번호
    private Long memberId;  // 사용자번호
    private Long bookId;    // 책번호
    private String reqState;//요청: "reserve", "cancel", "rental", "return"
    private Long kioskId;
    private Long kioskNo;


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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public String getReqState() {
        return reqState;
    }

    public void setReqState(String reqState) {
        this.reqState = reqState;
    }

    public Long getKioskId() {
        return kioskId;
    }

    public void setKioskId(Long kioskId) {
        this.kioskId = kioskId;
    }

    public Long getKioskNo() {
        return kioskNo;
    }

    public void setKioskNo(Long kioskNo) {
        this.kioskNo = kioskNo;
    }

}

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

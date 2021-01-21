package library;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Book_table")
public class Book {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String bookStatus;
    private Long memberId;
    private Long rendtalId;
    private Long kioskId;

    @PostPersist
    public void onPostPersist(){
        // 예약
        StatusUpdated statusUpdated = new StatusUpdated();
        BeanUtils.copyProperties(this, statusUpdated);
        statusUpdated.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate() {
        if( (bookStatus.equals("kioskrentaled")) ||(bookStatus.equals("kioskreturned")) ) {
            // 키오스크 대여,반납
            KioskStatusUpdated kioskStatusUpdated = new KioskStatusUpdated();
            BeanUtils.copyProperties(this, kioskStatusUpdated);
            kioskStatusUpdated.publishAfterCommit();
        }
        else {
            // 예약취소, 대여,반납
            StatusUpdated statusUpdated = new StatusUpdated();
            BeanUtils.copyProperties(this, statusUpdated);
            statusUpdated.publishAfterCommit();
        }
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getBookStatus() {
        return bookStatus;
    }
    public void setBookStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }

    public Long getMemberId() {
        return memberId;
    }
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getRendtalId() {
        return rendtalId;
    }
    public void setRendtalId(Long rendtalId) {
        this.rendtalId = rendtalId;
    }

    public Long getKioskId() {
        return kioskId;
    }
    public void setKioskId(Long kioskId) {
        this.kioskId = kioskId;
    }




}

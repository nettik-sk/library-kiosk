
package library;

public class KioskReturned extends AbstractEvent {

    private Long id;
    private Long memberId;
    private Long bookId;
    private String rentalStatus;
    private Long kioskNo;
    private Long kioskId;

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
    public String getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
    }
    public Long getKioskNo() {
        return kioskNo;
    }

    public void setKioskNo(Long kioskNo) {
        this.kioskNo = kioskNo;
    }

    public Long getKioskId() {
        return kioskId;
    }

    public void setKioskId(Long kioskId) {
        this.kioskId = kioskId;
    }
}

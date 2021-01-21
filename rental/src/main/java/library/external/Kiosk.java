package library.external;

public class Kiosk {

    private Long id;
    private Long rentalId;
    private String bookStatus;
    private Long bookId;
    private Long memberId;
    private Long kioskNo;
    private Long kioskId;

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
    public Long getKioskId() {
        return kioskId;
    }
    public void setKioskId(Long kioskId) {
        this.kioskId = kioskId;
    }

}

package library;

import library.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationListViewHandler {


    @Autowired
    private ReservationListRepository reservationListRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReserved_then_CREATE_1 (@Payload Reserved reserved) {
        try {
            if (reserved.isMe()) {
                /*
                // view 객체 생성
                  = new ();
                // view 객체에 이벤트의 Value 를 set 함
                .setRentalId(.getId());
                .setBookId(.getBookId());
                .setMemberId(.getMemberId());
                // view 레파지 토리에 save
                Repository.save();

                 */
                // view 객체 생성
                ReservationList reservationList = new ReservationList();
                // view 객체에 이벤트의 Value 를 set 함
                reservationList.setRentalId(reserved.getId());
                reservationList.setBookId(reserved.getBookId());
                reservationList.setMemberId(reserved.getMemberId());
                reservationList.setRentalStatus("reserved");
                // view 레파지 토리에 save
                reservationListRepository.save(reservationList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenStatusUpdated_then_UPDATE_1(@Payload StatusUpdated statusUpdated) {
        try {
            if (statusUpdated.isMe()) {
                /*
                // view 객체 조회
                List<> List = Repository.findByRentalId(.getRendtalId());
                for(  : List){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    // view 레파지 토리에 save
                    Repository.save();
                }
                */
                // view 객체 조회
                System.out.println("############ listener in : " + statusUpdated.toJson());
                Optional<ReservationList> reservationListOptional = reservationListRepository.findById(statusUpdated.getRendtalId());
                ReservationList reservationList = reservationListOptional.get();

                // view 객체에 이벤트의 eventDirectValue 를 set 함
                reservationList.setRentalStatus(statusUpdated.getBookStatus());
                // view 레파지 토리에 save
                reservationListRepository.save(reservationList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaid_then_DELETE_1(@Payload Paid paid) {
        try {
            if (paid.isMe()) {
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
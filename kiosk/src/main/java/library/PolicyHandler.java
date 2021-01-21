package library;

import library.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyHandler{
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }
    @Autowired
    KioskRepository kioskRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverKioskReturned_(@Payload KioskReturned kioskReturned){

        if(kioskReturned.isMe()){

            Optional<Kiosk> kioskOptional = kioskRepository.findById(kioskReturned.getKioskId());
            Kiosk kiosk = kioskOptional.get();

            //kiosk.setId(kioskReturned.getId());
            kiosk.setMemberId(kioskReturned.getMemberId());
            kiosk.setBookId(kioskReturned.getBookId());
            //kiosk.setBookStatus("kioskrental");
            kiosk.setBookStatus("kioskreturn");

            kioskRepository.save(kiosk);

            System.out.println("##### listener  : " + kioskReturned.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverKioskStatusUpdated_(@Payload KioskStatusUpdated kioskStatusUpdated){

        if(kioskStatusUpdated.isMe()){

            // 키오스크 렌탈 상태 값 업데이트

            Optional<Kiosk> kioskOptional = kioskRepository.findById(kioskStatusUpdated.getKioskId());
            Kiosk kiosk = kioskOptional.get();

            //kiosk.setId(kioskStatusUpdated.getRendtalId());
            kiosk.setMemberId(kioskStatusUpdated.getMemberId());
            kiosk.setBookId(kioskStatusUpdated.getId());
            kiosk.setBookStatus(kioskStatusUpdated.getBookStatus());

            kioskRepository.save(kiosk);

            System.out.println("##### listener  : " + kioskStatusUpdated.toJson());
        }
    }

}

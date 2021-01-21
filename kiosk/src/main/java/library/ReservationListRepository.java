package library;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationListRepository extends CrudRepository<ReservationList, Long> {

    //List<> findByRentalId(Long rentalId);

}
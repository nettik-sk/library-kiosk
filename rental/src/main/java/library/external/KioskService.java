
package library.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

//@FeignClient(name="kiosk", url="http://kiosk:8080")
@FeignClient(name="kiosk", url="${api.kiosk.url}")
public interface KioskService {

    @RequestMapping(method= RequestMethod.POST, path="/kiosks")
    public void selfRental(@RequestBody Kiosk kiosk);

}
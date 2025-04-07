package cat.politecnicllevant.backups.restclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "convalidacions")
public interface ConvalidacionsRestClient {
    @GetMapping(path = "/getDataSourceData")
    ResponseEntity<Map<String, String>> getDataSourceData();
}

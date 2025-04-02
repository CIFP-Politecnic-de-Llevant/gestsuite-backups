package cat.politecnicllevant.backups.restclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "gestordocumental")
public interface GestorDocumentalRestClient {
    @GetMapping(path = "/backup", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<ByteArrayResource> getBackup();
}

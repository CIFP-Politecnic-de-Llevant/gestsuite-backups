package cat.politecnicllevant.backups.restclient;

import cat.politecnicllevant.backups.dto.FileUploadDto;
import cat.politecnicllevant.backups.dto.google.FitxerBucketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@FeignClient(name = "core")
public interface CoreRestClient {
    @PostMapping("/googlestorage/uploadobject")
    ResponseEntity<FitxerBucketDto> uploadObject(@RequestParam("objectName") String objectName, @RequestParam("filePath") String filePath, @RequestParam("contentType") String contentType, @RequestParam("bucket") String bucket) throws IOException, GeneralSecurityException;

    @GetMapping(path = "/getDataSourceData")
    ResponseEntity<Map<String, String>> getDataSourceData();

    @PostMapping("/public/fitxerbucket/uploadlocal2")
    ResponseEntity<String> handleFileUpload2(@RequestBody FileUploadDto uploadfile) throws IOException;
}
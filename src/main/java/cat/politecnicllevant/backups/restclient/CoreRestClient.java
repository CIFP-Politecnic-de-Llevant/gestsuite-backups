package cat.politecnicllevant.backups.restclient;

import cat.politecnicllevant.backups.dto.FileUploadDto;
import cat.politecnicllevant.backups.dto.google.FitxerBucketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;

@FeignClient(name = "core")
public interface CoreRestClient {
    @PostMapping("/googlestorage/uploadobject")
    ResponseEntity<FitxerBucketDto> uploadObject(@RequestParam("objectName") String objectName, @RequestParam("filePath") String filePath, @RequestParam("contentType") String contentType, @RequestParam("bucket") String bucket) throws IOException, GeneralSecurityException;

    @PostMapping("/public/fitxerbucket/uploadlocal")
    ResponseEntity<String> handleFileUpload(@RequestPart(value = "file") final MultipartFile uploadfile) throws IOException;

    @PostMapping("/public/fitxerbucket/uploadlocal2")
    ResponseEntity<String> handleFileUpload2(@RequestBody FileUploadDto uploadfile) throws IOException;
}

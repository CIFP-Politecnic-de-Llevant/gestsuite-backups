package cat.politecnicllevant.backups.service;

import cat.politecnicllevant.backups.dto.FileUploadDto;
import cat.politecnicllevant.backups.dto.google.FitxerBucketDto;
import cat.politecnicllevant.backups.restclient.CoreRestClient;
import cat.politecnicllevant.backups.restclient.GestorDocumentalRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final GestorDocumentalRestClient gestorDocumentalRestClient;
    private final CoreRestClient coreRestClient;

    @Scheduled(fixedRate = 60000)  // Cada minut
    public void getBackUpGestorDocumental() throws Exception {
        var response = gestorDocumentalRestClient.getBackup();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {

            long timestamp = System.currentTimeMillis();
            String contentType = "application/sql";
            String uniqueFileName = "backup-gestordocumental-" + timestamp;

            //Upload to Core
            String pathArxiu = "/tmp/" + uniqueFileName + ".sql";
            File f = new File(pathArxiu);
            String remotePath = "";
            try {
                ByteArrayResource resource = response.getBody();
                byte[] dumpBytes = resource.getByteArray();

                FileUploadDto fileUploadDTO = new FileUploadDto(f.getName(), dumpBytes);
                ResponseEntity<String> uploadLocalResponse = coreRestClient.handleFileUpload2(fileUploadDTO);
                remotePath = uploadLocalResponse.getBody();

                String objectName = uniqueFileName + ".sql";
                String bucket = "backup_politecnicllevant";

                ResponseEntity<FitxerBucketDto> uploadResponse = coreRestClient.uploadObject(objectName, remotePath, contentType, bucket);

                if (uploadResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println(objectName + " pujat correctament al bucket " + bucket);
                } else {
                    System.err.println("Error al pujar " + objectName + " al bucket " + bucket + " : " + uploadResponse.getStatusCode());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

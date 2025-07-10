package cat.politecnicllevant.backups.service;

import cat.politecnicllevant.backups.dto.FileUploadDto;
import cat.politecnicllevant.backups.dto.google.FitxerBucketDto;
import cat.politecnicllevant.backups.restclient.ConvalidacionsRestClient;
import cat.politecnicllevant.backups.restclient.CoreRestClient;
import cat.politecnicllevant.backups.restclient.GestorDocumentalRestClient;
import cat.politecnicllevant.backups.restclient.ProfessoratManagerRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final CoreRestClient coreRestClient;
    private final GestorDocumentalRestClient gestorDocumentalRestClient;
    private final ConvalidacionsRestClient convalidacionsRestClient;
    private final ProfessoratManagerRestClient professoratManagerRestClient;

//    @Scheduled(fixedRate = 60000 * 5)  // Cada minut
    @Scheduled(cron = "0 0 23 * * *")
    public void generateBackups() throws Exception {

        // Backup Core
        var coreDataSourceResponse = coreRestClient.getDataSourceData();
        generateBackupPerService(coreDataSourceResponse);

        // Backup GestorDocumental
        var gestorDocumentalDataSourceResponse = gestorDocumentalRestClient.getDataSourceData();
        generateBackupPerService(gestorDocumentalDataSourceResponse);

        // Backup Convalidacions
        var convalidacionsDataSourceResponse = convalidacionsRestClient.getDataSourceData();
        generateBackupPerService(convalidacionsDataSourceResponse);

        // Backup ProfessoratManager
        var professoratManagerDataSourceResponse = professoratManagerRestClient.getDataSourceData();
        generateBackupPerService(professoratManagerDataSourceResponse);
    }

    private void generateBackupPerService(ResponseEntity<Map<String,String>> dataSourceResponse) throws Exception {
        if (dataSourceResponse == null || !dataSourceResponse.getStatusCode().is2xxSuccessful() || dataSourceResponse.getBody() == null)
            return;

        Map<String, String> dataSourceData = dataSourceResponse.getBody();

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "--column-statistics=0",
                "--no-tablespaces",
                "-h", dataSourceData.get("dbHost"),
                "-P", dataSourceData.get("dbPort"),
                "-u", dataSourceData.get("dbUser"),
                dataSourceData.get("dbName")
        );

        Map<String, String> env = pb.environment();
        env.put("MYSQL_PWD", dataSourceData.get("dbPassword"));

        pb.redirectErrorStream(true);
        Process process = pb.start();

        InputStream inputStream = process.getInputStream();
        ByteArrayResource dump_resource = new ByteArrayResource(inputStream.readAllBytes());

        uploadToBucket(dump_resource, dataSourceData.get("dbName"));
    }

    private void uploadToBucket(ByteArrayResource resource, String dbName) throws IOException, GeneralSecurityException {
        LocalDateTime localDateTime = LocalDateTime.now();

        String contentType = "application/sql";
        String uniqueFileName = "backup-" + dbName + "-" + getTimestampFromDate(localDateTime);

        //Upload to Core
        String pathArxiu = "/tmp/" + uniqueFileName + ".sql";
        File f = new File(pathArxiu);
        byte[] dumpBytes = resource.getByteArray();

        FileUploadDto fileUploadDTO = new FileUploadDto(f.getName(), dumpBytes);
        ResponseEntity<String> uploadLocalResponse = coreRestClient.handleFileUpload2(fileUploadDTO);
        String remotePath = uploadLocalResponse.getBody();

        String objectName = getDateStringFromDate(localDateTime) + "/" + uniqueFileName + ".sql";
        String bucket = "backup_politecnicllevant";

        // Upload to bucket
        ResponseEntity<FitxerBucketDto> uploadResponse = coreRestClient.uploadObject(objectName, remotePath, contentType, bucket);

        if (uploadResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println(objectName + " pujat correctament al bucket " + bucket);
        } else {
            System.err.println("Error al pujar " + objectName + " al bucket " + bucket + " : " + uploadResponse.getStatusCode());
        }
    }

    private long getTimestampFromDate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        return localDateTime.atZone(zone).toInstant().toEpochMilli();
    }

    private String getDateStringFromDate(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDateTime.format(formatter);
    }
}

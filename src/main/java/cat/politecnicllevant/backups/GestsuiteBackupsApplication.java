package cat.politecnicllevant.backups;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class GestsuiteBackupsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestsuiteBackupsApplication.class, args);
	}

}

package truonggg.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("schema-export")
public class SchemaExportRunner implements ApplicationRunner {

	private final ApplicationContext applicationContext;

	public SchemaExportRunner(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) {
		// Schema generation happens during startup; khi runner chạy xong thì đóng context và thoát để kết thúc lệnh export.
		System.out.println("Schema export profile: exiting after script generation...");

		if (this.applicationContext instanceof ConfigurableApplicationContext ctx) {
			ctx.close();
		}
		System.exit(0);
	}
}


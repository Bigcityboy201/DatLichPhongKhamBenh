package truonggg.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình static resource cho thư mục uploads:
 * - Cho phép truy cập các file ảnh qua URL: /uploads/**.
 * - Mặc định trỏ tới thư mục "uploads" ở cùng cấp với thư mục chạy ứng dụng.
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
	}
}



package truonggg.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class TestPageConstants {

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_SIZE = 2;
	public static final Pageable PAGEABLE_0_2 = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);

	private TestPageConstants() {
	}
}



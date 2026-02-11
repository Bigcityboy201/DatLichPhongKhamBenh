package truonggg.reponse;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PagedResult<T> {
	private List<T> content;
	private int totalElements;
	private int totalPages;
	private int currentPage;
	private int pageSize;

	public static <T> PagedResult<T> from(Page<?> page, List<T> content) {
		return PagedResult.<T>builder()
				.content(content)
				.totalElements((int) page.getTotalElements())
				.totalPages(page.getTotalPages())
				.currentPage(page.getNumber())
				.pageSize(page.getSize())
				.build();
	}

	public static <T> PagedResult<T> from(Page<T> page) {
		return from(page, page.getContent());
	}
}

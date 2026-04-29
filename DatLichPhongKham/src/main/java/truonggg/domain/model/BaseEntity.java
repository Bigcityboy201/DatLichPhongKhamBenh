package truonggg.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import truonggg.utils.DateUtils;

/**
 * Base entity class that provides common fields for all entities.
 * Automatically manages createdAt and updatedAt timestamps.
 * 
 * Entities can extend this class to inherit timestamp management.
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity {

	// Sử dụng camelCase để khớp với các entity hiện tại (createdAt/updatedAt)
	@Column(name = "createdAt", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updatedAt", nullable = false)
	private LocalDateTime updatedAt;

	/**
	 * Sets createdAt and updatedAt before entity is persisted.
	 * Called automatically by JPA.
	 */
	@PrePersist
	protected void onCreate() {
		LocalDateTime now = DateUtils.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	/**
	 * Updates updatedAt before entity is updated.
	 * Called automatically by JPA.
	 */
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = DateUtils.now();
	}
}


package truonggg.siteInfo.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import truonggg.siteInfo.domain.model.SiteInfo;

@Repository
public interface SiteInfoRepository extends JpaRepository<SiteInfo, Integer> {

}

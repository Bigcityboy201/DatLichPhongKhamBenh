package truonggg.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.SiteInfo;

@Repository
public interface SiteInfoRepository extends JpaRepository<SiteInfo, Integer> {

}

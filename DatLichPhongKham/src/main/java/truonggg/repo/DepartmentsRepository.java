package truonggg.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.Departments;

@Repository
public interface DepartmentsRepository extends JpaRepository<Departments, Integer> {

}

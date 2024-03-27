package rd.useridentity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import rd.useridentity.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
}

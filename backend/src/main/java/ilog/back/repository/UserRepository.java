package ilog.back.repository;

import ilog.back.entity.User;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u.id from User u")
    List<Long> findAllIds();
}

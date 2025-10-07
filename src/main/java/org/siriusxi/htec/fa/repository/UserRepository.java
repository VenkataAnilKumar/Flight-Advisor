package org.siriusxi.htec.fa.repository;

import org.siriusxi.htec.fa.domain.model.User;
import org.siriusxi.htec.fa.infra.exception.NotFoundException;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@CacheConfig(cacheNames = "users")
public interface UserRepository extends CrudRepository<User, Integer> {
    
    @CacheEvict(allEntries = true)
    @NonNull <S extends User> List<S> saveAll(@NonNull Iterable<S> entities);
    
    @Caching(evict = {
        @CacheEvict(key = "#p0.id"),
        @CacheEvict(key = "#p0.username")
    })
    @NonNull <S extends User> S save(@NonNull S entity);
    
    @Cacheable
    @NonNull Optional<User> findById(@NonNull Integer id);
    
    @Cacheable
    default User getById(Integer id) {
        Optional<User> optionalUser = findById(id);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException(User.class, id);
        }
        if (!optionalUser.get().isEnabled()) {
            throw new NotFoundException(User.class, id);
        }
        return optionalUser.get();
    }
    
    @Cacheable
    Optional<User> findByUsernameIgnoreCase(String username);
    
}

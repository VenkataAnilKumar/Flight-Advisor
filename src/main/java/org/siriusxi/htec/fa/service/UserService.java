package org.siriusxi.htec.fa.service;

import lombok.extern.slf4j.Slf4j;
import org.siriusxi.htec.fa.domain.dto.request.CreateUserRequest;
import org.siriusxi.htec.fa.domain.dto.response.UserView;
import org.siriusxi.htec.fa.domain.mapper.UserMapper;
import org.siriusxi.htec.fa.domain.model.User;
import org.siriusxi.htec.fa.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ValidationException;

import static java.lang.String.format;
import static org.siriusxi.htec.fa.domain.model.Role.CLIENT;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    
    private final UserRepository repository;
    private final UserMapper userMapper;
    
    public UserService(UserRepository repository,
                       UserMapper userMapper) {
        this.repository = repository;
        this.userMapper = userMapper;
    }
    
    /**
     * This method is responsible to create a new user.
     *
     * @param request data to create a new user
     * @return UserView of created user.
     */
    @Transactional
    public UserView create(CreateUserRequest request) {
        
        if (repository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new ValidationException("Username exists!");
        }
    
        // Add user
        User user = repository.save(userMapper.toUser(request));
        // Add user authorities
        user.setAuthorities(CLIENT);
        // Update user to add authorities
        repository.save(user);
        
        // Return user view
        return userMapper.toView(user);
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        return repository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(
                () -> new UsernameNotFoundException(
                    format("User with username - %s, not found", username)));
    }
}

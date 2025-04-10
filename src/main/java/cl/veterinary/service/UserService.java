package cl.veterinary.service;

import java.util.Optional;

import cl.veterinary.model.User;

public interface UserService {

   
    Optional<User> findUserById(Long id);
    User saveUser(User user);
    User updateUser(User user);
    void deleteUser(Long id);


}

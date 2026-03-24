package app.dao;

import app.dto.LoginResponseDTO;
import persistence.entity.Profile;

public interface ProfileRepository extends WriteRepository<Profile> {
    LoginResponseDTO authenticate(String username, String password);
}

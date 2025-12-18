package ar.edu.iua.iw3.model.business.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ar.edu.iua.iw3.controllers.dto.UserListResponseDTO;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;

public interface IAuthBusiness {
    String authenticate(String username, String password) throws UnauthorizedException, BusinessException;
    boolean validateToken(String token) throws BusinessException;
    void signup(String username, String password, String role) throws BusinessException;
    List<UserListResponseDTO> getAllUsers() throws BusinessException;
    Page<UserListResponseDTO> getAllUsersPaginated(Pageable pageable) throws BusinessException;
}
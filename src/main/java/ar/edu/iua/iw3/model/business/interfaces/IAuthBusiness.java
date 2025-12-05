package ar.edu.iua.iw3.model.business.interfaces;

import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;

public interface IAuthBusiness {
    String authenticate(String username, String password) throws UnauthorizedException, BusinessException;
    boolean validateToken(String token) throws BusinessException;
}
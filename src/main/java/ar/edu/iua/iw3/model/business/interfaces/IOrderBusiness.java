package ar.edu.iua.iw3.model.business.interfaces;

import java.util.List;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;

public interface IOrderBusiness {

	public List<Order> list() throws BusinessException;

	public Order load(int id) throws NotFoundException, BusinessException;

	public Order add(Order orden) throws FoundException, BusinessException;

	public void delete(int id) throws NotFoundException, BusinessException;
}
package ar.edu.iua.iw3.model.business;

import java.util.List;

import ar.edu.iua.iw3.model.Orden;

public interface IOrdenBusiness {

	public List<Orden> list() throws BusinessException;

	public Orden load(int id) throws NotFoundException, BusinessException;

	public Orden add(Orden orden) throws FoundException, BusinessException;

	public void delete(int id) throws NotFoundException, BusinessException;
}
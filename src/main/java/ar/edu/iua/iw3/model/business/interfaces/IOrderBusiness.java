package ar.edu.iua.iw3.model.business.interfaces;

import java.util.List;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.OrderDetail;
import ar.edu.iua.iw3.model.Reconciliation;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.FoundException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.business.exceptions.UnauthorizedException;

public interface IOrderBusiness {

	public List<Order> list() throws BusinessException;

	public Order load(int id) throws NotFoundException, BusinessException;

	public Order add(Order orden) throws FoundException, BusinessException;

	public void delete(int id) throws NotFoundException, BusinessException;

	// Punto 2: registrar pesaje inicial (tara) -> genera contraseña de activación y cambia estado
	public Order registerInitialWeighing(int id, Double tare) throws NotFoundException, BusinessException;

	// Punto 3: recibir dato de detalle en tiempo real. password opcional segun integración
	public Order addDetail(int id, OrderDetail detail, Integer password) throws NotFoundException, BusinessException, UnauthorizedException;

	// Punto 4: cerrar orden para carga
	public Order closeOrder(int id) throws NotFoundException, BusinessException;

	// Punto 5: registrar pesaje final y obtener conciliación
	public Reconciliation finalizeWeighing(int id, Double finalWeighing) throws NotFoundException, BusinessException;

	// Punto 3b: solicitar preset de la orden (requiere contraseña de activación si aplica)
	public Double getPreset(int id, Integer password) throws NotFoundException, BusinessException, UnauthorizedException;
}
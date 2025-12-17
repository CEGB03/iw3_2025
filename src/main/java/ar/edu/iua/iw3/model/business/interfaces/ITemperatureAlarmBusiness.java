package ar.edu.iua.iw3.model.business.interfaces;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.TemperatureAlarm;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;

public interface ITemperatureAlarmBusiness {

    TemperatureAlarm handle(Order order, Double temperature) throws BusinessException;

    TemperatureAlarm acknowledge(int orderId, String username, String note) throws NotFoundException, BusinessException;
}

package ar.edu.iua.iw3.model.business.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ar.edu.iua.iw3.model.Order;
import ar.edu.iua.iw3.model.OrderDetail;
import ar.edu.iua.iw3.model.business.exceptions.BusinessException;
import ar.edu.iua.iw3.model.business.exceptions.NotFoundException;
import ar.edu.iua.iw3.model.persistence.OrderDetailRepository;
import ar.edu.iua.iw3.model.persistence.OrderRepository;

public class OrderBusinessTest {

    @Mock
    private OrderRepository orderDAO;

    @Mock
    private OrderDetailRepository orderDetailDAO;

    @InjectMocks
    private OrderBusiness orderBusiness;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterInitialWeighing_success() throws Exception {
        Order o = new Order();
        o.setId(1);
        o.setState(1);

        when(orderDAO.findById(1)).thenReturn(Optional.of(o));
        when(orderDAO.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order res = orderBusiness.registerInitialWeighing(1, 12345.0);

        assertNotNull(res);
        assertEquals(2, res.getState());
        assertNotNull(res.getActivationPassword());
        assertTrue(res.getActivationPassword() >= 10000 && res.getActivationPassword() <= 99999);
        verify(orderDAO, times(1)).save(any(Order.class));
    }

    @Test
    public void testAddDetail_withValidPassword_savesDetail() throws Exception {
        Order o = new Order();
        o.setId(2);
        o.setState(2);
        o.setActivationPassword(11111);

        when(orderDAO.findById(2)).thenReturn(Optional.of(o));
        when(orderDetailDAO.save(any(OrderDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(orderDAO.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDetail d = new OrderDetail();
        d.setMassAccumulated(100.0);
        d.setDensity(0.85);
        d.setTemperature(15.0);
        d.setFlow(500.0);

        Order res = orderBusiness.addDetail(2, d, 11111);

        assertNotNull(res);
        assertEquals(100.0, res.getLastMassAccumulated());
        assertEquals(0.85, res.getLastDensity());
        verify(orderDetailDAO, times(1)).save(any(OrderDetail.class));
        verify(orderDAO, times(1)).save(any(Order.class));
    }

    @Test
    public void testAddDetail_withWrongPassword_discards() throws Exception {
        Order o = new Order();
        o.setId(3);
        o.setState(2);
        o.setActivationPassword(22222);

        when(orderDAO.findById(3)).thenReturn(Optional.of(o));

        OrderDetail d = new OrderDetail();
        d.setMassAccumulated(50.0);
        d.setFlow(100.0);

        Order res = orderBusiness.addDetail(3, d, 11111); // wrong password

        // expect no change
        assertNotNull(res);
        assertNull(res.getLastMassAccumulated());
        verify(orderDetailDAO, times(0)).save(any(OrderDetail.class));
    }

}

package com.soen345.Ticket.Reservation.Application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.google.cloud.firestore.Firestore;
@SpringBootTest
class CloudBasedTicketReservationApplicationTests {
	@MockitoBean
	Firestore firestore; //mock firestore for CI

	@Test
	void contextLoads() {
	}

}

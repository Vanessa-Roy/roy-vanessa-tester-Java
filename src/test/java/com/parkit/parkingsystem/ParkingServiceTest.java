package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;
	private static ParkingSpot parkingSpot;
	private static Ticket ticket;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeEach
	private void setUpPerTest() {
		try {
			parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void processExitingVehicleTest() throws Exception {
		// used parkingService.getVehichleRegNumber() -> parkingService.processExitingVehicle()
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		//used parkingService.processExitingVehicle
		when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
		//used parkingService.processExitingVehicle
		when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
		//used parkingService.processExitingVehicle
		when(ticketDAO.updateTicket(ticket)).thenReturn(true);
		
		parkingService.processExitingVehicle();
		
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpot);
	}

	@Test
	public void testProcessIncomingVehicle() {
		// used parkingService.getVehichleType() -> parkingService.getNextParkingNumberIfAvailable() -> parkingService.processIncomingVehicle()
		when(inputReaderUtil.readSelection()).thenReturn(1);
		//GIVEN the slot 1 is available
		// used parkingService.getNextParkingNumberIfAvailable() -> parkingService.processIncomingVehicle()
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		parkingService.processIncomingVehicle();

		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
	}

	@Test
	public void processExitingVehicleTestUnableUpdate() throws Exception {

		// GIVEN updateTicket() return false by default

		parkingService.processExitingVehicle();

		assertEquals(false, ticketDAO.updateTicket(ticket));
		verify(parkingSpotDAO, Mockito.never()).updateParking(parkingSpot);
	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {
		// used parkingService.getVehichleType() -> parkingService.getNextParkingNumberIfAvailable()
		when(inputReaderUtil.readSelection()).thenReturn(1);
		//GIVEN the slot 1 is available
		// used parkingService.getNextParkingNumberIfAvailable()
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		
		parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		
		assertEquals(1, parkingSpot.getId());
	
	
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
		// used parkingService.getVehichleType() -> parkingService.getNextParkingNumberIfAvailable()
		when(inputReaderUtil.readSelection()).thenReturn(1);
		//GIVEN no slots available or an error occurred
		// used parkingService.getNextParkingNumberIfAvailable()
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
		
		parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		
		assertNull(parkingSpot);
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
		//GIVEN the user enters a wrong vehicle type
		// used parkingService.getVehichleType() -> parkingService.getNextParkingNumberIfAvailable()
		when(inputReaderUtil.readSelection()).thenReturn(3);
		
		parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		
		assertNull(parkingSpot);
	}

}

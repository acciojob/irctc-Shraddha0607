package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EnumType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    public Integer getTotalAvailableSeat(Train train, Integer start, Integer end){

        int total_mayBe_seat = train.getNoOfSeats();
        List<Ticket> bookedTicket = train.getBookedTickets();

        // now take each ticket, and get which ticket is not related to stations
        // get ticket from ticketId
        //  get station in integer
        //  now check whether lie in the given station or not
        //  if lies, decrease total passenger related to that list
        for(Ticket ticket : bookedTicket){
            int total_already_seat_booked = ticket.getPassengersList().size();
            int startS = ticket.getFromStation().ordinal();
            int endS = ticket.getToStation().ordinal();
            if((start >=startS && start <=endS) || (end >=startS && end <=endS)){
                total_mayBe_seat -=total_already_seat_booked;
            }
        }
        return total_mayBe_seat;
    }

    public boolean getRouteAvailableOrNot(Train train, Integer startS, Integer endS){
        boolean flag = true;
        String route = train.getRoute();
        int i=0;
        for( i =0; i<route.length(); i++){
            if(route.charAt(i) == ' ') break;
        }
        String start = route.substring(0, i);
        Station station1 = Station.valueOf( start);
        Integer startStation = station1.ordinal();

        int j=0;
        for( j =route.length()-1; j>=0; j--){
            if(route.charAt(j) == ' ') break;
        }
        String end = route.substring(i+1, route.length());
        Station station2 = Station.valueOf( end);           // remember this syntax to change String in enum
        Integer endStation= station2.ordinal();

        // get integer related to that
        if((startS >= startStation &&  startS<=endStation) &&  (endS>=startStation && endS <= endStation)){
            flag = true;
        }
        else{
            flag = false;
        }

        return flag;
    }
    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        //We need to find out the available seats between the given 2 stations.
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.

        //1)  check availability of available seats by same logic of seat availablity api
        int start = bookTicketEntryDto.getFromStation().ordinal();
        int end = bookTicketEntryDto.getToStation().ordinal();
        int id = bookTicketEntryDto.getTrainId();
        Optional<Train> train = trainRepository.findById(id);
        if(train.isEmpty()) return 0;

      int total_mayBe_seat = getTotalAvailableSeat(train.get(), start, end);
//        System.out.println(total_mayBe_seat);
        // Incase the there are insufficient tickets
        int total_seats_to_book = bookTicketEntryDto.getNoOfSeats();
//        System.out.println(total_seats_to_book);
        if(total_mayBe_seat < total_seats_to_book){
            throw new Exception("Less tickets are available");
        }
//        System.out.println("444");

        // 2) check the from and to station lies in route of train or not
        boolean routeAvailable = getRouteAvailableOrNot(train.get(), start, end);
        if(!routeAvailable) throw new Exception("Invalid stations");
//        System.out.println("22222");
        // 3) create a new ticket entity
        Ticket ticket = new Ticket();
        ticket.setTrain(train.get());
        List<Passenger> passengerList = new ArrayList<>();
        for(Passenger passenger: passengerList){
            passengerList.add(passenger);
        }
        ticket.setPassengersList(passengerList);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        int fare = bookTicketEntryDto.getNoOfSeats() * 100;
        ticket.setTotalFare(fare);

        Ticket ticket1 = ticketRepository.save(ticket);
//        System.out.println("3333333");

        // now need to attach with booking person id
        Integer bookingPersonId = bookTicketEntryDto.getBookingPersonId();
        Optional<Passenger> optionalPassenger = passengerRepository.findById(bookingPersonId);
        if(optionalPassenger.isEmpty()) throw new Exception("personId is not present");

        Passenger passenger = optionalPassenger.get();
        List<Ticket> bookedTickets = passenger.getBookedTickets(); // Get the booked ticket list
        bookedTickets.add(ticket1); // Add the new ticket to the list
        passengerRepository.save(passenger);
        return ticket1.getTicketId();


    }
}

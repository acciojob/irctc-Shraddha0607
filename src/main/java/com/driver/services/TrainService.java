package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EnumType;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        List<Station> allRoute = trainEntryDto.getStationRoute();
        StringBuilder route = new StringBuilder();
        for(Station nStation : allRoute){
            if(route.length() > 0){
                route.append(" ");
            }
            route.append(String.valueOf(nStation));
        }
        train.setRoute(route.toString());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setBookedTickets(new ArrayList<>());     // intially train has no booking
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        Train generateTrain = trainRepository.save(train);

        return generateTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        int start = seatAvailabilityEntryDto.getFromStation().ordinal();
        int end = seatAvailabilityEntryDto.getToStation().ordinal();
        int id = seatAvailabilityEntryDto.getTrainId();
        Optional<Train> train = trainRepository.findById(id);
        if(train.isEmpty()) return 0;
        int total_mayBe_seat = train.get().getNoOfSeats();
        List<Ticket> bookedTicket = train.get().getBookedTickets();

        // now take each ticket, and get which ticket is not related to stations
        // 1) get ticket from ticketId
        // 2) get station in integer
        // 3) now check whether lie in the given station or not
        // 4) if lies, decrease total passenger related to that list
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

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        // 1) get train from id
        Optional<Train> train = trainRepository.findById(trainId);
        if(train.isEmpty()) return 0;

        // 2) get station in number-- change enum
        // 3) get route -- start and end
        String route = train.get().getRoute();
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
        Station station2 = Station.valueOf( end);
        Integer endStation= station2.ordinal();

        // get integer related to that
        Integer searchStation  = station.ordinal();
        // 4) take counter
        int totalBoardedPassenger = 0;
        if(searchStation >=startStation && searchStation<=endStation){
            // 5) use loop and apply on bookedTickets
            List<Ticket> bookedTicket = train.get().getBookedTickets();
            for(Ticket ticket: bookedTicket){
                Station fromStation = ticket.getFromStation();
                Integer fromStationInInteger = fromStation.ordinal();
                if(fromStationInInteger == startStation) totalBoardedPassenger++;

            }
            // get from station and if equal to station then increase counter
        }
        else
            throw new Exception("Train is not passing from this station");

        return totalBoardedPassenger;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        // 1) get train and get all tickets
        Optional<Train> train = trainRepository.findById(trainId);
        if(train.isEmpty()) return 0;   // if train not found, we can through error also
        Train trainInfo = train.get();
        List<Ticket> bookedTicket = trainInfo.getBookedTickets();

        // 2) get all passengerlist
        int old_age = 0;
        for(Ticket ticket : bookedTicket){
            List<Passenger> allPassengerInThatTicket = ticket.getPassengersList();
            for(Passenger passenger: allPassengerInThatTicket){
                int age = passenger.getAge();
                old_age = Math.max(old_age, age);
            }
        }

        // 3) get the max_age
        return old_age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        return new ArrayList<>();
    }

}

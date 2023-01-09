package com.aerotravel.flightticketbooking.controller;

import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.Flight;
import com.aerotravel.flightticketbooking.model.Passenger;
import com.aerotravel.flightticketbooking.services.AircraftService;
import com.aerotravel.flightticketbooking.services.AirportService;
import com.aerotravel.flightticketbooking.services.FlightService;
import com.aerotravel.flightticketbooking.services.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    AirportService airportService;
    @Autowired
    AircraftService aircraftService;
    @Autowired
    FlightService flightService;
    @Autowired
    PassengerService passengerService;

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/airport/new")
    public String showAddAirportPage(Model model) {
        model.addAttribute("airport", new Airport());
        return "newAirport";
    }

    @GetMapping("/airport/edit")
    public String showEditAirport(@PathParam("airportId") long airportId, Model model) {
        var record = airportService.getById(airportId);

        model.addAttribute("airport", record);
        return "editAirport";
    }

    @PostMapping("/airport/edit")
    public String editAirport(@PathParam("airportId") long airportId, @Valid Airport airport,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            airport.setAirportId(airportId);
            return "editAirport";
        }

        airportService.save(airport);
        return "redirect:/airports";
    }

    @PostMapping("/airport/new")
    public String saveAirport(@Valid @ModelAttribute("airport") Airport airport, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("airport", new Airport());
            return "newAirport";
        }
        airportService.save(airport);
        model.addAttribute("airports", airportService.getAllPaged(0));
        model.addAttribute("currentPage", 0);
        return "airports";
    }

    @GetMapping("/airport/delete")
    public String deleteAirport(@PathParam("airportId") long airportId, Model model) {
        airportService.deleteById(airportId);
        model.addAttribute("airports", airportService.getAllPaged(0));
        model.addAttribute("currentPage", 0);
        return "airports";
    }

    @GetMapping("/airports")
    public String showAirportsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        model.addAttribute("airports", airportService.getAllPaged(pageNo));
        model.addAttribute("currentPage", pageNo);
        return "airports";
    }

    @GetMapping("/aircraft/new")
    public String showAddAircraftPage(Model model) {
        model.addAttribute("aircraft", new Aircraft());
        return "newAircraft";
    }

    @PostMapping("/aircraft/new")
    public String saveAircraft(@Valid @ModelAttribute("aircraft") Aircraft aircraft, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("aircraft", new Aircraft());
            return "newAircraft";
        }
        aircraftService.save(aircraft);
        model.addAttribute("aircrafts", aircraftService.getAllPaged(0));
        model.addAttribute("currentPage", 0);
        return "aircrafts";
    }

    @GetMapping("/aircraft/edit")
    public String showEditAircraft(@PathParam("aircraftId") long aircraftId, Model model) {
        var record = aircraftService.getById(aircraftId);

        model.addAttribute("aircraft", record);
        return "editAircraft";
    }

    @PostMapping("/aircraft/edit")
    public String editAircraft(@PathParam("aircraftId") long aircraftId, @Valid Aircraft aircraft,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            aircraft.setAircraftId(aircraftId);
            return "editAircraft";
        }

        aircraftService.save(aircraft);
        return "redirect:/aircrafts";
    }

    @GetMapping("/aircraft/delete")
    public String deleteAircraft(@PathParam("aircraftId") long aircraftId, Model model) {
        aircraftService.deleteById(aircraftId);
        model.addAttribute("aircrafts", aircraftService.getAllPaged(0));
        model.addAttribute("currentPage", 0);
        return "aircrafts";
    }

    @GetMapping("/aircrafts")
    public String showAircraftsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        model.addAttribute("aircrafts", aircraftService.getAllPaged(pageNo));
        model.addAttribute("currentPage", pageNo);
        return "aircrafts";
    }

    @GetMapping("/flight/new")
    public String showNewFlightPage(Model model) {
        model.addAttribute("flight", new Flight());
        model.addAttribute("aircrafts", aircraftService.getAll());
        model.addAttribute("airports", airportService.getAll());
        return "newFlight";
    }

    @PostMapping("/flight/new")
    public String saveFlight(@Valid @ModelAttribute("flight") Flight flight, BindingResult bindingResult,
                             @RequestParam("departureAirport") long departureAirport,
                             @RequestParam("destinationAirport") long destinationAirport,
                             @RequestParam("aircraft") long aircraftId,
                             @RequestParam("arrivalTime") String arrivalTime,
                             @RequestParam("departureTime") String departureTime,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("flight", new Flight());
            model.addAttribute("aircrafts", aircraftService.getAll());
            model.addAttribute("airports", airportService.getAll());
            return "newFlight";
        }
        if (departureAirport == destinationAirport) {
            model.addAttribute("sameAirportError", "Departure and destination airport can't be same");
            model.addAttribute("flight", new Flight());
            model.addAttribute("aircrafts", aircraftService.getAll());
            model.addAttribute("airports", airportService.getAll());
            return "newFlight";
        }

        flight.setAircraft(aircraftService.getById(aircraftId));
        flight.setDepartureAirport(airportService.getById(departureAirport));
        flight.setDestinationAirport(airportService.getById(destinationAirport));
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flightService.save(flight);

        model.addAttribute("flights", flightService.getAllPaged(0));
        model.addAttribute("currentPage", 0);
        return "flights";
    }

    @GetMapping("/flight/delete")
    public String deleteFlight(@PathParam("flightId") long flightId, Model model) {
        flightService.deleteById(flightId);
        model.addAttribute("flights", flightService.getAllPaged(0));
        model.addAttribute("currentPage", 0);
        return "flights";
    }

    @GetMapping("/flights")
    public String showFlightsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        model.addAttribute("flights", flightService.getAllPaged(pageNo));
        model.addAttribute("currentPage", pageNo);
        return "flights";
    }

    @GetMapping("/flights_list")
    public String showSimpleFlightsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        model.addAttribute("flights", flightService.getAllPaged(pageNo));
        model.addAttribute("currentPage", pageNo);
        return "flights_list";
    }

    @GetMapping("/flight/search")
    public String showSearchFlightPage(Model model) {
        model.addAttribute("airports", airportService.getAll());
        model.addAttribute("flights", null);
        return "searchFlight";
    }

    @PostMapping("/flight/search")
    public String searchFlight(@RequestParam("departureAirport") long departureAirport,
                               @RequestParam("destinationAirport") long destinationAirport,
                               @RequestParam("departureTime") String departureTime,
                               Model model) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate deptTime = LocalDate.parse(departureTime, dtf);
        Airport depAirport = airportService.getById(departureAirport);
        Airport destAirport = airportService.getById(destinationAirport);

        if (departureAirport == destinationAirport) {
            model.addAttribute("AirportError", "Departure and destination airport cant be same!");
            model.addAttribute("airports", airportService.getAll());
            return "searchFlight";
        }
        List<Flight> flights = flightService.getAllByAirportAndDepartureTime(depAirport, destAirport, deptTime);
        if (flights.isEmpty()) {
            model.addAttribute("notFound", "No Record Found!");
        } else {
            model.addAttribute("flights", flights);
        }

        model.addAttribute("airports", airportService.getAll());
        return "searchFlight";
    }

    @GetMapping("/flight/book")
    public String showBookFlightPage(Model model) {
        model.addAttribute("airports", airportService.getAll());
        return "bookFlight";
    }

    @PostMapping("/flight/book")
    public String searchFlightToBook(@RequestParam("departureAirport") long departureAirport,
                                     @RequestParam("destinationAirport") long destinationAirport,
                                     @RequestParam("departureTime") String departureTime,
                                     Model model) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate deptTime = LocalDate.parse(departureTime, dtf);
        Airport depAirport = airportService.getById(departureAirport);
        Airport destAirport = airportService.getById(destinationAirport);

        if (departureAirport == destinationAirport) {
            model.addAttribute("AirportError", "Departure and destination airport cant be same!");
            model.addAttribute("airports", airportService.getAll());
            return "bookFlight";
        }
        List<Flight> flights = flightService.getAllByAirportAndDepartureTime(depAirport, destAirport, deptTime);
        if (flights.isEmpty()) {
            model.addAttribute("notFound", "No Record Found!");
        } else {
            model.addAttribute("flights", flights);
        }
        model.addAttribute("airports", airportService.getAll());
        return "bookFlight";
    }

    @GetMapping("/flight/book/new")
    public String showCustomerInfoPage(@RequestParam long flightId, Model model) {
        model.addAttribute("flightId", flightId);
        model.addAttribute("passenger", new Passenger());
        return "newPassenger";
    }

    @PostMapping("/flight/book/new")
    public String bookFlight(@Valid @ModelAttribute("passenger") Passenger passenger, BindingResult bindingResult, @RequestParam("flightId") long flightId, Model model) {
        Flight flight = flightService.getById(flightId);
        Passenger passenger1 = passenger;
        passenger1.setFlight(flight);
        passengerService.save(passenger1);
        model.addAttribute("passenger", passenger1);
        return "confirmationPage";
    }

    @GetMapping("/flight/book/verify")
    public String showVerifyBookingPage() {
        return "verifyBooking";
    }

    @PostMapping("/flight/book/verify")
    public String showVerifyBookingPageResult(@RequestParam("flightId") long flightId,
                                              @RequestParam("passengerId") long passengerId,
                                              Model model) {

        var flightOptional = flightService.getOptionallyById(flightId);
        if (flightOptional.isPresent()) {
            var flight = flightOptional.get();
            model.addAttribute("flight", flight);
            var passengers = flight.getPassengers();
            Passenger passenger = null;
            for (Passenger p : passengers) {
                if (p.getPassengerId() == passengerId) {
                    passenger = passengerService.getById(passengerId);
                    model.addAttribute("passenger", passenger);
                }
            }
            if (passenger == null) {
                model.addAttribute("notFound", "Passenger " + passengerId + " was Not Found");
            }
        } else {
            model.addAttribute("notFound", "Flight " + flightId + " was Not Found");
        }
        return "verifyBooking";

    }

    @PostMapping("/flight/book/cancel")
    public String cancelTicket(@RequestParam("passengerId") long passengerId, Model model) {
        passengerService.deleteById(passengerId);
        model.addAttribute("flights", flightService.getAllPaged(0));
        model.addAttribute("currentPage", 0);
        return "flights";
    }

    @GetMapping("passengers")
    public String showPassengerList(@RequestParam long flightId, Model model) {
        List<Passenger> passengers = flightService.getById(flightId).getPassengers();
        model.addAttribute("passengers", passengers);
        model.addAttribute("flight", flightService.getById(flightId));
        return "passengers";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }


    @GetMapping("fancy")
    public String showLoginPage1() {
        return "index";
    }
}

package com.JustinThyme.justinthymer.controllers.GroundControl;


import com.JustinThyme.justinthymer.controllers.TwilioReminder.TwillTask;
import com.JustinThyme.justinthymer.models.converters.PacketSeedToSeed;
import com.JustinThyme.justinthymer.models.data.PacketDao;
import com.JustinThyme.justinthymer.models.data.SeedDao;
import com.JustinThyme.justinthymer.models.data.SeedInPacketDao;
import com.JustinThyme.justinthymer.models.data.UserDao;
import com.JustinThyme.justinthymer.models.converters.SeedToPacketSeed;
import com.JustinThyme.justinthymer.models.forms.Packet;
import com.JustinThyme.justinthymer.models.forms.Seed;
import com.JustinThyme.justinthymer.models.forms.SeedInPacket;
import com.JustinThyme.justinthymer.models.forms.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

//import  org.springframework.security.crypto.password;
//import BCryptPasswordEncoder;
//import org.mindrot.jbcrypt.BCrypt;

@Controller
@RequestMapping("JustinThyme")
public class MainController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private SeedDao seedDao;

    @Autowired
    private PacketDao packetDao;

    @Autowired
    private SeedInPacketDao seedInPacketDao;

//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;


    @RequestMapping(value="")
    public String splash(Model model) {

        model.addAttribute("title", "Welcome to JustinThyme");
        return "splash";

    }

    @RequestMapping(value="/login", method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("title", "Log on in!");
        return "/login";
    }

    @RequestMapping(value="/login", method = RequestMethod.POST)
    public String login(Model model, @RequestParam String username, @RequestParam String password, HttpServletResponse response) {

        //model.addAttribute("users", userDao.findAll());
        Iterable<User> users = userDao.findAll();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                model.addAttribute("user", user);
                //add cookie called username
                Cookie userCookie = new Cookie("username", user.getUsername());
                // set cookie to expire in 1 hour
                userCookie.setMaxAge(1 * 60 * 60);
                response.addCookie(userCookie);
                //set loggedIn == 1(true) in database
                user.setLoggedIn(true);
                userDao.save(user);
                // gets the packet associated with that user for display
                Packet userPacket = packetDao.findByUserId(user.getId());

                //makes a list of seeds not picked for display from list of seesInPacket
                List<Seed> seedsToRemove = new ArrayList<>();
                for (SeedInPacket seedInPacket : userPacket.getSeeds()) {
                    Seed aSeed = PacketSeedToSeed.fromPackToSeed(seedInPacket);
                    seedsToRemove.add(aSeed);
                }

                for (Seed seed : seedsToRemove ) {
                    System.out.println("!!!!!!!!" + seed.name);
                }
                List<Seed> seedsLeft = seedDao.findByArea(user.getArea());
                seedsLeft.removeAll(seedsToRemove);

                //TODO Not sure why seesToRemove aren't being removed SEE line 256
                for  (Seed seed : seedsLeft) {
                    System.out.println("&&&&&&&&&&&" + seed.name);
                }


                model.addAttribute("seeds", userPacket.getSeeds());
                model.addAttribute("seedsLeft", seedsLeft);
                //TODO set sessionID and cookie to something other than username for security
                return "/welcome-user";
               }
                else {
                model.addAttribute("title", "No user by that name or incorrect password!");
            }
        }
        return "/login";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logout(Model model) {
        model.addAttribute("title", "Click here to Logout.");
        return "/logout";
    }

    @RequestMapping(value="/logout", method = RequestMethod.POST)
    public String logout(Model model, HttpServletResponse response) {
        model.addAttribute("title", "See ya next Thyme!");
        //Set loggedIn to false
        Iterable<User> users = userDao.findAll();
        for (User user: users) {
            if (!(user.isLoggedIn() == true)) {
                continue;
            }
            user.setLoggedIn(false);
            userDao.save(user);
        }
        //Remove cookie
        Cookie userCookie = new Cookie("username","");
        userCookie.setMaxAge(0);
        response.addCookie(userCookie);
        return "/see-ya";
    }



    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String add(Model model) {
        model.addAttribute("title", "New User!");
        model.addAttribute(new User());
        model.addAttribute("areas", Seed.Area.values());
        return "/signup";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String add(@ModelAttribute @Valid User newUser, Errors errors, Model model,
                      String verifyPassword, HttpServletResponse response) {

        String username = newUser.username;
        String password = newUser.getPassword();


//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String hashedPassword = passwordEncoder.encode(password);

        // username must be unique
        Iterable<User> users = userDao.findAll();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                model.addAttribute("title", "Try again");
                model.addAttribute(newUser);
                model.addAttribute("areas", Seed.Area.values());
                model.addAttribute("userErrorMessage", "That username is taken.");
                return "/signup";
            }
        }

        if (errors.hasErrors() || (!password.equals(verifyPassword))) {
            model.addAttribute("title", "Try again");
            model.addAttribute(newUser);
            model.addAttribute("areas", Seed.Area.values());
            if(password != "" && !password.equals(verifyPassword)) {
                model.addAttribute("errorMessage", "Passwords do not match.");
            }
            return "/signup";
        } else {
            //add cookie called username
            Cookie userCookie = new Cookie("username", username);
            // set cookie to expire in 1 hour
            userCookie.setMaxAge(1 * 60 * 60);
            response.addCookie(userCookie);
            //save new user to database
            userDao.save(newUser);
            //set loggedIn == 1(true) in database
            newUser.setLoggedIn(true);
            model.addAttribute("user", newUser);
            Seed.Area area = newUser.getArea();
            List<Seed> seeds = new ArrayList<>();
            seeds = seedDao.findByArea(area);

            model.addAttribute("seeds", seeds);
            return "/seed-edit";
        }
    }

    @RequestMapping(value = "/seed-edit", method = RequestMethod.GET)
    public String showSeeds(Model model, User newUser) {

        Seed.Area area = newUser.getArea();
        model.addAttribute(new Packet());
        model.addAttribute("seeds", seedDao.findByArea(area));
        model.addAttribute("user", newUser);
        return "/seed-edit";
    }



    @RequestMapping(value = "/seed-edit", method = RequestMethod.POST)
    public String seedListing(HttpSession session, Model model, @RequestParam int[] seedIds,
                              Integer userId) {

        Packet newPacket = new Packet();
        User currentUser = userDao.findOne(userId);
        newPacket.setUser(currentUser);
        packetDao.save(newPacket);
        //2 lists, 1 needed for packet and another to remove Seeds from display list
        //because you cannot remove seedsInPacket from seedDao
        List<SeedInPacket> seedsToPlant = new ArrayList<>();
        List<Seed> pickedSeedsAsSeeds = new ArrayList<>();

        //goes through list of chosen seeds and adds them to user's packet and sets reminder
        for (int seedId : seedIds) {
            Seed seedPicked = seedDao.findOne(seedId);
            pickedSeedsAsSeeds.add(seedPicked);

            //note conversion using factory
            SeedInPacket seedToPlant = SeedToPacketSeed.fromSeedToPacket(seedPicked, newPacket);
            seedToPlant.setReminder(seedToPlant);
            seedsToPlant.add(seedToPlant);
            seedInPacketDao.save(seedToPlant);

        }

        //after all seeds have been converted and reminder turned on, set to packet
        newPacket.setSeeds(seedsToPlant);


        //below needed for TwilioTask
        String number = currentUser.getPhoneNumber();
        Timer timer = new Timer(true); //daemon set to true

        //needed for display
        Seed.Area area = currentUser.getArea();
        List<Seed> notChosenSeeds = seedDao.findByArea(area);
        notChosenSeeds.removeAll(pickedSeedsAsSeeds);


        //loops through the user's seeds and starts the update for each
        for (SeedInPacket seed : newPacket.getSeeds()) {

            if (seed.getReminder() == true) {
                String message = "It's time to plant " + seed.name;
                Date date = seed.getPlantDate();
                timer.schedule(new TwillTask.TwillReminder(message, number), date);
            }
        }


        model.addAttribute("user", currentUser);
        model.addAttribute("seeds", newPacket.getSeeds());
        model.addAttribute("seedsLeft", notChosenSeeds);

        return "/welcome-user";


    }


    @RequestMapping(value="/welcome-user", method=RequestMethod.POST)
    public String welcomeDisplay(HttpSession session, Model model, User loggedUser, @ModelAttribute Packet aPacket,
                                 @RequestParam int[] seedIds, Integer userId) {
        aPacket = packetDao.findOne(userId);

        model.addAttribute("user", loggedUser);
        model.addAttribute("seeds", aPacket.getSeeds());
        return "/welcome-user";
    }

    @RequestMapping(value="/welcome-user-temp")
        public String tempHolder() {
        return "/welcome-user-temp";
    }

    @RequestMapping(value = "/unsubscribe")
    public String displayConstruction() {
        return "/well-wishes";
    }

//    @RequestMapping(value = "/unsubscribe", method = RequestMethod.GET)
//    public String displayUserToRemove(HttpSession session, Model model, @RequestParam int userId) {
//        //System.out.println(session.getAttributeNames());
//        //model.addAttribute("user", userDao.getAll());
//        model.addAttribute("title", "Sayonara!");
//        return "unsubscribe" + userId;
//    }
//
//    @RequestMapping(value = "/unsubscribe/<userId>", method = RequestMethod.POST)
//    public String processUserRemoval(@RequestParam int userId) {
//
//        User exUser = userDao.findById(userId);
//        //Packet moldyPacket = packetDao.findByUserId(userId);
//        //packetDao.delete(moldyPacket);
//        userDao.delete(exUser);
//
//        return "/well-wishes";
//    }



}

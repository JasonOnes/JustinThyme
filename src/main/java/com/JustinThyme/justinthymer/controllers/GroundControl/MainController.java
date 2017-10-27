package com.JustinThyme.justinthymer.controllers.GroundControl;


import com.JustinThyme.justinthymer.controllers.TwilioReminder.TwillTask;
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
import javax.servlet.http.HttpServletRequest;
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


    @RequestMapping(value = "")
    public String splash(Model model) {

        model.addAttribute("title", "Welcome to JustinThyme");
        return "splash";

    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("title", "Log on in!");
        return "/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(Model model, @RequestParam String username, @RequestParam String password, HttpServletResponse response, HttpServletRequest request) {

        //model.addAttribute("users", userDao.findAll());
        Iterable<User> users = userDao.findAll();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                model.addAttribute("user", user);
                // add user to session
                request.getSession().setAttribute("user", user);

                // create and set cookie for username
                Cookie usernameCookie = new Cookie("username", username);
                usernameCookie.setMaxAge(60 * 60);
                response.addCookie(usernameCookie);

                // create and set cookie for sessionId
                long sessionID = (long) (Math.random() * 1000000);
                String sessionId = String.valueOf(sessionID);
                Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
                sessionIdCookie.setMaxAge(60 * 60);
                response.addCookie(sessionIdCookie);

                // save data to database
                user.setSessionId(sessionId);
                userDao.save(user);

                // gets the packet associated with that user for display
                Packet userPacket = packetDao.findByUserId(user.getId());

                //makes a list of seeds not picked for display from list of seedsInPacket
                List<Seed> seedsToRemove = new ArrayList<>();
                for (SeedInPacket seedInPacket : userPacket.getSeeds()) {
                    String name = seedInPacket.getName();
                    List<Seed> aSeed = seedDao.findByName(name);
                    // note returns a list of all the seeds with that name, not most efficient but it works
                    seedsToRemove.addAll(aSeed);
                }


                List<Seed> seedsLeft = seedDao.findByArea(user.getArea());
                seedsLeft.removeAll(seedsToRemove);

                //note seedsLeft will be Seed objects and seeds will be SeedInPacket

                model.addAttribute("title", "Testing seed removal");
                model.addAttribute("seeds", userPacket.getSeeds());
                model.addAttribute("seedsLeft", seedsLeft);

                return "/welcome-user";
            } else {
                model.addAttribute("title", "No user by that name or incorrect password!");
            }
        }
        return "/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Model model) {
        model.addAttribute("title", "Click here to Logout.");
        return "/logout";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(Model model, HttpServletResponse response, HttpServletRequest request) {
        model.addAttribute("title", "See ya next Thyme!");

        // Remove sessionId from database
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("sessionId".equals(cookie.getName())) {
                String sessionId = cookie.getValue();

                Iterable<User> users = userDao.findAll();
                for (User user : users) {
                    if (user.getSessionId().equals(sessionId)) {
                        user.setSessionId("");
                        userDao.save(user);
                    }
                }
            }
        }

        //Remove cookies
        Cookie userCookie = new Cookie("username", "");
        Cookie sessionIdCookie = new Cookie("sessionId", "");
        userCookie.setMaxAge(0);
        sessionIdCookie.setMaxAge(0);
        response.addCookie(userCookie);
        response.addCookie(sessionIdCookie);
        request.getSession().removeAttribute("user");

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
                      String verifyPassword, HttpServletResponse response, HttpServletRequest request) {

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
            if (password != "" && !password.equals(verifyPassword)) {
                model.addAttribute("errorMessage", "Passwords do not match.");
            }
            return "/signup";
        } else {
            // create and set cookie for username
            Cookie usernameCookie = new Cookie("username", username);
            usernameCookie.setMaxAge(60 * 60);
            response.addCookie(usernameCookie);

            // create and set cookie for sessionId
            long sessionID = (long) (Math.random() * 1000000);
            String sessionId = String.valueOf(sessionID);
            Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
            sessionIdCookie.setMaxAge(60 * 60);
            response.addCookie(sessionIdCookie);

            // save data to database
            newUser.setSessionId(sessionId);
            userDao.save(newUser);

            model.addAttribute("user", newUser);
            Seed.Area area = newUser.getArea();
            List<Seed> seeds = new ArrayList<>();
            seeds = seedDao.findByArea(area);

            request.getSession().setAttribute("user", newUser);
            model.addAttribute("seeds", seeds);
            return "/seed-edit";
        }
    }

    @RequestMapping(value = "/seed-edit", method = RequestMethod.GET)
    public String showSeeds(Model model, User newUser, HttpServletRequest request) {

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

        //needed for display note see above @108
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

//
//    @RequestMapping(value = "/welcome-user", method = RequestMethod.POST)
//    public String welcomeDisplay(HttpSession session, Model model, User loggedUser, @ModelAttribute Packet aPacket,
//                                 @RequestParam int[] seedIds, Integer userId) {
//        aPacket = packetDao.findOne(userId);
//
//        model.addAttribute("user", loggedUser);
//        model.addAttribute("seeds", aPacket.getSeeds());
//        return "/welcome-user";
//    }

//
//    @RequestMapping(value = "/welcome-user-temp")
//
//    public String tempHolder(Model model, HttpServletRequest request) {
//        User user = (User) request.getSession().getAttribute("user");
//        if (user == null) {
//            model.addAttribute("title", "Login");
//            return "/splash";
//        }
//
//        return "/welcome-user-temp";
//    }

    @RequestMapping(value ="/welcome-user", method =RequestMethod.GET)
    public String dashboard (Model model, HttpServletRequest request){
        User user = (User)request.getSession().getAttribute("user");

//        if user is not logged in, sent back to splash page
        if(user == null){
            model.addAttribute("title", "Login");
            return "/splash";
        } else{
            Packet aPacket = packetDao.findByUserId(user.getId());

            List<Seed> seedsToRemove = new ArrayList<>();
            for (SeedInPacket seedInPacket : aPacket.getSeeds()) {
                String name = seedInPacket.getName();
                List<Seed> aSeed = seedDao.findByName(name);
                seedsToRemove.addAll(aSeed);
            }

            List<Seed> notChosenSeeds = seedDao.findByArea(user.getArea());
            notChosenSeeds.removeAll(seedsToRemove);

            model.addAttribute("user", user);
            model.addAttribute("seeds", aPacket.getSeeds());
            model.addAttribute("seedsLeft", notChosenSeeds);
        }
        return "/welcome-user";
    }

    @RequestMapping (value ="/welcome-user", method = RequestMethod.POST)
    public String dashboardAdd (Model model , @RequestParam int[] seedIds, Integer userId){


        Packet aPacket = packetDao.findByUserId(userId);

        List<SeedInPacket> seedsToPlant = new ArrayList<>();
        List<Seed> pickedSeedsAsSeeds = new ArrayList<>();

        //add selected seed to seedInPacketDao
        for (int seedId : seedIds) {
            Seed seedPicked = seedDao.findOne(seedId);
            pickedSeedsAsSeeds.add(seedPicked);

            SeedInPacket seedToPlant = SeedToPacketSeed.fromSeedToPacket(seedPicked, aPacket);
            seedToPlant.setReminder(seedToPlant);
            seedsToPlant.add(seedToPlant);
            seedInPacketDao.save(seedToPlant);
        }

        User user = userDao.findOne(userId);

        //get seedname from user's seedInPacket
        List<Seed> seedsToRemove = new ArrayList<>();
        for (SeedInPacket seedInPacket : aPacket.getSeeds()) {
            String name = seedInPacket.getName();
            List<Seed> aSeed = seedDao.findByName(name);
            seedsToRemove.addAll(aSeed);
        }

        List<Seed> seedsLeft = seedDao.findByArea(user.getArea());
        seedsLeft.removeAll(seedsToRemove);

        model.addAttribute("user", user);
        model.addAttribute("seeds", aPacket.getSeeds());
        model.addAttribute("seedsLeft",seedsLeft );

        return "/welcome-user";
    }




    @RequestMapping(value = "/unsubscribe", method = RequestMethod.GET)
    public String displayUserToRemove(Model model) {

        model.addAttribute("title", "Sayonara!");
        return "/unsubscribe";
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    public String processUserRemoval(Model model, HttpServletRequest request) {

        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {
            return "redirect:login";
        }

        else {

            //retrieve and iterate over users packet to remove seeds before deleting user
            List<SeedInPacket> seeds = seedInPacketDao.findAll();
            Packet moldyPacket = packetDao.findByUserId(user.getId());
                for (SeedInPacket seed : seeds) {
                    if (seed.getPacket() == moldyPacket) {
                        seedInPacketDao.delete(seed);
                    }
                }
                //this qualifier avoids the error if unsubscribe twice in a row
                if (moldyPacket != null) {
                packetDao.delete(moldyPacket);}
                // "logs out" user before deletion
                request.getSession().removeAttribute("user");
                userDao.delete(user);
                model.addAttribute("title", "Deleted!");
                return "/well-wishes";

            }
        }

    }






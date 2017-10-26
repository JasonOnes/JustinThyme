package com.JustinThyme.justinthymer.controllers.GroundControl;


import com.JustinThyme.justinthymer.controllers.TwilioReminder.TwillTask;
import com.JustinThyme.justinthymer.models.data.PacketDao;
import com.JustinThyme.justinthymer.models.data.SeedDao;
import com.JustinThyme.justinthymer.models.data.UserDao;
import com.JustinThyme.justinthymer.models.forms.Packet;
import com.JustinThyme.justinthymer.models.forms.Seed;
import com.JustinThyme.justinthymer.models.forms.User;
//import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.ListUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

@Controller
@RequestMapping("JustinThyme")
public class MainController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private SeedDao seedDao;

    @Autowired
    private PacketDao packetDao;


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
    public String login(Model model, @RequestParam String username, @RequestParam String password, HttpServletResponse response, HttpServletRequest request) {

        model.addAttribute("users", userDao.findAll());
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
                long sessionID = (long)(Math.random() * 1000000);
                String sessionId = String.valueOf(sessionID);
                Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
                sessionIdCookie.setMaxAge(60 * 60);
                response.addCookie(sessionIdCookie);

                // save data to database
                user.setSessionId(sessionId);
                userDao.save(user);

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
    public String logout(Model model, HttpServletResponse response, HttpServletRequest request) {
        model.addAttribute("title", "See ya next Thyme!");

        // Remove sessionId from database
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies) {
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
            // create and set cookie for username
            Cookie usernameCookie = new Cookie("username", username);
            usernameCookie.setMaxAge(60 * 60);
            response.addCookie(usernameCookie);

            // create and set cookie for sessionId
            long sessionID = (long)(Math.random() * 1000000);
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
    public String seedListing(HttpSession session, Model model, User newUser, @ModelAttribute Packet aPacket, @RequestParam int[] seedIds,
                              Integer userId) {

        //goes through list of chosen seeds and adds them to user's packet
        for (int seedId : seedIds) {
            Seed seedToPlant = seedDao.findOne(seedId);
            aPacket.addSeed(seedToPlant);
            aPacket.setReminder(seedToPlant);//note turns reminder on for all seeds in this sprint
        }

        User currentUser = userDao.findOne(userId);
        aPacket.setUser(currentUser);
        packetDao.save(aPacket);



        String number = currentUser.getPhoneNumber();
        Timer timer = new Timer(true);
        Seed.Area area = currentUser.getArea();

        //loops through the user's seeds and set the update reminder for each
        for (Seed seed : aPacket.getSeeds()) {

            if (seed.getReminder() == true) {
                String message = "It's time to plant " + seed.name;
                Date date = seed.getPlantDate();
                timer.schedule(new TwillTask.TwillReminder(message, number), date);
            }
        }


        List<Seed> notChosenSeeds = seedDao.findByArea(area);
        notChosenSeeds.removeAll(aPacket.getSeeds());


        model.addAttribute("user", currentUser);
        model.addAttribute("packet", aPacket);
        model.addAttribute("seeds",aPacket.getSeeds());
        model.addAttribute("seedsLeft", notChosenSeeds);

        return "/welcome-user";


    }

    @RequestMapping (value = "welcome-user", method = RequestMethod.GET)
    public String dashboard (Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        Packet aPacket = packetDao.findOne(user.getId());

        Seed.Area area = user.getArea();
        List<Seed> notChosenSeeds = seedDao.findByArea(area);
        notChosenSeeds.removeAll(aPacket.getSeeds());


        model.addAttribute("user", user);
        model.addAttribute("seeds", aPacket.getSeeds());
        model.addAttribute("seedsLeft", notChosenSeeds);

        return "/welcome-user";
    }

//    @RequestMapping (value ="welcome-user", method = RequestMethod.POST)
//    public String dahboardAdd (Model model)



    @RequestMapping(value="/welcome-user-temp")
    public String tempHolder(Model model, HttpServletRequest request) {
        User user = (User)request.getSession().getAttribute("user");
        if(user == null){
            model.addAttribute("title", "Login");
            return "/splash";
        }

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

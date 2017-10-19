package com.JustinThyme.justinthymer.controllers.GroundControl;


import com.JustinThyme.justinthymer.controllers.TwilioReminder.TwillTask;
import com.JustinThyme.justinthymer.models.data.PacketDao;
import com.JustinThyme.justinthymer.models.data.SeedDao;
import com.JustinThyme.justinthymer.models.data.UserDao;
import com.JustinThyme.justinthymer.models.forms.Packet;
import com.JustinThyme.justinthymer.models.forms.Seed;
import com.JustinThyme.justinthymer.models.forms.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
        model.addAttribute(new User());
        return "/login";
    }
    // @ModelAttribute @Valid User user, Error errors
    @RequestMapping(value="/login", method = RequestMethod.POST)
    public String login(Model model, User user) {
        User knownUser = userDao.findOne(user.getId());
        // User knownUser = userDao.findOne(findByUsername());

        if (user.getPassword() == knownUser.getPassword()) {
            model.addAttribute("user", user);
            return "/welcome-user";
        } else {
            model.addAttribute("title", "NO user by that name or incorrect password!");
            return "/login";
        }
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
                      String verifyPassword) {

        String username = newUser.username;
        String password = newUser.getPassword();


        if (errors.hasErrors() || (!password.equals(verifyPassword))) {
            if(password != "" && !password.equals(verifyPassword)) {
                model.addAttribute("errorMessage", "Passwords do not match.");
                model.addAttribute("title", "Try again");
                model.addAttribute(newUser);
                model.addAttribute("areas", Seed.Area.values());
            } else if (password.equals(verifyPassword)){
                model.addAttribute("title", "Try again");
                model.addAttribute(newUser);
                model.addAttribute("areas", Seed.Area.values());
            }
            return "/signup";
        } else {
            userDao.save(newUser);
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
    public String seedListing(HttpSession session, Model model, User newUser, @ModelAttribute Packet aPacket, @RequestParam int[] seedIds,
                              Integer userId) {

        //goes through list of chosen seeds and adds them to user's packet
        for (int seedId : seedIds) {
            Seed seedToPlant = seedDao.findOne(seedId);
            aPacket.addSeed(seedToPlant);
            aPacket.setReminder(seedToPlant);//note turns reminder on for all seeds in this sprint
        }


        aPacket.setUser_id(userId);
        packetDao.save(aPacket);
        User currentUser = userDao.findOne(userId);


        String number = currentUser.getPhoneNumber();
        Timer timer = new Timer(true);

        //note below for informational purposes may try and use for current user functionality
        System.out.println("$$$$$$$$$$$$$$$$$" + session.getAttributeNames());
        System.out.println("@@@@@@@@@@" + session.getAttribute(currentUser.username));
        //loops through the user's seeds and set the update reminder for each
        for (Seed seed : aPacket.getSeeds()) {
            String message = "It's time to plant " + seed.name;
            Date date = seed.getPlantDate();
            timer.schedule(new TwillTask.TwillReminder(message, number), date);
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("packet", aPacket);
        model.addAttribute("seeds",aPacket.getSeeds());

        return "/welcome-user";


    }

    @RequestMapping(value="/logout")
    public String whateverDisplay() {
        return "/logout";
    }

    @RequestMapping(value = "/unsubscribe")
    public String displayConstruction() {
        return "/unsubscribe";
    }
//    @RequestMapping(value = "/unsubscribe", method = RequestMethod.GET)
//    public String displayUserToRemove(HttpSession session, Model model, @RequestParam int userId) {
//        System.out.println(session.getAttributeNames());
//        //model.addAttribute("user", userDao.getAll());
//        model.addAttribute("title", "Sayonara!");
//        return "unsubscribe";
//    }
//
//    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
//    public String processUserRemoval(@RequestParam int userId) {
//
//        User exUser = userDao.findById(userId);
//        //Packet moldyPacket = packetDao.findByUserId(userId);
//        //packetDao.delete(moldyPacket);
//        userDao.delete(exUser);
//
//
//
//        return "redirect:";
//    }



}

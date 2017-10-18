package com.JustinThyme.justinthymer.models.forms;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;
import java.util.List;


@Entity
public class Seed {

    @Id
    @GeneratedValue
    private int id;
    public String name;

    public enum Area {

        WESTCOAST ("West Coast"), NORTHWEST("Northwest"), SOUTHWEST ("Southwest"),
        MIDSOUTH ("South"), MIDNORTH ("Mid North"), NORTHEAST ("North East"), SOUTHEAST ("South East"),
        FLORIDA ("Florida");

        private final String name;

        Area(String name) { this.name = name;}

        public String getName() {
            return name;
        }

    }


    public enum Season {
        WINTER, SPRING, SUMMER, FALL
    }

    public Date simpleDateGet (Season aSeason) {
        switch(aSeason) {
            case WINTER:

                plantDate = new Date(117, 11, 21, 19, 05);
                break;
            case SPRING:
                plantDate = new Date(118, 2, 20, 19,05);
                break;
            case SUMMER:
                plantDate = new Date(118, 5, 21, 19, 05);
                break;
            case FALL:
                plantDate = new Date(118, 8, 1, 19, 05);
                break;
            default:
               plantDate = null;
        }
        return plantDate;
    }

    public Area area;
    public Season season;
    public Date plantDate;

    Boolean reminder; //note package private so can set in Packet

//    @ManyToMany
//    private List<Packet> packets = new ArrayList<>();


    //note reminder not in constructor so defaulted to false (intentional)
    public Seed(String aName, Area anArea, Season aSeason) {
        this.name = aName;
        this.area = anArea;
        this.season = aSeason;
        //this.plantDate = aDate; note expand with api later perhaps
        this.plantDate = simpleDateGet(aSeason);
        //this.packets = packets;
        this.reminder = false;
    }

    public Seed () { }

    public int getId() { return id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Area getArea() {
        return area;
    }
    public void setArea(Area area) {
        this.area = area;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public Date getPlantDate() {
        return plantDate;
    }

    public void setPlantDate(Date plantDate) {
        this.plantDate = plantDate;
    }

    //note following in Packet for now
//    public Boolean getReminder() {
//        return reminder;
//    }
//
//    public void setReminder(Boolean reminder) {
//        this.reminder = reminder;
//    }
}
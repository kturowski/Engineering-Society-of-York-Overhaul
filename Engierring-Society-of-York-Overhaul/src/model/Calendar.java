package model;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Calendar {
		
		List<String> dateInfo; 
		Date date = new Date();
		String dateRead = "";
		String weekDay = "";
		String monthName = "";
		String ampm = "";
		int day;
		int month;
		int year;
		int hour;
		int minutes;
		

public Calendar () {
	
}

public Calendar(Date date, String dateRead, List<String> dateInfo, String weekDay, String monthName, String ampm, int day,
		int month, int year, int hour, int minutes)
{
	
	//this gets current date info and puts it in an arraylist, im not sure this will even be useful tbh
	dateInfo = new ArrayList<String>();
	SimpleDateFormat fr = new SimpleDateFormat("E dd.M.yyyy hh:mm a");
	dateRead = fr.format(date);	
	//yes i know i could parse this with a reader but this took like 2 seconds	
	dateInfo.add(0, dateRead.substring(0, 1));  //Day in Week
	dateInfo.add(1, dateRead.substring(2, 4));  //Day in Month
	dateInfo.add(2, dateRead.substring(5, 6));  //Month Name
	dateInfo.add(3, dateRead.substring(7, 11)); //Year
	dateInfo.add(4, dateRead.substring(12, 14));//Hour
	dateInfo.add(5, dateRead.substring(15, 17));//Minutes
	dateInfo.add(6, dateRead.substring(18, 19));//AM/PM

	this.weekDay = weekDay;
	this.monthName = monthName;
	this.ampm = ampm;
	this.day = day;
	this.month = month;
	this.year = year;
	this.hour = hour;
	this.minutes = minutes;
	
}

public String getWeekDay()
{
	return this.weekDay;
}

public String getMonthName()
{
	return this.monthName;
}

public String getampm()
{
	return this.ampm;
}
public int getDay()
{
	return this.day;
}
public int getMonth()
{
	return this.month;
}
public int getYear()
{
	return this.year;
}

public int getHour()
{
	return this.hour;
}
public int getMinutes()
{
	return this.minutes;
}

///////////////////////////////////////// Setters

public void setWeekDay(String weekDay)
{
    this.weekDay = weekDay;
}

public void setMonthName(String monthName)
{
	this.monthName = monthName;
}

public void setampm(String ampm)
{
	this.ampm = ampm;
}
public void setDay(int day)
{
	this.day = day;
}
public void setMonth(int month)
{
	this.month = month;
}
public void setYear(int year)
{
	this.year = year;
}

public void setHour(int hour)
{
	this.hour = hour;
}
public void setMinutes(int minutes)
{
	 this.minutes = minutes;
}

}
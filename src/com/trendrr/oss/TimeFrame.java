/**
 * 
 */
package com.trendrr.oss;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.LazyInit;


/**
 * 
 * 
 * @author dustin
 *
 */
public enum TimeFrame {
	
	
	MILLISECONDS("milliseconds"),
	SECONDS("seconds"),
	MINUTES("minutes"),
	HOURS("hours"),
	DAYS("days"),
	WEEKS("weeks"),
	MONTHS("months"),
	YEARS("years");
	
	protected static Log log = LogFactory.getLog(TimeFrame.class);
	
	TimeFrame(String str) {
		
	}
	

	
//	/**
//	 * returns all timeframe(end) that appear within the current 
//	 * 
//	 * dates >= start
//	 * dates < end (now by default)
//	 * 
//	 * will return [start] if start and end fall on the same timeframe 
//	 * 
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	public List<Date> range(Date start, Date end) {
//		List<Date> dates = new ArrayList<Date>();
//		if (start == null)
//			return dates;
//		
//		
//		if (end == null)
//			end = new Date();
//		if (start.after(end))
//			return dates;
//		
//		Date d = this.end(start);
//		while(d.before(end)) {
//			dates.add(d);
//			d = this.add(d, 1);
//		}
//		if (dates.isEmpty()) {
//			dates.add(d);
//		}
//		
//		return dates;
//	}
	
	public boolean same(Date d1, Date d2) {
		return this.toTrendrrEpoch(d1) == this.toTrendrrEpoch(d2);
		
	}

	public static final Date trendrrEpoch = IsoDateUtil.parse("2000-01-01T05:00:00Z");
	private static final Date epochWeekStart = IsoDateUtil.parse("1999-12-26T05:00:00.00Z");
	
	/**
	 * trendrr epoch is the number of elements since jan 1 2000.  
	 * 
	 * This value makes it easy to implement rolling timeframes without having to 
	 * worry about consistent date time parsing (and the representation is much smaller).
	 * 
	 * Many people would shun this, but we have found it extremely useful.
	 * 
	 * Note that seconds and millis return a Long, everything else returns an Int.
	 * 
	 * @param date
	 * @return
	 */
	public Number toTrendrrEpoch(Date date) {
		if (date == null)
			return -1;
		
		long millis = date.getTime() - trendrrEpoch.getTime();
		if (this == MILLISECONDS) {
			return millis;
		}
		long seconds = (millis / 1000);
		if (this == SECONDS) {
			return seconds;
		}
		int minutes = (int)(seconds / 60);
		if (this == MINUTES) {
			return minutes;
		}
		int hours = (int)(minutes / 60);
		if (this == HOURS) {
			return hours;
		}
		
		if (TimeZone.getTimeZone("America/New_York").inDaylightTime(date)) {
			//add an hour for the date math below.
			hours++;
		}

		int days = (int)(hours / 24);
		if (this == DAYS) {
			return days;
		}
		
		if (this == WEEKS) {
			millis = date.getTime() - epochWeekStart.getTime();
			seconds = (int)(millis / 1000);
			minutes = (int)(seconds / 60);
			hours = (int)(minutes / 60);
			
			if (TimeZone.getTimeZone("America/New_York").inDaylightTime(date)) {
				//add an hour for the date math below.
				hours++;
			}
			days = (int)(hours / 24);
			return (int)(days / 7);
		}
		
		if (this == MONTHS) {
			//get the year.
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
			c.setTime(trendrrEpoch);
			Calendar d = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
			d.setTime(date);
			int years = d.get(Calendar.YEAR) - c.get(Calendar.YEAR);
			return (years*12) + d.get(Calendar.MONTH);
		}
		
		if (this == YEARS) {
			//get the year.
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
			c.setTime(trendrrEpoch);
			
			Calendar d = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
			d.setTime(date);
			return d.get(Calendar.YEAR) - c.get(Calendar.YEAR);
		}
		
		return -1;
	}
	
	
	public Date fromTrendrrEpoch(int val) {
		if (this == WEEKS)
			return this.add(epochWeekStart, val);
		return this.add(trendrrEpoch, val);		
	}
	

/*
 * End and start do not work as there is no consistent way to alter a timezone in a Calendar 
 * then change any fields (try it! it just doesn't work).
 * 
 * This is easily implemented in Joda Time but Id rather not add a dependancy.
 * 	
 */
	
//	/**
//	 * returns the first second of the given timeframe. millisecond is 
//	 * always 0
//	 * 
//	 * when timezone matters (i.e. DAYS) then end is given in America/New_York.
//	 * 
//	 * 
//	 * @param date
//	 * @param frame
//	 * @return
//	 */
//	public Date start(Date date) {
//		if(this == MILLISECONDS){
//			return date;
//		}
//		if (this == SECONDS) {
//			Calendar c = Calendar.getInstance();
//			c.setTime(date);
//			c.set(Calendar.MILLISECOND, 0);
//				return c.getTime();
//		}
//		if (this == MINUTES) {
//			Calendar c = Calendar.getInstance();
//			c.setTime(date);
//			c.set(Calendar.MILLISECOND, 0);
//			c.set(Calendar.SECOND, 0);
//			return c.getTime();
//		}
//		
//		int epoch = this.toTrendrrEpoch(date);
//		Date d = this.fromTrendrrEpoch(epoch);
//		
//		return d;
//	}
//
//	/**
//	 * returns the last second of the given timeframe. millisecond is 
//	 * always 0
//	 * 
//	 * when timezone matters (i.e. DAYS) then end is given in America/New_York.
//	 * 
//	 * 
//	 * @param date
//	 * @param frame
//	 * @return
//	 */
//	public Date end(Date date) {
//		if (this == MILLISECONDS) {
//			return date;
//		}
//		if (this == SECONDS) {
//			return date;
//		}
//		
//		
//		Date d = this.add(date, 1);
//		d = this.start(d);
//		return TimeFrame.SECONDS.add(d, -1);
//	}
	
	public Date add(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		if (this == MILLISECONDS) {
			cal.add(Calendar.MILLISECOND, amount);
			return cal.getTime();
		}
		if (this == SECONDS) {
			cal.add(Calendar.SECOND, amount);
			return cal.getTime();
		}
		if (this == MINUTES) {
			cal.add(Calendar.MINUTE, amount);
			return cal.getTime();
		}
		if (this == HOURS) {
			cal.add(Calendar.HOUR_OF_DAY, amount);
			return cal.getTime();
		}
		if (this == DAYS) {
			cal.add(Calendar.DATE, amount);
			return cal.getTime();
		}
		if (this == WEEKS) {
			cal.add(Calendar.WEEK_OF_YEAR, amount);
			return cal.getTime();
		}
		if (this == MONTHS) {
			cal.add(Calendar.MONTH, amount);
			return cal.getTime();
		}
		if (this == YEARS) {
			cal.add(Calendar.YEAR, amount);
			return cal.getTime();
		}
		return null;
	}
	
	public Date subtract(Date date, int amount) {
		return add(date, -amount);
	}
	
	/**
	 * gets a timeframe instance based on the start and end times.
	 * 
	 * basically the timeframe will be the largest timeframe without going over
	 * 
	 * 
	 * 
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static TimeFrame instance(Date start, Date end) {
		long millis = Math.abs(end.getTime() - start.getTime());
		
		long amount = 1000;
		if (millis < amount) {
			return TimeFrame.MILLISECONDS;
		}
		amount *= 60;
		if (millis < amount) {
			return TimeFrame.SECONDS;
		}
		
		amount *= 60;
		if (millis < amount) {
			return TimeFrame.MINUTES;
		}
		
		amount *= 24;
		if (millis < amount) {
			return TimeFrame.HOURS;
		}
		
		amount *= 7;
		if (millis < amount) {
			return TimeFrame.DAYS;
		}
		
		amount *= 4;
		if (millis < amount) {
			return TimeFrame.WEEKS;
		}
		
		amount *= 12;
		if (millis < amount) {
			return TimeFrame.MONTHS;
		}
		return TimeFrame.YEARS;
		
		
	}
	
	public static TimeFrame instance(String str) {
		String s = str.toLowerCase().trim();
		if (s.startsWith("mil")) {
			return TimeFrame.MILLISECONDS;
		}
		if (s.startsWith("sec")) {
			return TimeFrame.SECONDS;
		}
		if (s.startsWith("min")) {
			return TimeFrame.MINUTES;
		}
		if (s.startsWith("h") || s.startsWith("hr")) {
			return TimeFrame.HOURS;
		}
		if (s.startsWith("da")) {
			return TimeFrame.DAYS;
		}
		if (s.startsWith("w")) {
			return TimeFrame.WEEKS;
		}
		if (s.startsWith("month")) {
			return TimeFrame.MONTHS;
		}
		if (s.startsWith("y")) {
			return TimeFrame.YEARS;
		}
		return null;
	}

	public int compare(TimeFrame frame) {
		Date tmp = new Date();
		Date d1 = this.add(tmp, 1);
		Date d2 = frame.add(tmp, 1);				
		return d1.compareTo(d2);
	}
	
	public static void sort(List<TimeFrame> timeframes) {
		Collections.sort(timeframes, new Comparator<TimeFrame>() {
			@Override
			public int compare(TimeFrame o1, TimeFrame o2) {
				Date tmp = new Date();
				Date d1 = o1.add(tmp, 1);
				Date d2 = o2.add(tmp, 1);				
				return d1.compareTo(d2);
			}
		});
	}
}

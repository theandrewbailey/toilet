package libOdyssey;

import java.util.Date;

/**
 *
 * @author alpha
 */
public class DateRange {

    private Date start;
    private Date end;
    private char dayInterval;

    public DateRange() {
    }

    public DateRange(Date s, Date e, char d) {
        start = s;
        end = e;
        dayInterval = d;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public char getDayInterval() {
        return dayInterval;
    }
}

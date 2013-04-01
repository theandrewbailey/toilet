package libOdyssey;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import libOdyssey.bean.Analyzer;

/**
 *
 * @author alpha
 */
public class RangeGenerator implements Iterable<DateRange>, Iterator<DateRange> {

    public static final char[] DAY_INTERVAL=new char[]{'D','W','M','Q','Y'};
    private char dayInterval = ' ';
    private GregorianCalendar startDate;
    private Date endDate;
    private Analyzer anal;

    public RangeGenerator(Analyzer a){
        anal=a;
    }

    @Override
    public boolean hasNext() {
        if (startDate == null) { // init
            switchModes();
        }
        endDate = startDate.getTime();
        subtract();
        // check
        boolean processed = anal.isRangeProcessed(startDate.getTime(), dayInterval);
        if (!processed && !anal.getSessionsInRange(startDate.getTime(), endDate).isEmpty()) {
            return true;
        }
        if (processed || anal.getSessionsInRange(new Date(0L), startDate.getTime()).isEmpty()) {
            switchModes();
            subtract();
        }
        return tryAgain();
    }

    private boolean tryAgain() {
        try {
            return hasNext();
        } catch (RuntimeException r) {
            return false;
        }
    }

    private void subtract() {
        switch (dayInterval) {
            case 'D':
                startDate.add(GregorianCalendar.DAY_OF_YEAR, -1);
                break;
            case 'W':
                startDate.add(GregorianCalendar.DAY_OF_YEAR, -7);
                break;
            case 'M':
                startDate.add(GregorianCalendar.MONTH, -1);
                break;
            case 'Q':
                startDate.add(GregorianCalendar.MONTH, -3);
                break;
            case 'Y':
                startDate.add(GregorianCalendar.YEAR, -1);
                break;
        }
    }

    private void switchModes() {
        startDate = new GregorianCalendar();
        startDate.set(GregorianCalendar.HOUR, 0);
        startDate.set(GregorianCalendar.MINUTE, 0);
        startDate.set(GregorianCalendar.SECOND, 0);
        startDate.set(GregorianCalendar.MILLISECOND, 0);
        switch (dayInterval) {
            case ' ':
                dayInterval = 'D';
                break;
            case 'D':
                startDate.add(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY - startDate.get(GregorianCalendar.DAY_OF_WEEK));
                dayInterval = 'W';
                break;
            case 'W':
                startDate.set(GregorianCalendar.DAY_OF_MONTH, 1);
                dayInterval = 'M';
                break;
            case 'M':
                startDate.set(GregorianCalendar.DAY_OF_MONTH, 1);
                startDate.set(GregorianCalendar.MONTH, startDate.get(GregorianCalendar.MONTH) / 3 + 1);
                dayInterval = 'Q';
                break;
            case 'Q':
                startDate.set(GregorianCalendar.DAY_OF_YEAR, 1);
                dayInterval = 'Y';
                break;
            case 'Y':
                throw new RuntimeException("End of the line");
        }
    }

    @Override
    public DateRange next() {
        return new DateRange(startDate.getTime(), endDate, dayInterval);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<DateRange> iterator() {
        return this;
    }
}

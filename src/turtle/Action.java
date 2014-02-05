package turtle;

/**
 * Command list entries
 */
enum ActionType {
    FORWARD, TURN
}

public class Action {
    ActionType type;
    int intParam;
    double doubleParam;
    String displayString;
    LineSegment lineSeg;

    public Action(ActionType type, int intParam, double doubleParam,
            String displayString, LineSegment lineSeg) {
        this.type = type;
        this.intParam = intParam;
        this.doubleParam = doubleParam;
        this.displayString = displayString;
        this.lineSeg = lineSeg;
    }

    public String toString() {
        if (displayString == null) {
            return "";
        } else {
            return displayString;
        }
    }
}

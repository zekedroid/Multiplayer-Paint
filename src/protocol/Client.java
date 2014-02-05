package protocol;

import java.util.List;
import java.util.Map;
import java.util.Set;

import adts.Line;

/**
 * A Client is expected to be able to handle responses from the server.
 * Each method below corresponds to a type of response.
 */
public interface Client {
    public void onReceiveUsernameChanged(String rcvdName);
    public void onReceiveBoardIDs(Map<Integer, String> boardNameForID);
    public void onReceiveWelcome(int id);
    public void onReceiveDraw(Line l);
    public void onReceiveBoardLines(List<Line> ls, Set<String> userNames);
    public void onReceiveClear();
    public void onReceiveUsers(int boardID, List<String> users);
    public void onReceiveCurrentBoardID(int boardID);
}

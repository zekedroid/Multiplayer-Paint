package adts;

/**
 * Represents a user with a name and id
 * 
 * Concurrency argument:
 *      The id is a final private integer and the name is a string (immutable). 
 *      The name is the only field that can be changed, so we synchronize the
 *      getter and setter. Thus the class is threadsafe
 */
public class User {
    /**
     * The id of the user, this does not change!
     */
    private final int id;

    /**
     * The name of the user, this could change
     */
    private String name;

    /**
     * Constructs the user with the given id and name
     * 
     * @param id
     *            the id of the user
     * @param name
     *            the name of the user
     */
    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructs a user with the given id. The name is set to be "User" + id
     * (ex. if id=2, the name would be "User2")
     * 
     * @param id
     *            the id of the user
     */
    public User(int id) {
        this(id, "User" + id);
    }

    /**
     * @return the id of this user
     */
    public int getID() {
        return this.id;
    }

    /**
     * @return the name of this user
     */
    public synchronized String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the name of this user
     */
    public synchronized void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name of the user
     */
    @Override
    public String toString() {
        return this.name;
    }
}

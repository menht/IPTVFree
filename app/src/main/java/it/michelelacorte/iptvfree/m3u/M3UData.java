package it.michelelacorte.iptvfree.m3u;

/**
 * M3UData is a class that rapresent link and name of multimedia link.
 * It provides one constructor and two public method for manipulate M3U object.
 *
 * Created by Michele on 23/04/2016.
 */
public class M3UData {
    private String channelName;
    private String channelLink;

    /**
     * Public constructor, it create M3UData object which rapresent multimedia link.
     * @param channelName String
     * @param channelLink String
     */
    public M3UData(String channelName, String channelLink)
    {
        this.channelName = channelName;
        this.channelLink = channelLink;
    }

    /**
     * Return channel link
     * @return String channelLink
     */
    public String getChannelLink() {
        return channelLink;
    }

    /**
     * Return channel name
     * @return String channelName
     */
    public String getChannelName() {
        return channelName;
    }

}

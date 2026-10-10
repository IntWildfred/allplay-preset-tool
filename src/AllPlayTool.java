// AllPlay Preset Tool - minimal AllPlay (AllJoyn) controller. Confirmed on Panasonic SC-PMX150. MIT License.
// Plays a radio stream with net.allplay.MediaPlayer.UpdatePlaylist + Play so a PRESET button can store it.
// Usage:
//   java -Djava.library.path=lib -cp tchaikovsky.jar;. AllPlayTool list
//   java ... AllPlayTool current  [nameFilter]
//   java ... AllPlayTool play  "<url>" "<station name>" "<description>" "<logo url>" [nameFilter]
//   java ... AllPlayTool mcuplay "<url>" "<station name>" "<description>" "<logo url>" [nameFilter]
import org.alljoyn.bus.*;
import org.alljoyn.bus.annotation.Position;
import org.alljoyn.bus.annotation.Signature;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.BusProperty;
import java.util.*;
import java.util.concurrent.*;

public class AllPlayTool {
    static {
        // alljoyn_java.dll (AllJoyn 16.04a "win10" build) imports MSAJApi.dll, which newer Windows no longer ships.
        // Use the real one if present, otherwise preload our stub from the lib folder. Then force the bundled router.
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            // Always preload our stub (works whether or not Windows still has its own MSAJApi.dll).
            String dir = System.getProperty("java.library.path").split(java.io.File.pathSeparator)[0];
            System.load(new java.io.File(dir, "MSAJApi.dll").getAbsolutePath());
        }
        if (System.getProperty("org.alljoyn.bus.address") == null) System.setProperty("org.alljoyn.bus.address", "null:");
        System.loadLibrary("alljoyn_java");
    }

    // ---- AllJoyn data types (net.allplay.MediaPlayer signatures, as exposed by the speaker over AllJoyn) ----
    public static class Item {   // (ssssxsssa{ss}a{sv}v)
        @Position(0) @Signature("s") public String url = "";
        @Position(1) @Signature("s") public String title = "";
        @Position(2) @Signature("s") public String artist = "";
        @Position(3) @Signature("s") public String thumbnailUrl = "";
        @Position(4) @Signature("x") public long durationMs = 0;
        @Position(5) @Signature("s") public String mediaType = "audio";
        @Position(6) @Signature("s") public String album = "";
        @Position(7) @Signature("s") public String genre = "";
        @Position(8) @Signature("a{ss}") public Map<String,String> otherData = new HashMap<>();
        @Position(9) @Signature("a{sv}") public Map<String,Variant> mediumDescription = new HashMap<>();
        @Position(10) @Signature("v") public Variant userData = new Variant("", "s");
        public String toString() {
            String ud; try { ud = userData == null ? "null" : userData.getSignature() + ":" + (userData.getSignature().equals("s") ? userData.getObject(String.class) : "?"); } catch (Exception e) { ud = "?"; }
            String md = "";
            if (mediumDescription != null) for (Map.Entry<String,Variant> e : mediumDescription.entrySet()) {
                try { md += e.getKey() + "=" + (e.getValue().getSignature().equals("s") ? e.getValue().getObject(String.class) : e.getValue().getSignature()) + " "; } catch (Exception ex) { md += e.getKey() + "=? "; }
            }
            return "  url=" + url + "\n  title=" + title + "\n  artist=" + artist + "\n  thumbnailUrl=" + thumbnailUrl
                + "\n  durationMs=" + durationMs + "\n  mediaType=" + mediaType + "\n  album=" + album + "\n  genre=" + genre
                + "\n  otherData=" + otherData + "\n  mediumDescription={" + md + "}\n  userData=" + ud;
        }
    }
    public static class PlayState { // (sxuuuiia(ssssxsssa{ss}a{sv}v))
        @Position(0) @Signature("s") public String state;
        @Position(1) @Signature("x") public long positionMs;
        @Position(2) @Signature("u") public int sampleRate;
        @Position(3) @Signature("u") public int channels;
        @Position(4) @Signature("u") public int bitsPerSample;
        @Position(5) @Signature("i") public int currentIndex;
        @Position(6) @Signature("i") public int nextIndex;
        @Position(7) @Signature("a(ssssxsssa{ss}a{sv}v)") public Item[] items;
    }
    public static class PlaylistInfo {
        @Position(0) @Signature("s") public String controllerType;
        @Position(1) @Signature("s") public String playlistUserData;
    }

    @BusInterface(name = "net.allplay.MediaPlayer")
    public interface MediaPlayer {
        @BusProperty(signature = "(sxuuuiia(ssssxsssa{ss}a{sv}v))") PlayState getPlayState() throws BusException;
        @BusMethod(name = "UpdatePlaylist", signature = "a(ssssxsssa{ss}a{sv}v)iss")
        void UpdatePlaylist(Item[] items, int index, String controllerType, String playlistUserData) throws BusException;
        @BusMethod(name = "Play", signature = "ixb") void Play(int itemIndex, long startPositionMs, boolean pauseStateOnly) throws BusException;
        @BusMethod(name = "Stop") void Stop() throws BusException;
        @BusMethod(name = "GetPlaylistInfo", replySignature = "ss") PlaylistInfo GetPlaylistInfo() throws BusException;
    }
    @BusInterface(name = "net.allplay.MCU")
    public interface MCU {
        @BusMethod(name = "PlayItem", signature = "ssssxss")
        void PlayItem(String url, String title, String artist, String thumbnailUrl, long duration, String album, String genre) throws BusException;
        @BusMethod(name = "GetCurrentItemUrl", replySignature = "s") String GetCurrentItemUrl() throws BusException;
    }

    static final String SERVICE = "net.allplay.MediaPlayer";
    static final short SESSION_PORT = 1;          // confirmed by tchaikovsky/openHAB (AllPlay speakers bind port 1)
    static final String OBJ = "/net/allplay/MediaPlayer";

    public static void main(String[] a) throws Exception {
        if (a.length == 0) { System.out.println("commands: list | current [name] | play url name desc logo [name] | mcuplay url name desc logo [name]"); return; }
        String cmd = a[0];
        String filter = null;
        if (cmd.equals("current") && a.length > 1) filter = a[1];
        if ((cmd.equals("play") || cmd.equals("mcuplay")) && a.length > 5) filter = a[5];

        BusAttachment bus = new BusAttachment("AllPlayTool", BusAttachment.RemoteMessage.Receive);
        check(bus.connect(), "connect to AllJoyn bus (" + System.getProperty("org.alljoyn.bus.address") + ")");
        System.out.println("[info] AllJoyn bus connected via " + System.getProperty("org.alljoyn.bus.address") + " as " + bus.getUniqueName());
        final BlockingQueue<String> found = new LinkedBlockingQueue<>();
        bus.registerBusListener(new BusListener() {
            public void foundAdvertisedName(String name, short transport, String prefix) {
                if (!name.endsWith(".quiet")) found.add(name);
            }
        });
        bus.registerAboutListener(new AboutListener() {
            public void announced(String busName, int version, short port, AboutObjectDescription[] od, Map<String,Variant> data) {
                found.add(busName);
            }
        });
        Status s1 = bus.findAdvertisedName(SERVICE);
        Status s2 = bus.whoImplements(new String[]{SERVICE});
        if (s1 != Status.OK && s2 != Status.OK) { System.out.println("Discovery could not start: " + s1 + " / " + s2); quit(1); }
        System.out.println("Searching AllPlay speakers (10 s)...");
        Set<String> names = new LinkedHashSet<>();
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) { String n = found.poll(500, TimeUnit.MILLISECONDS); if (n != null) names.add(n); }
        if (names.isEmpty()) { System.out.println("NO SE ENCONTRO NINGUNA RADIO AllPlay. Revisa: PC y radio en la misma red; Firewall de Windows permite Java en redes Privadas; radio encendida en modo NETWORK."); quit(2); }

        for (String busName : names) {
            SessionOpts so = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, false, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);
            Mutable.IntegerValue sid = new Mutable.IntegerValue();
            Status st = bus.joinSession(busName, SESSION_PORT, sid, so, new SessionListener());
            if (st == Status.ALLJOYN_JOINSESSION_REPLY_ALREADY_JOINED) { }
            if (st != Status.OK) { System.out.println(busName + ": joinSession failed " + st); continue; }
            String devName = "?";
            try { Map<String,Variant> about = new AboutProxy(bus, busName, sid.value).getAboutData("en");
                  if (about.get("DeviceName") != null) devName = about.get("DeviceName").getObject(String.class); } catch (Exception e) { }
            System.out.println("Speaker: " + devName + "   [" + busName + "]");
            if (filter != null && !devName.toLowerCase().contains(filter.toLowerCase())) { bus.leaveSession(sid.value); continue; }
            ProxyBusObject p = bus.getProxyBusObject(busName, OBJ, sid.value, new Class<?>[]{MediaPlayer.class, MCU.class});
            MediaPlayer mp = p.getInterface(MediaPlayer.class);
            MCU mcu = p.getInterface(MCU.class);
            switch (cmd) {
                case "list": break;
                case "current": {
                    try { PlayState ps = mp.getPlayState();
                          System.out.println(" state=" + ps.state + " index=" + ps.currentIndex + " items=" + (ps.items == null ? 0 : ps.items.length));
                          if (ps.items != null) for (Item it : ps.items) System.out.println(it);
                    } catch (BusException e) { System.out.println(" PlayState error: " + e); }
                    try { PlaylistInfo pi = mp.GetPlaylistInfo(); System.out.println(" controllerType=" + pi.controllerType + "\n playlistUserData=" + pi.playlistUserData); }
                    catch (BusException e) { System.out.println(" GetPlaylistInfo error: " + e); }
                    try { System.out.println(" MCU.GetCurrentItemUrl=" + mcu.GetCurrentItemUrl()); } catch (BusException e) { System.out.println(" MCU.GetCurrentItemUrl not available: " + e); }
                    break; }
                case "play": {
                    Item it = new Item();
                    it.url = a[1]; it.title = a[2]; it.artist = a[3]; it.album = a[3]; it.thumbnailUrl = a[4];
                    it.userData = new Variant(a[2], "s");            // the app puts the station name in userData
                    String controllerType = System.getProperty("controllerType", "com.qualcomm.qce.allplay.radio-AllPlayTool");
                    mp.UpdatePlaylist(new Item[]{it}, 0, controllerType, "");
                    mp.Play(0, 0L, false);
                    System.out.println(" OK: enviado UpdatePlaylist + Play. Si suena, manten un boton de preset 4 segundos.");
                    break; }
                case "mcuplay": {
                    mcu.PlayItem(a[1], a[2], a[3], a[4], 0L, a[3], "");
                    System.out.println(" OK: enviado MCU.PlayItem. Si suena, manten un boton de preset 4 segundos.");
                    break; }
            }
            bus.leaveSession(sid.value);
        }
        quit(0);
    }
    static void quit(int code) { System.out.flush(); Runtime.getRuntime().halt(code); }
    static void check(Status s, String what) { if (s != Status.OK) { System.out.println(what + " failed: " + s); quit(1); } }
}

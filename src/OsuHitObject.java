public class OsuHitObject {
    private final int x; //k1 = 64 , k2 = 192 , k3 = 320 , k4 = 448
    private final int time; //milliseconds

    /**
     * A constructor for a OsuHitObject object
     *
     * @param key       A value for the corresponding note
     * @param msPerNote The amount of milliseconds from when the chart started
     */
    public OsuHitObject(int key, int msPerNote) {
        this.x = key;
        this.time = msPerNote;
    }

    /**
     * Converts a OsuHitObject into a string containing osu hitobject data
     *
     * @return A string with hitobject information for the current instance of a OsuHitObject
     */
    public String toOsuFormat() {
        return String.format("%d,192,%d,1,0,0:0:0:0:", this.x, this.time);
    }
}

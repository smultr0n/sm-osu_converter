import java.io.*;
import java.util.Scanner;

public class OsuFile {
    final String audioFileName;
    final String title;
    final String artist;
    final String version; //Difficulty name

    //TIMING POINTS
    final double beatLength;

    /**
     * A constructor for an OsuFile object
     * @param songInfo Array containing a simfile's song information
     * @param difficulty Array containing a simfile's specified difficulty information
     * @param bpm A simfile's BPM
     *
     */
    public OsuFile(String[] songInfo, String[] difficulty, double bpm) {
        title = songInfo[0];
        artist = songInfo[1];
        audioFileName = songInfo[2];
        version = difficulty[2] + " " + difficulty[3];
        beatLength = 60_000 / bpm;
    }

    /**
     * Scans a template ".osu" file and adds it to an output string with linebreaks, then replacing relevant lines with
     * information from the variables in a instantiated OsuFile object.
     *
     * @return A string containing all data in a .osu file except for hitobjects.
     * @throws FileNotFoundException
     */
    public String toOsuFileFormat() throws FileNotFoundException {
        Scanner in = new Scanner(new File("dummy.osu"));
        String line = in.nextLine();
        String output = "";
        while (in.hasNextLine()) {
            output += line + "\n";
            line = in.nextLine();
        }
        output += "[TimingPoints]\n0,"+this.beatLength+",4,1,0,100,1,0\n\n[HitObjects]\n";
        output = output.replace("AudioFilename:", "AudioFilename:" + this.audioFileName);
        output = output.replace("Title:", "Title:" + this.title);
        output = output.replace("Artist:", "Artist:" + this.artist);
        output = output.replace("Version:", "Version:" + this.version);
        return output;
    }
}

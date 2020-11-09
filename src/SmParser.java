import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SmParser {
    /**
     * Gets a given simfile's BPM and initial offset, calls the method getAllNotes to save all notes into an array, then returns
     * a string from the method measuresToOsuNotes.
     *
     * @param filename   The filename of a simfile
     * @param difficulty The difficulty of a simfile
     * @return String with all notes in a Osu Format
     * @throws IOException
     */
    public static String main(String filename, String[] difficulty) throws IOException {
        double bpm = getBpm(filename);
        int offset = getOffset(filename);
        ArrayList<String> allNotes = getAllNotes(filename, difficulty);
        return allMeasuresToOsuNotes(allNotes, bpm, offset);
    }

    /**
     * Scans a given simfile for information on title, artist and the name of the audio file,
     * then puts that information in to an array
     *
     * @param filename The filename of a simfile
     * @return An array with song information
     * @throws FileNotFoundException
     */
    public static String[] getSongInfo(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));
        String[] songInfo = new String[3];
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.contains("#TITLE:")) {
                songInfo[0] = line.substring(line.indexOf(':') + 1, line.length() - 1);
            }
            if (line.contains("#ARTIST:")) {
                songInfo[1] = line.substring(line.indexOf(':') + 1, line.length() - 1);
            }
            if (line.contains("#MUSIC:")) {
                songInfo[2] = line.substring(line.indexOf(':') + 1, line.length() - 1);
            }
        }
        return songInfo;
    }

    /**
     * Scans a given simfile for the #BPMS tag and checks if it has more than one BPM value,
     * then returns the BPM of the simfile, or returns 0 if there are BPM changes in the simfile
     *
     * @param filename The filename of a simfile
     * @return The simfile's BPM
     * @throws FileNotFoundException
     */
    public static double getBpm(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.contains("#BPMS:")) {
                if (line.contains(",") | in.nextLine().contains(",")) { //Checks the current line and the line after for bpm changes
                    System.out.println("Simfile has invalid bpm and/or bpm changes.");
                }
                else if (!line.contains(";")) { //To circumvent poor .sm formatting
                    return Double.parseDouble(line.substring(line.indexOf('=') + 1));
                } else {
                    return Double.parseDouble(line.substring(line.indexOf('=') + 1, line.indexOf(';')));
                }
            }
        }
        return 0;
    }

    /**
     * Scans a given simfile for the #OFFSET tag and takes the value and converts it from seconds to milliseconds
     *
     * @param filename The filename of a simfile
     * @return The offset in milliseconds
     * @throws FileNotFoundException
     */
    public static int getOffset(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));
        int offset = 0;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.contains("#OFFSET:")) {
                double offsetValue = Double.parseDouble(line.substring(line.indexOf(':') + 1, line.indexOf(';')));
                offset = (int) (offsetValue * 1000); //conversion from seconds to milliseconds
                break;
            }
        }
        return offset;
    }

    /**
     * A helper method for allMeasuresToOsuNotes. Goes through each element in the current measure's array and sets the current millisecond count
     * and key position for each element/note, then instantiates a new OsuHitObject with those values. The instance method toOsuFormat
     * is called on the OsuHitObject, concatenating to the string hitObjectOutput with a linebreak.
     * This is repeated for the whole measure which then returns a string containing a whole measure in Osu formatting.
     *
     * @param notes         The input measure that needs to be converted
     * @param msPerNote     The millisecond count per each note element in the notes parameter
     * @param currentOffset The current offset from the start from the simfile
     * @return String of the measure's notes in Osu Format
     */
    public static String measureToOsuNotes(ArrayList<String> notes, int msPerNote, int currentOffset) {
        String hitObjectOutput = "";
        for (int i = 0; i < notes.size(); i++) {
            for (int j = 0; j < notes.get(i).length(); j++) { //j checks if a note flag is present in the line
                String currentNotes = notes.get(i);
                if (currentNotes.charAt(j) == '1' || currentNotes.charAt(j) == '2' || currentNotes.charAt(j) == '4') {
                    int key = switch (j) {
                        case 0 -> 64;
                        case 1 -> 192;
                        case 2 -> 320;
                        case 3 -> 448;
                        default -> 0;
                    };
                    int currentMs = currentOffset + (msPerNote * i + 1);
                    OsuHitObject hitObject = new OsuHitObject(key, currentMs);
                    hitObjectOutput += hitObject.toOsuFormat() + "\n";
                }
            }
        }
        return hitObjectOutput;
    }

    /**
     * Converts a given measure's time property from beats to milliseconds
     *
     * @param measure All the current notes in a measure
     * @param bpm     The BPM for a measure
     * @return Millisecond count for a beat
     */
    public static int beatToMilliseconds(ArrayList<String> measure, double bpm) {
        int noteType = measure.size();
        int msMeasure = (int) Math.round((60_000 / bpm) * 4);
        return msMeasure / noteType;
    }

    /**
     * Converts array of simfile notes to Osu-formatted notes.
     * Divides input array to smaller subarrays, one for each measure,
     * calculates the timings of the notes within the array, while accounting for current measure and initial offset,
     * from beats and measures to milliseconds,
     * then returns a string of notes divided by newline.
     *
     * @param notes  Array containing all the notes of a simfile
     * @param bpm    The BPM of a simfile
     * @param offset The initial offset of a simfile
     * @return String of all notes as in an Osu format
     */
    public static String allMeasuresToOsuNotes(ArrayList<String> notes, double bpm, int offset) {
        int count = 0;
        int measure = 0;
        String output = "";
        int msPerMeasure = (int) Math.round((60_000 / bpm) * 4);
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).contains("!")) {
                ArrayList<String> measureNotes = new ArrayList<>(notes.subList(count, i));
                count = i + 1;
                int currentOffset = (msPerMeasure * measure) - offset;
                measure++;
                int msPerNote = beatToMilliseconds(measureNotes, bpm);
                output += measureToOsuNotes(measureNotes, msPerNote, currentOffset);
            }
        }
        return output;
    }

    /**
     * Scans a simfile for difficulty information and adds each difficulty's information in a String array
     * then adds that array into an arraylist where each element represents each difficulty in the simfile
     *
     * @param filename The filename of a simfile
     * @return An array list with string arrays with all difficulty data
     * @throws IOException
     */
    public static ArrayList<String[]> getDiff(String filename) throws IOException {
        Scanner in = new Scanner(new File(filename));
        ArrayList<String[]> difficulties = new ArrayList<>();
        int lineNumb = 0;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            lineNumb++;
            if (line.equals("#NOTES:")) {
                String diffType = in.nextLine().replaceAll(":", "").trim();
                String diffAuthor = in.nextLine().replaceAll(":", "").trim();
                String diffName = in.nextLine().replaceAll(":", "").trim();
                String diffNumber = in.nextLine().replaceAll(":", "").trim();
                if (diffAuthor.equals("")) {
                    diffAuthor = "Unkown";
                }
                String diffLine = Integer.toString(lineNumb + 5);
                lineNumb += 4;
                String[] difficulty = {diffType, diffAuthor, diffName, diffNumber, diffLine};
                difficulties.add(difficulty);
            }
        }
        return difficulties;
    }

    /**
     * Scans all the note data from a specified difficulty of a simfile
     * and adds all the notes as elements in an arraylist with seperators for measures
     *
     * @param filename   The filename of a simfile
     * @param difficulty The difficulty of a simfile
     * @return String ArrayList with all note data
     * @throws FileNotFoundException
     */
    public static ArrayList<String> getAllNotes(String filename, String[] difficulty) throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));
        int noteLineIndex = Integer.parseInt(difficulty[4]); //+ 5; //these 5 extra lines skip over diff info
        for (int i = 0; i < noteLineIndex; i++) {
            in.nextLine();
        }
        ArrayList<String> notes = new ArrayList<>();
        while (true) {
            String line = in.nextLine();
            if (line.charAt(0) == ';') { //Signifies the end of note data
                notes.add("!");
                break;
            }
            if (line.charAt(0) == ',') {
                notes.add("!"); //Seperator for measures
            } else {
                notes.add(line);
            }
        }
        return notes;
    }
}

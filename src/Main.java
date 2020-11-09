import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        String filename;
        while (true) { //Ensures that the program can keep going until the user wants to quit
            System.out.println("Enter filename: ");
            while (true) {
                try {
                    filename = in.nextLine();
                    new Scanner(new File(filename));
                    if (SmParser.getBpm(filename) <= 0 || !filename.endsWith(".sm")) {
                        System.out.println("Valid '.sm' file not found, try again:");
                    } else {
                        break;
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Valid '.sm' file not found, try again:");
                }
            }
            ArrayList<String[]> diffs = SmParser.getDiff(filename);
            System.out.println("Select which difficulty you would like to convert:");
            int diffIndex = 0;
            for (String[] diff : diffs) {
                System.out.println(diffs.indexOf(diff) + 1 + ". " + diff[0] + " " + diff[2] + " " + diff[3] + " by " + diff[1]);
                diffIndex++;
            }
            int indexChoice;
            while (true) {
                try {
                    indexChoice = Integer.parseInt(in.nextLine());
                    if (indexChoice <= diffIndex && indexChoice > 0 && diffs.get(indexChoice - 1)[0].equals("dance-single")) {
                        String[] songInfo = SmParser.getSongInfo(filename);
                        String osuNotes = SmParser.main(filename, diffs.get(indexChoice - 1));
                        double bpm = SmParser.getBpm(filename);
                        OsuFile osuFile = new OsuFile(songInfo, diffs.get(indexChoice - 1), bpm);
                        createFile(osuNotes, osuFile);
                        System.out.println("File successfully converted!");
                        break;
                    } else if (indexChoice <= diffIndex && indexChoice > 0 && !diffs.get(indexChoice - 1)[0].equals("dance-single")) {
                        System.out.println("Sorry! Only dance-single difficulties are allowed.");
                        break;
                    } else {
                        System.out.println("Not a valid difficulty, try again:");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Not a valid difficulty, try again:");
                }
            }
            System.out.println("Would you like to convert another file? (y/n)");
            while (true) {
                String choice = in.nextLine();
                if (choice.equalsIgnoreCase("N")) {
                    System.exit(0);
                }
                if (choice.equalsIgnoreCase("Y")) {
                    break;
                } else {
                    System.out.println("Invalid input, try again:");
                }
            }
        }
    }

    /**
     * Creates a complete .osu file with metadata and complete note data
     *
     * @param osuNotes String containing all Osu hitobjects
     * @param osuFile  OsuFile object that contains metadata and misc data
     */
    public static void createFile(String osuNotes, OsuFile osuFile) {
        String filename = osuFile.toOsuFileName();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(filename))) {
            out.write(osuFile.toOsuFileFormat());
            out.write(osuNotes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

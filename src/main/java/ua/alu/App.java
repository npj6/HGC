package ua.alu;

import java.io.File;                  // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner;             // Import the Scanner class to read text files

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    public static void main(String[] args) {
        File myObj = new File(args[0]);
        ArrayList<Card> deckList = new ArrayList<>();

        // try-with-resources: Scanner will be closed automatically
        try (Scanner myReader = new Scanner(myObj)) {
            Pattern card = Pattern.compile("^\\s*(\\d+)\\s*x\\s*(.*\\S)\\s*$");
            Matcher matcher;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                matcher = card.matcher(data);
                if (matcher.find()) {
                    System.out.println(matcher.group(2) + " ("+matcher.group(1)+")");
                    deckList.add(new Card(matcher.group(2), Integer.parseInt(matcher.group(1))));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}



import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Main {

    private static Console console;
    private static ArrayList<String> words;
	
	public static void main(String[] args) {
		
		console = new Console();
        words = new ArrayList<String>();
        populateWords();
		
		console.clear();
		System.out.println("Welcome to the Word of The Day tier list helper app.\n\n");
		System.out.println("Press 'n' to go to the next page, or 'p' to go to the previous page.\n\n");
        String c;
        do {
            c = console.getChar();
            if (c.equals("n"))
                System.out.println("Please input 'n' to go to the next page.");
        } while (!c.equals("n"));
		primaryLoop();
	}

    public static void primaryLoop() {
        int i = 0;
        String c = "";

        while (!c.equals("q")) {
            console.clear();
            System.out.println("Word #" + (i + 1) + ":\t\t" + words.get(i) + "\n\n");
            getDefinitions(words.get(i));

            c = console.getChar();
            if (c.equals("n")) {
                if (i == words.size() - 1)
                    System.out.println("This is the last word!\n\n");
                else
                    i++;
            } else if (c.equals("p")) {
                if (i == 0)
                    System.out.println("This is the first word!\n\n");
                else
                    i--;
            } else if (c.equals("q")) {
                // exit
            } else {
                int num = Integer.parseInt(c);
                if (num > 0 && num <= words.size()) {
                    i = num - 1;
                } else {
                    System.out.println("Unrecognized input.");
                }
            }
        }
        System.out.println("Goodbye!");
        System.exit(0);
    }

    public static void getDefinitions(String word) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.dictionaryapi.dev/api/v2/entries/en/" + word))
                .GET()
                .build();
            
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Type collectionType = new TypeToken<Collection<WordDTO>>(){}.getType();
            Collection<WordDTO> enums = new Gson().fromJson(response.body(), collectionType);

            printDefinitions(enums.iterator().next());
        } catch (Exception e) {
            System.out.println("No definitions found.\n\n");
        }
    }

    public static void printDefinitions(WordDTO word) {
        for (MeaningDTO meaning : word.meanings) {
            System.out.println(word.word + " (" + meaning.partOfSpeech + "):");
            for (DefinitionDTO definition : meaning.definitions) {
                System.out.println("\t" + definition.definition);
                if (definition.example != null) {
                    System.out.println("\tExample: " + definition.example + "\n");
                } else {
                    System.out.print("\n");
                }
            }
            System.out.print("\n");
        }
    }

    public static void populateWords() {
        Scanner wordScanner = null;
        try {
            File wordFile = new File("words.txt");
            wordScanner = new Scanner(wordFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (wordScanner.hasNextLine()) {
            String word = wordScanner.nextLine();
            words.add(word);
        }
    }

    private static class Console {

        Scanner console;

        public Console() {
            console = new Scanner(System.in);
        }

        public void clear() {
            System.out.print("\033[H\033[2J");  
            System.out.flush();
        }

        public String getChar() {
            System.out.print("> ");
            String input = console.nextLine();
            return input;
        }

    }

    class WordDTO {
        String word;
        String phonetic;
        ArrayList<PhoneticsDTO> phonetics;
        String origin;
        ArrayList<MeaningDTO> meanings;
    }

    class PhoneticsDTO {
        String text;
        String audio;
    }

    class MeaningDTO {
        String partOfSpeech;
        ArrayList<DefinitionDTO> definitions;
    }

    class DefinitionDTO {
        String definition;
        String example;
    }

}
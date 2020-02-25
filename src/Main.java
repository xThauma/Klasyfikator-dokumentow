import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

	static final String PATH_TRAIN = "C:/Users/Kamil/Desktop/Semestr 2/Przetwarzanie Jêzyka Naturalnego/PS3/data/train";
	static final String PATH_TEST = "C:/Users/Kamil/Desktop/Semestr 2/Przetwarzanie Jêzyka Naturalnego/PS3/data/test";
	static BufferedReader bf;
	static List<String> data;
	static Set<String> directories;
	static int globalCounter = 0;
	static StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
	static String directioriesString = "";
	static StringBuilder sb;
	static Map<String, String> map = new HashMap<>();
	static String[] stopwords = { "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself",
			"it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being",
			"have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against",
			"between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here",
			"there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s",
			"t", "can", "will", "just", "don", "should", "now", "would", "could", "BBC", "people", "new", "need", "much", "move", "even", "and" };

	static int counter = 0;
	static String[] categories = { "tech", "politics", "business", "entertainment", "sport" };

	public static void main(String[] args) throws IOException {
		loadDirectiories();
		lemmFiles();
		// printValues();
		// writeToFile(map.get("sport"));
		checkOccurs();

	}

	private static void checkOccurs() throws IOException {
		Map<String, Integer> mapWithOccurs = new HashMap<>();
		Map<Integer, Map<String, Integer>> totalMapWithOccurs = new HashMap<>();
		for (Map.Entry<String, String> tempMap : map.entrySet()) {
			mapWithOccurs = new HashMap<>();
			if (tempMap.getKey().equals(categories[globalCounter])) {
				String[] words = tempMap.getValue().split("\\s*(\\s|=>|;)\\s*");
				for (int i = 0; i < words.length; i++) {
					if (mapWithOccurs.containsKey(words[i])) {
						mapWithOccurs.put(words[i], mapWithOccurs.get(words[i]) + 1);
					} else {
						mapWithOccurs.put(words[i], 1);
					}
				}
				totalMapWithOccurs.put(globalCounter++, mapWithOccurs);
			}

		}

//		writeToFile(mapWithOccurs.toString());
//		totalMapWithOccurs.forEach((e, v) -> {
//			System.out.println(e + "!!!!!!!!!!!!!!!!!!!!!!!!-");
//			v.forEach((c, d) -> {
//				System.out.println(c + "-" + d);
//			});
//
//		});
		for (int i = 0; i < totalMapWithOccurs.size(); i++) {
			Map<String, Integer> sorted = totalMapWithOccurs.get(i).entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(40)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
			totalMapWithOccurs.put(i, sorted);

		}

		totalMapWithOccurs.forEach((e, v) -> {
			System.out.println(e + "-" + v);
		});

//		Map<String, Integer> sorted = mapWithOccurs.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(40)
//				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
//		sorted.forEach((e, v) -> {
//			System.out.println(e + "-" + v);
//		});

	}

	public static void writeToFile(String str) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(str.substring(0, 6) + ".txt"));
		writer.write(str);

		writer.close();
	}

	private static void lemmFiles() {
		Pattern pattern = Pattern.compile("[a-zA-Z]*");
		for (Map.Entry<String, String> tempMap : map.entrySet()) {
			String[] words = tempMap.getValue().split("\\s*(\\s|=>|;)\\s*");
			String newLine = "";
			for (int i = 0; i < words.length; i++) {
				Matcher matcher = pattern.matcher(words[i]);
				if (words[i].matches(".*\\d.*") || stringContainsItemFromList(words[i], stopwords) || !matcher.matches() || words[i].length() < 3) {
					continue;
				} else {
					newLine += words[i].toLowerCase() + " ";
				}
			}
			List<String> lemmedList = lemmatizer.lemmatize(newLine);
			newLine = "";
			for (int i = 0; i < lemmedList.size(); i++) {
				newLine += lemmedList.get(i) + " ";
			}
			map.put(tempMap.getKey(), newLine);

			// System.out.println(tempMap.getKey() + " " + tempMap.getValue());
		}
	}

	private static void printValues() {
		for (Map.Entry<String, String> tempMap : map.entrySet()) {
			System.out.println(tempMap.getKey());
		}
	}

	public static void loadDirectiories() throws IOException {
		directories = new HashSet<>();
		try (Stream<Path> paths = Files.walk(Paths.get(PATH_TRAIN))) {
			paths.filter(Files::isRegularFile).forEach(e -> {
				File f = e.toFile();
				String text = "";
				try {
					sb = new StringBuilder();
					String line = null;

					bf = new BufferedReader(new FileReader(f));
					while ((line = bf.readLine()) != null) {
						text += line + "\n";

					}
					for (int i = 0; i < categories.length; i++) {
						if (e.toString().contains(categories[i])) {
							if (map.get(categories[i]) == null) {
								map.put(categories[i], "");
							} else {
								map.put(categories[i], map.get(categories[i]) + " " + text + " ");
							}
						}
					}
					bf.close();
				} catch (FileNotFoundException ef) {
					ef.printStackTrace();
				} catch (IOException eia) {
					eia.printStackTrace();
				}
			});
		}

	}

	public static void loadAllFiles() throws IOException {
		data = new ArrayList<>();
		List<File> filesInFolder = Files.walk(Paths.get(PATH_TEST)).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
		filesInFolder.forEach(event -> {
			try {
				sb = new StringBuilder();
				String line = null;

				bf = new BufferedReader(new FileReader(event));
				while ((line = bf.readLine()) != null) {
					String[] words = line.split("\\s*(\\s|=>|;)\\s*");
					String newLine = "";
					for (int i = 0; i < words.length; i++) {
						if (words[i].matches(".*\\d.*") || stringContainsItemFromList(words[i], stopwords)) {
							continue;
						} else if (words[i].length() > 3) {
							newLine += words[i] + " ";
						}
					}
					List<String> lemmedList = lemmatizer.lemmatize(newLine);
					newLine = "";
					for (int i = 0; i < lemmedList.size(); i++) {
						newLine += lemmedList.get(i) + " ";
					}
					sb.append(newLine);
					// System.out.println(newLine);
				}
				counter++;
				bf.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		System.out.println("Wczytano " + counter + " plików tekstowych.");
	}

	public static boolean stringContainsItemFromList(String inputStr, String[] items) {
		return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
	}

}

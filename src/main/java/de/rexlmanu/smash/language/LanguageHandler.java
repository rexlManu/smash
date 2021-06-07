package de.rexlmanu.smash.language;

import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import de.rexlmanu.smash.utility.LanguageUtils;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class LanguageHandler {

    private File langDirectory;
    private Language fallbackLanguage;
    private List<Language> languages;

    public LanguageHandler(File dataFolder) {
        this.langDirectory = new File(dataFolder, "lang");

        if (!this.langDirectory.exists()) {
            this.langDirectory.mkdir();
            this.copyLanguageToFolder("de_DE");
            this.copyLanguageToFolder("en_US");
        }
        this.languages = this.readLanguagesFromPaths(this.langDirectory.toPath());

        this.fallbackLanguage = this.findLanguage("en_us");
    }

    private void copyLanguageToFolder(String locale) {
        File targetFile = new File(this.langDirectory, locale + ".properties");
        try(InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("lang/" + locale + ".properties")) {

            Files.writeString(targetFile.toPath(), LanguageUtils.convertInputStreamToString(inputStream), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Language findLanguage(String locale) {
        return this.languages.stream().filter(language -> language.locale().equalsIgnoreCase(locale)).findFirst().orElse(this.fallbackLanguage);
    }

    private List<Language> readLanguagesFromPaths(Path directory) {
        try {
            return Files.walk(directory).filter(path -> path.toString().endsWith(".properties")).map(path -> {
                File file = path.toFile();
                try (InputStream inputStream = new FileInputStream(file)) {
                    Properties properties = new Properties();
                    properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    Map<String, String> translations = new HashMap<>();
                    properties.forEach((o, o2) -> translations.put(o.toString(), o2.toString()));
                    return new Language(file.getName().split("\\.")[0].toLowerCase(), translations);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}

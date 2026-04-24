package com.backend_torch.Data.Persistence.API.helper;

import com.backend_torch.Data.Persistence.API.dtos.ProfileFilterDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// NaturalLanguageQueryParser.java
@Component
public class NaturalLanguageQueryParser {

    // Country name → ISO code map
    private static final Map<String, String> COUNTRY_MAP = Map.ofEntries(
            Map.entry("nigeria", "NG"), Map.entry("kenya", "KE"),
            Map.entry("ghana", "GH"), Map.entry("ethiopia", "ET"),
            Map.entry("tanzania", "TZ"), Map.entry("uganda", "UG"),
            Map.entry("south africa", "ZA"), Map.entry("egypt", "EG"),
            Map.entry("morocco", "MA"), Map.entry("senegal", "SN"),
            Map.entry("angola", "AO"), Map.entry("zambia", "ZM"),
            Map.entry("zimbabwe", "ZW"), Map.entry("cameroon", "CM"),
            Map.entry("ivory coast", "CI"), Map.entry("cote d'ivoire", "CI"),
            Map.entry("mali", "ML"), Map.entry("sudan", "SD"),
            Map.entry("mozambique", "MZ"), Map.entry("madagascar", "MG"),
            Map.entry("benin", "BJ"), Map.entry("rwanda", "RW"),
            Map.entry("somalia", "SO"), Map.entry("dr congo", "CD"),
            Map.entry("congo", "CG"), Map.entry("eritrea", "ER"),
            Map.entry("botswana", "BW"), Map.entry("namibia", "NA"),
            Map.entry("malawi", "MW"), Map.entry("tunisia", "TN"),
            Map.entry("algeria", "DZ"), Map.entry("libya", "LY"),
            Map.entry("burkina faso", "BF"), Map.entry("niger", "NE"),
            Map.entry("chad", "TD"), Map.entry("gabon", "GA"),
            Map.entry("togo", "TG"), Map.entry("sierra leone", "SL"),
            Map.entry("liberia", "LR"), Map.entry("gambia", "GM"),
            Map.entry("guinea", "GN"), Map.entry("guinea-bissau", "GW"),
            Map.entry("mauritania", "MR"), Map.entry("cape verde", "CV"),
            Map.entry("djibouti", "DJ"), Map.entry("south sudan", "SS"),
            Map.entry("burundi", "BI"), Map.entry("lesotho", "LS"),
            Map.entry("eswatini", "SZ"), Map.entry("mauritius", "MU"),
            Map.entry("seychelles", "SC"), Map.entry("comoros", "KM"),
            Map.entry("equatorial guinea", "GQ"), Map.entry("central african republic", "CF"),
            Map.entry("western sahara", "EH"), Map.entry("united states", "US"),
            Map.entry("usa", "US"), Map.entry("united kingdom", "GB"),
            Map.entry("uk", "GB"), Map.entry("france", "FR"),
            Map.entry("germany", "DE"), Map.entry("india", "IN"),
            Map.entry("china", "CN"), Map.entry("japan", "JP"),
            Map.entry("brazil", "BR"), Map.entry("canada", "CA"),
            Map.entry("australia", "AU")
    );

    public ProfileFilterDto parse(String query) {
        if (query == null || query.isBlank()) return null;

        String q = query.toLowerCase().trim();
        ProfileFilterDto filter = new ProfileFilterDto();
        filter.setPage(1);
        filter.setLimit(10);

        boolean matched = false;

        // --- Gender ---
        if (q.contains("female") || q.contains("woman") || q.contains("women") || q.contains("girls")) {
            filter.setGender("female");
            matched = true;
        } else if (q.contains("male") || q.contains("man") || q.contains("men") || q.contains("boys")) {
            filter.setGender("male");
            matched = true;
        }

        // --- Age group ---
        if (q.contains("child") || q.contains("children")) {
            filter.setAgeGroup("child");
            matched = true;
        } else if (q.contains("teenager") || q.contains("teen") || q.contains("teenagers")) {
            filter.setAgeGroup("teenager");
            matched = true;
        } else if (q.contains("senior") || q.contains("elderly") || q.contains("old")) {
            filter.setAgeGroup("senior");
            matched = true;
        } else if (q.contains("adult") || q.contains("adults")) {
            filter.setAgeGroup("adult");
            matched = true;
        }

        // --- "young" maps to ages 16-24 (not a stored group) ---
        if (q.contains("young") && filter.getAgeGroup() == null) {
            filter.setMinAge(16);
            filter.setMaxAge(24);
            matched = true;
        }

        // --- min_age from "above/over/older than X" ---
        Pattern abovePattern = Pattern.compile("(above|over|older than)\\s+(\\d+)");
        Matcher aboveMatcher = abovePattern.matcher(q);
        if (aboveMatcher.find()) {
            filter.setMinAge(Integer.parseInt(aboveMatcher.group(2)));
            matched = true;
        }

        // --- max_age from "below/under/younger than X" ---
        Pattern belowPattern = Pattern.compile("(below|under|younger than)\\s+(\\d+)");
        Matcher belowMatcher = belowPattern.matcher(q);
        if (belowMatcher.find()) {
            filter.setMaxAge(Integer.parseInt(belowMatcher.group(2)));
            matched = true;
        }

        // --- age range "between X and Y" ---
        Pattern betweenPattern = Pattern.compile("between\\s+(\\d+)\\s+and\\s+(\\d+)");
        Matcher betweenMatcher = betweenPattern.matcher(q);
        if (betweenMatcher.find()) {
            filter.setMinAge(Integer.parseInt(betweenMatcher.group(1)));
            filter.setMaxAge(Integer.parseInt(betweenMatcher.group(2)));
            matched = true;
        }

        // --- Country: try multi-word first, then single word ---
        String detectedCountry = null;
        // Sort by length descending to match "south africa" before "africa"
        List<String> sortedKeys = COUNTRY_MAP.keySet().stream()
                .sorted((a, b) -> b.length() - a.length())
                .toList();

        for (String countryName : sortedKeys) {
            if (q.contains(countryName)) {
                detectedCountry = COUNTRY_MAP.get(countryName);
                matched = true;
                break;
            }
        }
        filter.setCountryId(detectedCountry);

        if (!matched) return null; // triggers "Unable to interpret query"

        return filter;
    }
}
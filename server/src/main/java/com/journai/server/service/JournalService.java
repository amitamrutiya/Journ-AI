package com.journai.server.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.journai.server.model.Journal;
import com.journai.server.model.Mood;
import com.journai.server.model.User;
import com.journai.server.repository.JournalRepository;
import com.journai.server.repository.UserRepository;

@Service
@Transactional
public class JournalService {

    private static final Logger logger = LoggerFactory.getLogger(JournalService.class);

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class JournalData {
        private String userId;
        private String text;
        private Mood mood;
        private String summary;
        private String reason;
        private String title;

        // Constructors
        public JournalData() {
        }

        public JournalData(String userId, String text, Mood mood, String summary, String reason) {
            this.userId = userId;
            this.text = text;
            this.mood = mood;
            this.summary = summary;
            this.reason = reason;
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Mood getMood() {
            return mood;
        }

        public void setMood(Mood mood) {
            this.mood = mood;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    // private Mood mapMoodToEnum(Mood mood) {
    // if (mood == null) {
    // return Mood.NEUTRAL;
    // }

    // return mood;
    // }

    public Mood mapMoodToEnum(String mood) {
        if (mood == null || mood.isEmpty()) {
            return Mood.NEUTRAL;
        }

        Map<String, Mood> moodMap = new HashMap<>();
        moodMap.put("happy", Mood.HAPPY);
        moodMap.put("sad", Mood.SAD);
        moodMap.put("anxious", Mood.ANXIOUS);
        moodMap.put("neutral", Mood.NEUTRAL);
        moodMap.put("excited", Mood.EXCITED);
        moodMap.put("angry", Mood.ANGRY);
        moodMap.put("peaceful", Mood.PEACEFUL);
        moodMap.put("grateful", Mood.GRATEFUL);
        moodMap.put("frustrated", Mood.FRUSTRATED);
        moodMap.put("worried", Mood.WORRIED);
        moodMap.put("content", Mood.CONTENT);
        moodMap.put("tired", Mood.TIRED);

        return moodMap.getOrDefault(mood.toLowerCase(), Mood.NEUTRAL);
    }

    private String generateTitle(String text) {
        // Remove HTML tags
        String cleanText = text.replaceAll("<[^>]*>", "");

        // Remove markdown formatting
        cleanText = cleanText
                .replaceAll("^#{1,6}\\s+", "")
                .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                .replaceAll("\\*([^*]+)\\*", "$1")
                .replaceAll("__([^_]+)__", "$1")
                .replaceAll("_([^_]+)_", "$1")
                .replaceAll("~~([^~]+)~~", "$1")
                .replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("`([^`]+)`", "$1")
                .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1")
                .replaceAll("^\\s*[-*+]\\s+", "")
                .replaceAll("^\\s*\\d+\\.\\s+", "")
                .replaceAll("^\\s*>\\s+", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] words = cleanText.split("\\s+");
        List<String> titleWords = Arrays.asList(words).subList(0, Math.min(words.length, 8));
        String title = String.join(" ", titleWords);

        if (title.length() > 50) {
            title = title.substring(0, 47) + "...";
        }

        return title.isEmpty() ? "Journal Entry" : title;
    }

    private int calculateWordCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        // Remove HTML tags and count words
        String textOnly = content.replaceAll("<[^>]*>", " ");
        String[] words = textOnly.trim().split("\\s+");
        return words.length;
    }

    public Journal saveJournal(JournalData journalData) {
        try {
            logger.debug("Saving journal for user: {}", journalData.getUserId());

            // Ensure user exists
            User user = userRepository.findById(journalData.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + journalData.getUserId()));

            // Generate title if not provided
            String title = journalData.getTitle() != null ? journalData.getTitle()
                    : generateTitle(journalData.getText());

            // Convert mood to enum
            Mood moodEnum = mapMoodToEnum(journalData.getMood());

            String content = journalData.getText();
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("Journal content cannot be empty");
            }

            // Create and save journal
            Journal journal = new Journal();
            journal.setUser(user);
            journal.setTitle(title);
            journal.setContent(content);
            journal.setMood(moodEnum);
            journal.setSummary(journalData.getSummary());

            Journal savedJournal = journalRepository.save(journal);

            logger.info("Journal saved successfully for user: {}, journalId: {}",
                    journalData.getUserId(), savedJournal.getId());

            return savedJournal;

        } catch (Exception e) {
            logger.error("Error saving journal for user: {}", journalData.getUserId(), e);
            throw new RuntimeException("Failed to save journal", e);
        }
    }

    public Journal updateJournal(String journalId, String userId, JournalData journalData) {
        try {
            Journal journal = journalRepository.findByIdAndUser_Id(journalId, userId)
                    .orElse(null);

            if (journal == null) {
                return null;
            }

            // Update journal fields
            String title = journalData.getTitle() != null ? journalData.getTitle()
                    : generateTitle(journalData.getText());
            Mood moodEnum = mapMoodToEnum(journalData.getMood());

            String content = journalData.getText();
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("Journal content cannot be empty");
            }

            journal.setTitle(title);
            journal.setContent(content);
            journal.setMood(moodEnum);
            journal.setSummary(journalData.getSummary());

            Journal updatedJournal = journalRepository.save(journal);

            logger.info("Journal updated successfully: {}", journalId);
            return updatedJournal;

        } catch (Exception e) {
            logger.error("Error updating journal: {}", journalId, e);
            throw new RuntimeException("Failed to update journal", e);
        }
    }

    public List<Journal> getUserJournals(String userId, int limit, int offset, String selectedMonth) {
        try {
            Pageable pageable = PageRequest.of(offset / limit, limit);

            if (selectedMonth != null && !selectedMonth.isEmpty()) {
                YearMonth yearMonth = YearMonth.parse(selectedMonth);
                LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

                return journalRepository.findByUserIdAndDateRange(userId, startDate, endDate);
            } else {
                return journalRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable).getContent();
            }

        } catch (Exception e) {
            logger.error("Error fetching user journals for user: {}", userId, e);
            throw new RuntimeException("Failed to fetch user journals", e);
        }
    }

    public Journal getJournalById(String journalId, String userId) {
        return journalRepository.findByIdAndUser_Id(journalId, userId).orElse(null);
    }

    public void deleteJournal(String journalId, String userId) {
        try {
            // Verify the journal exists and belongs to the user before deleting
            journalRepository.findByIdAndUser_Id(journalId, userId)
                    .orElseThrow(() -> new RuntimeException("Journal not found"));

            journalRepository.deleteByIdAndUser_Id(journalId, userId);

            logger.info("Journal deleted successfully: {}", journalId);

        } catch (Exception e) {
            logger.error("Error deleting journal: {}", journalId, e);
            throw new RuntimeException("Failed to delete journal", e);
        }
    }

    public User getUserWithJournalIds(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public long getUserJournalCount(String userId) {
        return journalRepository.countByUserId(userId);
    }

    public Map<String, Object> getJournalInsights(String userId, String timeRange, Mood moodFilter) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate;

            // Calculate date range based on timeRange parameter
            startDate = switch (timeRange.toLowerCase()) {
                case "week" -> endDate.minusDays(7);
                case "quarter" -> endDate.minusDays(90);
                case "year" -> endDate.minusDays(365);
                default -> endDate.minusDays(30);
            }; // month

            // Get all journals in the time range
            List<Journal> journals;
            if (moodFilter != null) {
                journals = journalRepository.findByUserIdAndMoodAndDateRange(userId,
                        moodFilter, startDate, endDate);
            } else {
                journals = journalRepository.findByUserIdAndDateRange(userId, startDate, endDate);
            }

            // Calculate basic metrics
            int totalEntries = journals.size();
            int totalWords = 0;
            Map<String, Integer> moodCounts = new HashMap<>();
            Map<String, Map<String, Integer>> dailyData = new HashMap<>();
            Map<String, Integer> weeklyActivity = new HashMap<>();

            // Initialize weekly activity
            weeklyActivity.put("Mon", 0);
            weeklyActivity.put("Tue", 0);
            weeklyActivity.put("Wed", 0);
            weeklyActivity.put("Thu", 0);
            weeklyActivity.put("Fri", 0);
            weeklyActivity.put("Sat", 0);
            weeklyActivity.put("Sun", 0);

            // Process each journal entry
            for (Journal journal : journals) {
                // Calculate word count from content
                int wordCount = calculateWordCount(journal.getContent());
                totalWords += wordCount;

                // Count moods
                String mood = journal.getMood().toString().toLowerCase();
                moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);

                // Group by date for trend analysis
                String dateKey = journal.getCreatedAt().toLocalDate().toString();
                dailyData.computeIfAbsent(dateKey, k -> new HashMap<>());
                Map<String, Integer> dayData = dailyData.get(dateKey);
                dayData.put("wordCount", dayData.getOrDefault("wordCount", 0) + wordCount);
                dayData.put("entryCount", dayData.getOrDefault("entryCount", 0) + 1);

                // Count by day of week
                String dayOfWeek = journal.getCreatedAt().getDayOfWeek().toString().substring(0, 3);
                dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
                weeklyActivity.put(dayOfWeek, weeklyActivity.getOrDefault(dayOfWeek, 0) + 1);
            }

            // Calculate averages
            int averageWordsPerEntry = totalEntries > 0 ? Math.round((float) totalWords / totalEntries) : 0;

            // Calculate streaks (consecutive days with entries)
            Map<String, Integer> streaks = calculateStreaks(journals);
            int currentStreak = streaks.get("currentStreak");
            int longestStreak = streaks.get("longestStreak");

            // Prepare mood distribution
            List<Map<String, Object>> moodDistribution = moodCounts.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> moodData = new HashMap<>();
                        moodData.put("mood", entry.getKey());
                        moodData.put("count", entry.getValue());
                        moodData.put("percentage",
                                totalEntries > 0 ? Math.round((entry.getValue() * 100.0) / totalEntries) : 0);
                        return moodData;
                    })
                    .sorted((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")))
                    .toList();

            // Prepare word count trend (last 30 days)
            List<Map<String, Object>> wordCountTrend = dailyData.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .limit(30)
                    .map(entry -> {
                        Map<String, Object> trendData = new HashMap<>();
                        trendData.put("date", entry.getKey());
                        trendData.put("wordCount", entry.getValue().get("wordCount"));
                        trendData.put("entryCount", entry.getValue().get("entryCount"));
                        return trendData;
                    })
                    .toList();

            // Prepare weekly activity
            List<Map<String, Object>> weeklyActivityArray = weeklyActivity.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> activityData = new HashMap<>();
                        activityData.put("day", entry.getKey());
                        activityData.put("entries", entry.getValue());
                        return activityData;
                    })
                    .toList();

            // Build final insights response
            Map<String, Object> insights = new HashMap<>();
            insights.put("totalEntries", totalEntries);
            insights.put("averageWordsPerEntry", averageWordsPerEntry);
            insights.put("longestStreak", longestStreak);
            insights.put("currentStreak", currentStreak);
            insights.put("moodDistribution", moodDistribution);
            insights.put("wordCountTrend", wordCountTrend);
            insights.put("weeklyActivity", weeklyActivityArray);

            logger.info("Generated insights for user: {}, totalEntries: {}, timeRange: {}, moodFilter: {}",
                    userId, totalEntries, timeRange, moodFilter != null ? moodFilter : "none");

            return insights;

        } catch (Exception e) {
            logger.error("Error fetching journal insights for user: {}", userId, e);
            throw new RuntimeException("Failed to fetch journal insights", e);
        }
    }

    private Map<String, Integer> calculateStreaks(List<Journal> journals) {
        if (journals.isEmpty()) {
            Map<String, Integer> streaks = new HashMap<>();
            streaks.put("currentStreak", 0);
            streaks.put("longestStreak", 0);
            return streaks;
        }

        // Sort journals by date
        journals.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));

        // Get unique days with entries
        List<String> entryDays = journals.stream()
                .map(journal -> journal.getCreatedAt().toLocalDate().toString())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        int currentStreak = 0;
        int longestStreak = 0;
        int tempStreak = 1;

        for (int i = 1; i < entryDays.size(); i++) {
            LocalDateTime prevDay = LocalDateTime.parse(entryDays.get(i - 1) + "T00:00:00");
            LocalDateTime currentDay = LocalDateTime.parse(entryDays.get(i) + "T00:00:00");

            // Check if days are consecutive
            if (prevDay.toLocalDate().plusDays(1).equals(currentDay.toLocalDate())) {
                tempStreak++;
            } else {
                longestStreak = Math.max(longestStreak, tempStreak);
                tempStreak = 1;
            }
        }

        longestStreak = Math.max(longestStreak, tempStreak);

        // Calculate current streak (from today backwards)
        LocalDateTime today = LocalDateTime.now();
        String todayStr = today.toLocalDate().toString();

        if (entryDays.contains(todayStr)) {
            currentStreak = 1;
            for (int i = entryDays.size() - 2; i >= 0; i--) {
                LocalDateTime checkDay = LocalDateTime.parse(entryDays.get(i) + "T00:00:00");
                LocalDateTime expectedDay = today.minusDays(currentStreak);

                if (checkDay.toLocalDate().equals(expectedDay.toLocalDate())) {
                    currentStreak++;
                } else {
                    break;
                }
            }
        }

        Map<String, Integer> streaks = new HashMap<>();
        streaks.put("currentStreak", currentStreak);
        streaks.put("longestStreak", longestStreak);
        return streaks;
    }
}

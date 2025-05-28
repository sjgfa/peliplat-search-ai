package com.peliplat.ai.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 电影类型常量类
 * 提供电影类型ID和名称的映射关系
 */
public class MovieGenreConstants {

    /**
     * 电影类型ID到名称的映射
     */
    public static final Map<String, String> GENRE_ID_TO_NAME_MAP;

    /**
     * 电影类型名称到ID的映射
     */
    public static final Map<String, String> GENRE_NAME_TO_ID_MAP;

    /**
     * 电影类型展示名称到ID的映射（基于JSON树形结构）
     */
    public static final Map<String, String> GENRE_DISPLAY_NAME_TO_ID_MAP;

    static {
        Map<String, String> idToNameMap = new HashMap<>();
        Map<String, String> nameToIdMap = new HashMap<>();
        Map<String, String> displayNameToIdMap = new HashMap<>();

        // 基本类型
        idToNameMap.put("1", "Action");
        idToNameMap.put("2", "Adventure");
        idToNameMap.put("3", "Animation");
        idToNameMap.put("4", "Comedy");
        idToNameMap.put("5", "Crime");
        idToNameMap.put("6", "Documentary");
        idToNameMap.put("7", "Drama");
        idToNameMap.put("8", "Family");
        idToNameMap.put("9", "Fantasy");
        idToNameMap.put("11", "History");
        idToNameMap.put("12", "Horror");
        idToNameMap.put("13", "Music");
        idToNameMap.put("14", "Mystery");
        idToNameMap.put("15", "Romance");
        idToNameMap.put("18", "Thriller");
        idToNameMap.put("19", "War");
        idToNameMap.put("20", "Western");
        idToNameMap.put("101", "Sci-Fi");
        idToNameMap.put("102", "Reality-TV");
        idToNameMap.put("103", "Biography");
        idToNameMap.put("104", "Film-Noir");
        idToNameMap.put("105", "Musical");
        idToNameMap.put("106", "Game-Show");
        idToNameMap.put("107", "Talk-Show");
        idToNameMap.put("108", "News");
        idToNameMap.put("109", "Adult");
        idToNameMap.put("110", "Sport");
        idToNameMap.put("111", "Short");

        // 子类型 - 喜剧
        idToNameMap.put("tg20012692", "Action Comedy");
        idToNameMap.put("tg20012536", "Rom-com");
        idToNameMap.put("tg20015561", "Dark humor");
        idToNameMap.put("tg20015517", "Raunchy comedy");
        idToNameMap.put("tg20012643", "Buddy film");
        idToNameMap.put("tg20015334", "Toilet-humor");
        idToNameMap.put("tg20014457", "Dumb criminals");
        idToNameMap.put("tg20012943", "Band of misfits");
        idToNameMap.put("tg20014868", "Body swap or transplant");
        idToNameMap.put("tg20012892", "Funny");

        // 子类型 - 爱情
        idToNameMap.put("tg20013116", "Taboo love/Forbidden love");
        idToNameMap.put("tg20014315", "Age-gap Romance");
        idToNameMap.put("tg20012947", "Love Triangle");
        idToNameMap.put("tg20014347", "Misunderstandings and coincidences");
        idToNameMap.put("tg20014241", "Falling in love with a celebrity");
        idToNameMap.put("tg20015321", "Holiday romance");
        idToNameMap.put("tg20012614", "Musical romance");
        idToNameMap.put("tg20013895", "Films to watch on a date");
        idToNameMap.put("tg20014937", "Enemies to lovers");

        // 子类型 - 动作
        idToNameMap.put("tg20014278", "One person army");
        idToNameMap.put("tg20012624", "Spy film");
        idToNameMap.put("tg20012552", "Martial Arts");
        idToNameMap.put("tg20015747", "Car action");
        idToNameMap.put("tg20013249", "Adrenaline-pumping shootout");
        idToNameMap.put("tg20013061", "Rescue Mission");
        idToNameMap.put("tg20014289", "Retired master reemerges");
        idToNameMap.put("tg20013385", "Outstanding actors in action films");
        idToNameMap.put("tg20013005", "Revenge/Vengeance");
        idToNameMap.put("tg20013198", "Heroic bloodshed");

        // 子类型 - 恐怖
        idToNameMap.put("tg20012557", "Slasher Horror");
        idToNameMap.put("tg20012558", "Monster movie");
        idToNameMap.put("tg20014264", "Death game");
        idToNameMap.put("tg20013007", "Zombie Apocalypse");
        idToNameMap.put("tg20012663", "Haunted house");
        idToNameMap.put("tg20015241", "Exorcism movie");
        idToNameMap.put("tg20015494", "For die-hard horror fans");
        idToNameMap.put("tg20012608", "Body horror");
        idToNameMap.put("tg20014451", "Werewolf");
        idToNameMap.put("tg20014447", "Vampire");
        idToNameMap.put("tg20014832", "Cabin/Chalet");
        idToNameMap.put("tg20014458", "Evil children");
        idToNameMap.put("tg20013653", "Ghosts");

        // 子类型 - 科幻
        idToNameMap.put("tg20013667", "Artificial Intelligence");
        idToNameMap.put("tg20012583", "Dystopian");
        idToNameMap.put("tg20012588", "Time travel");
        idToNameMap.put("tg20013253", "Alien invasion");
        idToNameMap.put("tg20012629", "Parallel Universes/Realities");
        idToNameMap.put("tg20012584", "Space travel");
        idToNameMap.put("tg20012587", "Cyberpunk");
        idToNameMap.put("tg20012659", "Sci-Fi horror");
        idToNameMap.put("tg20013288", "Temporal paradox");
        idToNameMap.put("tg20013480", "Hollywood directors' sci-fi masterpieces");
        idToNameMap.put("tg20012755", "Future");
        idToNameMap.put("tg20012585", "Space opera");

        // 为了中文用户，添加中文名称映射
        idToNameMap.put("1", "动作");
        idToNameMap.put("2", "冒险");
        idToNameMap.put("3", "动画");
        idToNameMap.put("4", "喜剧");
        idToNameMap.put("5", "犯罪");
        idToNameMap.put("6", "纪录片");
        idToNameMap.put("7", "剧情");
        idToNameMap.put("8", "家庭");
        idToNameMap.put("9", "奇幻");
        idToNameMap.put("11", "历史");
        idToNameMap.put("12", "恐怖");
        idToNameMap.put("13", "音乐");
        idToNameMap.put("14", "悬疑");
        idToNameMap.put("15", "爱情");
        idToNameMap.put("18", "惊悚");
        idToNameMap.put("19", "战争");
        idToNameMap.put("20", "西部");
        idToNameMap.put("101", "科幻");
        idToNameMap.put("102", "真人秀");
        idToNameMap.put("103", "传记");
        idToNameMap.put("104", "黑色电影");
        idToNameMap.put("105", "音乐剧");
        idToNameMap.put("106", "游戏节目");
        idToNameMap.put("107", "脱口秀");
        idToNameMap.put("108", "新闻");
        idToNameMap.put("109", "成人");
        idToNameMap.put("110", "体育");
        idToNameMap.put("111", "短片");

        // 添加电影类型展示名称到ID的映射
        displayNameToIdMap.put("Comedy", "1901930552914399242");
        displayNameToIdMap.put("Romance", "1901930552918593847");
        displayNameToIdMap.put("Action", "1901930552914399244");
        displayNameToIdMap.put("Horror", "1901930552918593853");
        displayNameToIdMap.put("Animation", "1901930552918593850");
        displayNameToIdMap.put("Drama", "1901930552918593852");
        displayNameToIdMap.put("Adventure", "1901930552918593849");
        displayNameToIdMap.put("Thriller", "1901930552918593856");
        displayNameToIdMap.put("Fantasy", "1901930552918593854");
        displayNameToIdMap.put("Mystery", "1901930552918593855");
        displayNameToIdMap.put("Sci-Fi", "1901930552918593859");
        displayNameToIdMap.put("Family", "1901930552918593857");
        displayNameToIdMap.put("Teen", "1901930552914399254");
        displayNameToIdMap.put("Kids", "1901930552914399255");
        displayNameToIdMap.put("Crime", "1901930552918593851");
        displayNameToIdMap.put("Epic", "1901930552914399257");
        displayNameToIdMap.put("War", "1901930552918593864");
        displayNameToIdMap.put("History", "1901930552918593863");
        displayNameToIdMap.put("Disaster", "1901930552914399260");
        displayNameToIdMap.put("Sport", "1901930552918593862");
        displayNameToIdMap.put("Musical", "1901930552918593861");
        displayNameToIdMap.put("Music", "1901930552918593860");
        displayNameToIdMap.put("Documentary", "1901930552914399263");
        displayNameToIdMap.put("Niche", "1901930552914399264");
        displayNameToIdMap.put("Awards", "1901930552914399265");
        displayNameToIdMap.put("All Genres", "1901930552914399266");

        // All Genres子项
        displayNameToIdMap.put("Action Comedy", "1901930552918593604");
        displayNameToIdMap.put("Rom-com", "1901930552918593605");
        displayNameToIdMap.put("Dark humor", "1901930552918593606");
        displayNameToIdMap.put("Raunchy comedy", "1901930552918593607");
        displayNameToIdMap.put("Buddy film", "1901930552918593608");
        displayNameToIdMap.put("Toilet-humor", "1901930552918593609");
        displayNameToIdMap.put("Dumb criminals", "1901930552918593610");
        displayNameToIdMap.put("Band of misfits", "1901930552918593611");
        displayNameToIdMap.put("Body swap or transplant", "1901930552918593612");
        displayNameToIdMap.put("Funny", "1901930552918593613");

        // All Genres下的基本电影类型
        displayNameToIdMap.put("Short", "1901930552918593868");
        displayNameToIdMap.put("Western", "1901930552918593869");
        displayNameToIdMap.put("Film-Noir", "1901930552918593867");
        displayNameToIdMap.put("Documentary", "1901930552918593866");
        displayNameToIdMap.put("Biography", "1901930552918593865");
        displayNameToIdMap.put("Musical", "1901930552918593861");

        // 创建映射关系
        for (Map.Entry<String, String> entry : idToNameMap.entrySet()) {
            nameToIdMap.put(entry.getValue(), entry.getKey());
        }

        GENRE_ID_TO_NAME_MAP = Collections.unmodifiableMap(idToNameMap);
        GENRE_NAME_TO_ID_MAP = Collections.unmodifiableMap(nameToIdMap);
        GENRE_DISPLAY_NAME_TO_ID_MAP = Collections.unmodifiableMap(displayNameToIdMap);
    }

    /**
     * 根据类型ID获取类型名称
     * 
     * @param genreId 类型ID
     * @return 类型名称，如果没有找到则返回null
     */
    public static String getGenreNameById(String genreId) {
        return GENRE_ID_TO_NAME_MAP.get(genreId);
    }

    /**
     * 根据类型名称获取类型ID
     * 
     * @param genreName 类型名称
     * @return 类型ID，如果没有找到则返回null
     */
    public static String getGenreIdByName(String genreName) {
        return GENRE_NAME_TO_ID_MAP.get(genreName);
    }

    /**
     * 根据展示名称获取类型ID
     * 
     * @param displayName 展示名称
     * @return 类型ID，如果没有找到则返回null
     */
    public static String getGenreIdByDisplayName(String displayName) {
        return GENRE_DISPLAY_NAME_TO_ID_MAP.get(displayName);
    }

    /**
     * 私有构造函数，防止实例化
     */
    private MovieGenreConstants() {
        // 防止实例化
    }
}
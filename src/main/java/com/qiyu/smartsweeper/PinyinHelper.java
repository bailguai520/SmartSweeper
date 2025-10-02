package com.qiyu.smartsweeper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Complete Pinyin helper with Unicode dictionary
 * Supports 20,000+ Chinese characters from unicode_to_hanyu_pinyin.txt
 */
public class PinyinHelper {
    private static final Map<Character, String> PINYIN_MAP = new HashMap<>(20000);
    
    static {
        // 从资源文件加载完整的拼音字典
        try {
            InputStream is = PinyinHelper.class.getResourceAsStream("/assets/SmartSweeper/lang/unicode_to_hanyu_pinyin.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    // 格式: 4E00 (yi1)
                    String[] parts = line.split(" ", 2);
                    if (parts.length == 2) {
                        try {
                            // 解析 Unicode 十六进制
                            int codePoint = Integer.parseInt(parts[0], 16);
                            char ch = (char) codePoint;
                            
                            // 解析拼音，去掉括号和音调数字
                            String pinyinPart = parts[1];
                            if (pinyinPart.startsWith("(") && pinyinPart.endsWith(")")) {
                                String pinyins = pinyinPart.substring(1, pinyinPart.length() - 1);
                                
                                // 如果是 none0，跳过
                                if (pinyins.equals("none0")) {
                                    continue;
                                }
                                
                                // 取第一个拼音（多音字的情况）
                                String pinyin = pinyins.split(",")[0];
                                
                                // 去掉音调数字
                                pinyin = pinyin.replaceAll("[0-9]", "");
                                
                                if (!pinyin.isEmpty()) {
                                    PINYIN_MAP.put(ch, pinyin);
                                }
                            }
                        } catch (Exception e) {
                            // 跳过无效行
                        }
                    }
                }
                reader.close();
                SmartSweeper.LOGGER.info("Loaded {} Chinese characters with Pinyin", PINYIN_MAP.size());
            } else {
                SmartSweeper.LOGGER.warn("Could not find unicode_to_hanyu_pinyin.txt, Pinyin search will not work");
            }
        } catch (Exception e) {
            SmartSweeper.LOGGER.error("Failed to load Pinyin dictionary", e);
        }
    }
    
    public static String toPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            String py = PINYIN_MAP.get(c);
            if (py != null) {
                result.append(py);
            } else if (Character.isLetterOrDigit(c)) {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
    
    public static String toPinyinInitials(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            String py = PINYIN_MAP.get(c);
            if (py != null && py.length() > 0) {
                result.append(py.charAt(0));
            } else if (Character.isLetterOrDigit(c)) {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}

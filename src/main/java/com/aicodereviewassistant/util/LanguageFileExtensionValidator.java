package com.aicodereviewassistant.util;

import com.aicodereviewassistant.exception.BadRequestException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LanguageFileExtensionValidator {

    private static final Map<String, String> LANGUAGE_ALIAS_TO_CANONICAL = Map.ofEntries(
            Map.entry("java", "java"),
            Map.entry("javascript", "javascript"),
            Map.entry("js", "javascript"),
            Map.entry("typescript", "typescript"),
            Map.entry("ts", "typescript"),
            Map.entry("python", "python"),
            Map.entry("py", "python"),
            Map.entry("c", "c"),
            Map.entry("c++", "cpp"),
            Map.entry("cpp", "cpp"),
            Map.entry("cxx", "cpp"),
            Map.entry("go", "go"),
            Map.entry("rust", "rust"),
            Map.entry("kotlin", "kotlin"),
            Map.entry("kt", "kotlin"),
            Map.entry("swift", "swift"),
            Map.entry("php", "php"),
            Map.entry("ruby", "ruby"),
            Map.entry("rb", "ruby"),
            Map.entry("scala", "scala"),
            Map.entry("cs", "csharp"),
            Map.entry("c#", "csharp")
    );

    private static final Map<String, Set<String>> CANONICAL_LANGUAGE_EXTENSIONS = Map.ofEntries(
            Map.entry("java", Set.of(".java")),
            Map.entry("javascript", Set.of(".js", ".mjs", ".cjs")),
            Map.entry("typescript", Set.of(".ts", ".tsx")),
            Map.entry("python", Set.of(".py")),
            Map.entry("c", Set.of(".c", ".h")),
            Map.entry("cpp", Set.of(".cpp", ".cc", ".cxx", ".hpp", ".hh", ".hxx")),
            Map.entry("go", Set.of(".go")),
            Map.entry("rust", Set.of(".rs")),
            Map.entry("kotlin", Set.of(".kt", ".kts")),
            Map.entry("swift", Set.of(".swift")),
            Map.entry("php", Set.of(".php")),
            Map.entry("ruby", Set.of(".rb")),
            Map.entry("scala", Set.of(".scala")),
            Map.entry("csharp", Set.of(".cs"))
    );

    private LanguageFileExtensionValidator() {
    }

    public static void validateOrThrow(String fileName, String language, String sourceCode) {
        String canonicalLanguage = canonicalLanguage(language);
        if (canonicalLanguage == null) {
            return;
        }

        String normalizedFileName = fileName == null ? "" : fileName.trim().toLowerCase(Locale.ROOT);
        Set<String> allowedExtensions = CANONICAL_LANGUAGE_EXTENSIONS.getOrDefault(canonicalLanguage, Set.of());
        boolean match = allowedExtensions.stream().anyMatch(normalizedFileName::endsWith);
        if (!match) {
            throw new BadRequestException("Invalid file extension for language '" + language
                    + "'. Expected one of: " + String.join(", ", allowedExtensions));
        }

        String detected = detectLanguageFromCode(sourceCode);
        if (detected != null && !detected.equals(canonicalLanguage)) {
            throw new BadRequestException("Language mismatch: selected '" + language + "', but code looks like '" + detected + "'");
        }
    }

    public static void validateLanguageAndCodeOrThrow(String language, String sourceCode) {
        String canonicalLanguage = canonicalLanguage(language);
        if (canonicalLanguage == null) {
            return;
        }
        String detected = detectLanguageFromCode(sourceCode);
        if (detected != null && !detected.equals(canonicalLanguage)) {
            throw new BadRequestException("Language mismatch: selected '" + language + "', but code looks like '" + detected + "'");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String canonicalLanguage(String language) {
        return LANGUAGE_ALIAS_TO_CANONICAL.get(normalize(language));
    }

    private static String detectLanguageFromCode(String sourceCode) {
        String code = sourceCode == null ? "" : sourceCode.toLowerCase(Locale.ROOT);
        if (code.isBlank()) {
            return null;
        }

        if (code.contains("<?php")) {
            return "php";
        }
        if (code.contains("public static void main(") || code.contains("system.out.println(") || code.contains("import java.")) {
            return "java";
        }
        if (code.contains("#include <") || code.contains("std::") || code.contains("using namespace std")) {
            return code.contains("cout") || code.contains("cin") ? "cpp" : "c";
        }
        if (code.contains("console.log(") || code.contains("function ") || code.contains("=>")) {
            return "javascript";
        }
        if (code.contains("let ") || code.contains("const ") || code.contains("interface ") || code.contains(": string")) {
            return "typescript";
        }
        if (code.contains("def ") || code.contains("import ") && code.contains(":") && code.contains("print(")) {
            return "python";
        }
        if (code.contains("fn main(") || code.contains("let mut ") || code.contains("println!")) {
            return "rust";
        }
        if (code.contains("func main()") || code.contains("package main")) {
            return "go";
        }
        if (code.contains("fun main(") || code.contains("val ") || code.contains("var ")) {
            return "kotlin";
        }
        if (code.contains("using system;") || code.contains("namespace ") && code.contains("class ") && code.contains("console.writeline(")) {
            return "csharp";
        }
        if (code.contains("puts ") || code.contains("end\n") || code.contains("class ") && code.contains("def ")) {
            return "ruby";
        }
        if (code.contains("object ") && code.contains("def main(")) {
            return "scala";
        }
        return null;
    }
}

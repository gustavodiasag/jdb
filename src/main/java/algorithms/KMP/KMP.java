package main.java.algorithms.KMP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KMP {
  public static int comparison = 0;
  public static int[] prefixTable;
  public static String pattern;
  public static long executionTime;
  public static String path = "src/main/resources/input.txt";

  public static void search(String pattern) {
    KMP.pattern = pattern;
    List<Integer> occurrences = searchPattern(readTextFromFile(path), pattern);

    printResults(occurrences);
  }

  // Função para buscar todas as ocorrências do padrão em um texto
  public static List<Integer> searchPattern(String text, String pattern) {
    KMP.pattern = pattern;
    List<Integer> occurrences = new ArrayList<>();

    long startTime = System.currentTimeMillis();

    // Construir tabela de prefixos
    prefixTable = buildPrefixTable(pattern);

    int textIndex = 0;
    int patternIndex = 0;

    // Realizar a busca do padrão no texto
    while (textIndex < text.length()) {
      comparison++;
      if (pattern.charAt(patternIndex) == text.charAt(textIndex)) {
        patternIndex++;
        textIndex++;
      }

      // Se o padrão foi encontrado
      if (patternIndex == pattern.length()) {
        // Adicionar a posição da ocorrência à lista
        occurrences.add(textIndex - patternIndex);

        // Atualizar o índice do padrão baseado na tabela de prefixos
        patternIndex = prefixTable[patternIndex - 1];
      }

      // Se houve uma não correspondência
      else if (textIndex < text.length() && pattern.charAt(patternIndex) != text.charAt(textIndex)) {
        if (patternIndex != 0) {
          // Atualizar o índice do padrão baseado na tabela de prefixos
          patternIndex = prefixTable[patternIndex - 1];
        } else {
          textIndex++;
        }

      }
    }

    long endTime = System.currentTimeMillis();
    executionTime = endTime - startTime;

    return occurrences;
  }

  // Função para construir a tabela de prefixos
  private static int[] buildPrefixTable(String pattern) {
    int[] prefixTable = new int[pattern.length()];
    int prefixIndex = 0;
    int i = 1;

    // Construir a tabela
    while (i < pattern.length()) {
      if (pattern.charAt(i) == pattern.charAt(prefixIndex)) {
        prefixTable[i] = prefixIndex + 1;
        prefixIndex++;
        i++;
      } else {
        if (prefixIndex != 0) {
          // Atualizar o índice do prefixo baseado na tabela
          prefixIndex = prefixTable[prefixIndex - 1];
        } else {
          prefixTable[i] = 0;
          i++;
        }
      }
    }

    return prefixTable;
  }

  public static void printPrefixTable() {
    System.out.println("\nTabela de prefixos: ");
    for (int i = 0; i < pattern.length(); i++) {
      System.out.print(i + " ");
    }
    System.out.println();
    for (int i = 0; i < pattern.length(); i++) {
      System.out.print(pattern.charAt(i) + " ");
    }
    System.out.println();
    for (int i : prefixTable) {
      System.out.print(i + " ");
    }
    System.out.println();
  }

  //Função para exibir os resultados do casamento de padrões
  public static void printResults(List<Integer> occurrences) {
    printPrefixTable();
    System.out.println("\nComparisons: " + comparison);
    System.out.println("\nExecution time: " + executionTime + " ms");

    if (occurrences.isEmpty()) {
      System.out.println("\nNenhuma ocorrência encontrada.");
    } else {
      System.out.println("\nOcorrências encontradas nas seguintes posições:");
      for (int index : occurrences) {
        System.out.print(index + " ");
      }
      System.out.println("\n");
    }
  }

  // Função para ler o texto de um arquivo e retornar uma String
  private static String readTextFromFile(String fileName) {
    StringBuilder text = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        text.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return text.toString();
  }
}

package main.java.database;

public interface Sorting {
    
    default void quickSort(Record[] records, int left, int right ) {
        int i = left;
        int j = right;
        Record pivot = records[(right + left)/2];
        
        while (i <= j) {
            while (records[i] != null && records[i].getEpisodes() < pivot.getEpisodes())
                i++;
            
            while (records[j] != null && records[j].getEpisodes() > pivot.getEpisodes())
                j--;
            
            if (i <= j) {
                swap(records,i, j);
                i++;
                j--;
            }
        }
        
        if (left < j)
            quickSort(records, left, j);
        
        if (i < right)
            quickSort(records, i, right);
    }

    private static void swap(Record[] records, int i, int j) {
        Record tmp = records[i];
        records[i] = records[j];
        records[j] = tmp;
    }
}
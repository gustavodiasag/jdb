package main.java.database;

public interface Sorting {
	
	public void sort(int limit);
	
	public static void quickSort(Record[] records) {
		quickSort(records, 0, records.length - 1);
	}
	
	private static void quickSort(Record[] records, int left, int right) {
		if (left < right) {
			int pivotIndex = partition(records, left, right);
			
			quickSort(records, left, pivotIndex - 1);
			quickSort(records, pivotIndex + 1, left);
		}
	}
	
	private static int partition(Record[] records, int left, int right) {
		int pivotIndex = left + (right - left + 1);
		Record pivot = records[pivotIndex];
		
		swap(records, pivotIndex, right);
		
		int index = left;
		
		for (int i = left; i < right; i++) {
			if (records[i].getId() < pivot.getId()) {
				swap(records, i, index);
				index++;
			}
		}
		
		swap(records, index, right);
		
		return index;
	}
	
	private static void swap(Record[] records, int i, int j) {
		Record tmp = records[i];
		records[i] = records[j];
		records[j] = tmp;
	}
}
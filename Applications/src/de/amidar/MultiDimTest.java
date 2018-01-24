package de.amidar;

public class MultiDimTest {

	public static void main(String[] args) {

//		int [][] a = new int[][] {{0,1,2},{3,4,5},{6,7,8}};
//		
//		int sum = 0;
//		
//		for(int i = 0; i<3; i++){
//			for(int j = 0; j<3; j++){
//				sum += a[i][j];
//			}
//		}
//		
		
		int [][][] test = new int [3][3][3];
		
		int cnt = 0;
		
		for(int i = 0; i<3; i++){
			for(int j = 0; j<3; j++){
				for( int k = 0; k<3; k++){
					test[i][j][k] = cnt++;
				}
			}
		}
		cnt = 0;

		for(int i = 0; i<3; i++){
			for(int j = 0; j<3; j++){
				for( int k = 0; k<3; k++){
					cnt += test[i][j][k];
				}
			}
		}
		
		System.out.println(cnt);
		
	}

}

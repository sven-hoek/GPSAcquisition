package de.amidar;

public class MatrixMult {

	public static void main(String[] args) {
//		int [][] a = {{1,0,0},{0,1,0},{0,0,1},{1,0,0},{0,1,0},{0,0,1}};
//		
//		int [][] b = {{1,2,3},{4,5,6},{7,8,9}};
		
//		long start = System.currentTimeMillis();
		
		float a [][] = new float [208][208];
		
		float [] b [] = new float [208][208];
		
		for(int i = 0; i< 100; i++){
		float [][] c = matrixMult(a,b);
		}
		
//		long stop = System.currentTimeMillis();
////		
//		System.out.println(stop-start);
//		for(int i = 0; i < a.length; i++){
//			for(int j = 0; j<b[0].length; j++){
//				System.out.print(c[i][j]+ " ");
//			}System.out.println();
//		}

	}
	
	
	public static float[][] matrixMult(float[][] a, float[][] b){
		
		
		int ma = a.length;
		int na = a[0].length;
		
		int mb = b.length;
		int nb = b[0].length;
		
		float[][] res = new float [ma][nb];
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < ma; i=i+4){
			for(int j = 0; j< nb; j=j+4){
				float val = 0;
				float val2 = 0;
				float val3 = 0;
				float val4 = 0;
				float val5 = 0;
				float val6 = 0;
				float val7 = 0;
				float val8 = 0;
				float val9 = 0;
				float val10 = 0;
				float val11 = 0;
				float val12 = 0;
				float val13 = 0;
				float val14 = 0;
				float val15 = 0;
				float val16 = 0;
				for(int k = 0; k < nb; k++){
					val += a[i][k]*b[k][j];
					val2 += a[i][k]*b[k][j+1];
					val3 += a[i][k]*b[k][j+2];
					val4 += a[i][k]*b[k][j+3];
					val5 += a[i+1][k]*b[k][j];
					val6 += a[i+1][k]*b[k][j+1];
					val7 += a[i+1][k]*b[k][j+2];
					val8 += a[i+1][k]*b[k][j+3];
					val9 += a[i+2][k]*b[k][j];
					val10 += a[i+2][k]*b[k][j+1];
					val11 += a[i+2][k]*b[k][j+2];
					val12 += a[i+2][k]*b[k][j+3];
					val13 += a[i+3][k]*b[k][j];
					val14 += a[i+3][k]*b[k][j+1];
					val15 += a[i+3][k]*b[k][j+2];
					val16 += a[i+3][k]*b[k][j+3];
				}
				
				
				res[i][j] = val;
				res[i][j+1] = val2;
				res[i][j+2] = val3;
				res[i][j+3] = val4;
				res[i+1][j] = val5;
				res[i+1][j+2] = val6;
				res[i+1][j+2] = val7;
				res[i+1][j+3] = val8;
				res[i+2][j] = val9;
				res[i+2][j+1] = val10;
				res[i+2][j+2] = val11;
				res[i+2][j+3] = val12;
				res[i+3][j] = val13;
				res[i+3][j+1] = val14;
				res[i+3][j+2] = val15;
				res[i+3][j+3] = val16;
			}
		}
		long stop = System.currentTimeMillis();
		System.out.println("TiME : " + (stop-start));
		
		
		return res;
	}
	

}

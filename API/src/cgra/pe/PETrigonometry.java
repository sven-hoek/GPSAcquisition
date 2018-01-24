package cgra.pe;

public class PETrigonometry {
	
//	private static final double PId = 3.14159265359;
	private static final float PI = 3.14159265359f;
	
	private float x,y,z;
	private double xd,yd,zd;
//	private static PETrigonometry pet;
	
	public final float [] alpha = {0.78539816339745f,   0.46364760900081f,   0.24497866312686f,   0.12435499454676f,
		    0.06241880999596f,   0.03123983343027f,   0.01562372862048f,   0.00781234106010f,
		    0.00390623013197f,   0.00195312251648f,   0.00097656218956f,   0.00048828121119f,
		    0.00024414062015f,   0.00012207031189f,   0.00006103515617f,   0.00003051757812f,
		    0.00001525878906f,   0.00000762939453f,   0.00000381469727f,   0.00000190734863f,
		    0.00000095367432f,   0.00000047683716f,   0.00000023841858f,   0.00000011920929f,
		    0.00000005960464f,   0.00000002980232f,   0.00000001490116f,   0.00000000745058f }; 
	
	public final float [] k = {0.70710678118655f,   0.63245553203368f,   0.61357199107790f,   0.60883391251775f,
		    0.60764825625617f,   0.60735177014130f,   0.60727764409353f,   0.60725911229889f,
		    0.60725447933256f,   0.60725332108988f,   0.60725303152913f,   0.60725295913894f,
		    0.60725294104140f,   0.60725293651701f,   0.60725293538591f,   0.60725293510314f,
		    0.60725293503245f,   0.60725293501477f,   0.60725293501035f,   0.60725293500925f,
		    0.60725293500897f,   0.60725293500890f,   0.60725293500889f,   0.60725293500888f};
	
	public final double [] alphad = {0.78539816339745 ,   0.46364760900081 ,   0.24497866312686 ,   0.12435499454676 ,
		    0.06241880999596 ,   0.03123983343027 ,   0.01562372862048 ,   0.00781234106010 ,
		    0.00390623013197 ,   0.00195312251648 ,   0.00097656218956 ,   0.00048828121119 ,
		    0.00024414062015 ,   0.00012207031189 ,   0.00006103515617 ,   0.00003051757812 ,
		    0.00001525878906 ,   0.00000762939453 ,   0.00000381469727 ,   0.00000190734863 ,
		    0.00000095367432 ,   0.00000047683716 ,   0.00000023841858 ,   0.00000011920929 ,
		    0.00000005960464 ,   0.00000002980232 ,   0.00000001490116 ,   0.00000000745058f }; 
	
	public final double [] kd = {0.70710678118655 ,   0.63245553203368 ,   0.61357199107790 ,   0.60883391251775 ,
		    0.60764825625617 ,   0.60735177014130 ,   0.60727764409353 ,   0.60725911229889 ,
		    0.60725447933256 ,   0.60725332108988 ,   0.60725303152913 ,   0.60725295913894 ,
		    0.60725294104140 ,   0.60725293651701 ,   0.60725293538591 ,   0.60725293510314 ,
		    0.60725293503245 ,   0.60725293501477 ,   0.60725293501035 ,   0.60725293500925 ,
		    0.60725293500897 ,   0.60725293500890 ,   0.60725293500889 ,   0.60725293500888};
	
	private void cordic(float betha, int n){
		if(betha < -PI/2 ||  betha > PI/2){
			if(betha < 0)
				cordic(betha + PI, n);
			else
				cordic(betha - PI, n);
			x=-x;
			y=-y;
			return;
		}
		x = 1;
		y = 0;
		z = betha;
		float powerOfTwo = 1;
		int sigma = 0;
		for(int i = 0; i < n; i++ ){
			if(z<=0)
				sigma = -1;
			else
				sigma = 1;
			float xOld = x;
			x = x - sigma*y*powerOfTwo;
			y = y + sigma*xOld*powerOfTwo;
			z = z - sigma*alpha[i];
			powerOfTwo=powerOfTwo/2;
		}
		x = x * k[n];
		y = y * k[n];
	}
	
	public static float cos(float betha){
		PETrigonometry pet = new PETrigonometry();
		pet.cordic(betha,7);
		return pet.x;
	}
	
	public static float sin(float betha){
		PETrigonometry pet = new PETrigonometry();
		pet.cordic(betha,7);
		return pet.y;
	}
	
	private void cordic(double betha, int n){
		if(betha < -PI/2 ||  betha > PI/2){
			if(betha < 0)
				cordic(betha + PI, n);
			else
				cordic(betha - PI, n);
			xd=-xd;
			yd=-yd;
			return;
		}
		xd = 1;
		yd = 0;
		zd = betha;
		float powerOfTwo = 1;
		int sigma = 0;
		for(int i = 0; i < n; i++ ){
			if(zd<=0)
				sigma = -1;
			else
				sigma = 1;
			double xOld = xd;
			xd = xd - sigma*yd*powerOfTwo;
			yd = yd + sigma*xOld*powerOfTwo;
			zd = zd - sigma*alphad[i];
			powerOfTwo=powerOfTwo/2;
		}
		xd = xd * kd[n];
//		System.out.println('#');
//		System.out.println(betha);
//		System.out.println(xd);
		yd = yd * kd[n];
	}
	
	public static double cos(double betha){
		PETrigonometry pet = new PETrigonometry();
		pet.cordic(betha,23);
		return pet.xd;
	}
	
	public static double sin(double betha){
		PETrigonometry pet = new PETrigonometry();
		pet.cordic(betha,23);
		return pet.yd;
	}
	
	public static void main(String[] args){
		
		
		System.out.println(cos(3.141592f));
		
		
	}
	
	
}